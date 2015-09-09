package br.gov.pbh.prodabel.happyEyeballs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Happy EyeBalls em Java
 * 
 * Algoritmo RFC 6555.
 * 
 * @author guilherme
 * @version 0.1
 */
public class HappyEyeballs {

	/**
	 * Cache para armazenar as resoluções dos nomes.
	 */
	private static final Map<String, CacheItem> CACHE = new ConcurrentHashMap<String, CacheItem>();
	/**
	 * Semáforo para bloquear as threads para cálculo de tempo de expiração.
	 */
	private static final Object MUTEX = new Object();
	/**
	 * Instância única do objeto (Singleton).
	 */
	private static HappyEyeballs SingleHappyEyeballs = null;

	/**
	 * Pool de threads para paralelizar a resolução de nomes.
	 */
	private final ExecutorService EXECUTOR;
	/**
	 * Tempo de expiração da resolução do nome.
	 */
	private final long TEMPO_EXPIRACAO;
	/**
	 * Tempo de time out da conexão.
	 */
	private final long TEMPO_TIMEOUT;

	/**
	 * Construtor privado para garantir única instancia da classe.
	 * 
	 * @param TEMPO_EXPIRACAO
	 *            Tempo de expiração da resolução de nome.
	 * @param TEMPO_TIMEOUT
	 *            Tempo de time out de conexão.
	 * @param numThread
	 *            Número de threads do pool de threads.
	 */
	private HappyEyeballs(long TEMPO_EXPIRACAO, long TEMPO_TIMEOUT, int numThread) {
		super();
		this.TEMPO_EXPIRACAO = TEMPO_EXPIRACAO;
		this.TEMPO_TIMEOUT = TEMPO_TIMEOUT;
		this.EXECUTOR = Executors.newFixedThreadPool(numThread);
	}

	/**
	 * Retorna a única instancia da classe (Padrão singleton).
	 * 
	 * @return Única instância da classe.
	 */
	public static HappyEyeballs getSingleHappyEyeballs() {
		// TODO Forma melhor de fornecer os parâmetros de configuração do
		// algoritmo.
		if (SingleHappyEyeballs == null)
			SingleHappyEyeballs = new HappyEyeballs(5000L, 300L, 4);
		return SingleHappyEyeballs;
	}

	/**
	 * Finaliza o pool de threads e limpa o cache para evitar memory leak,
	 * executar ao finalizar o programa.
	 */
	public void terminarPoolThread() {
		EXECUTOR.shutdown();
		CACHE.clear();
	}

	/**
	 * Obtem o ip segundo o algoritmo Happy Eyeballs
	 * 
	 * @param nomeRede
	 *            Nome do servidor a ser resolvido.
	 * @param porta
	 *            Porta para teste de conexão.
	 * @return O ip resolvido ou null caso ocorra algum problema.
	 * @throws IOException
	 *             Caso ocorra alguma exceção.
	 */
	public InetAddress obterIP(String nomeRede, int porta) throws IOException {
		// Varre o cache
		if (CACHE.containsKey(nomeRede)) {
			final CacheItem item = CACHE.get(nomeRede);
			long tempoDecorrido = 0L;
			// Verifica se o tempo expirou
			synchronized (MUTEX) {
				tempoDecorrido = System.currentTimeMillis() - item.getTempoAcesso();
			}
			if (tempoDecorrido < TEMPO_EXPIRACAO) {
				return item.getEndereco();
			} else {
				CACHE.remove(nomeRede);
			}
		}
		// Busca todos os ips
		final InetAddress[] enderecos = InetAddress.getAllByName(nomeRede);
		final List<Inet4Address> enderecosIPV4 = new ArrayList<Inet4Address>();
		final List<Inet6Address> enderecosIPV6 = new ArrayList<Inet6Address>();
		// Separa os IPs
		for (InetAddress endereco : enderecos) {
			if (endereco instanceof Inet4Address) {
				enderecosIPV4.add((Inet4Address) endereco);
			} else {
				enderecosIPV6.add((Inet6Address) endereco);
			}
		}
		// Busca o melhor IP
		InetAddress melhorIP = obterMelhorIP(enderecosIPV4, enderecosIPV6, porta);
		enderecosIPV4.clear();
		enderecosIPV6.clear();
		CacheItem item = new CacheItem(melhorIP, System.currentTimeMillis());
		CACHE.put(nomeRede, item);
		return melhorIP;
	}

	/**
	 * Obtem o melhor IP usando busca em threads e conexão assincrona.
	 * 
	 * @param enderecosIPV4
	 *            Lista de IPV4
	 * @param enderecosIPV6
	 *            Lista de IPV6
	 * @param porta
	 *            Porta de conexão
	 * @return O melhor IP
	 * @throws IOException
	 *             Exceção caso ocorra algum problema.
	 */
	private InetAddress obterMelhorIP(List<Inet4Address> enderecosIPV4, List<Inet6Address> enderecosIPV6, int porta)
			throws IOException {
		InetAddress melhor = null;
		Object[] melhorIPV6 = null;
		Object[] melhorIPV4 = null;
		MelhorIP ipv6Tarefa = new MelhorIP(TEMPO_TIMEOUT, enderecosIPV6, porta);
		MelhorIP ipv4Tarefa = new MelhorIP(TEMPO_TIMEOUT, enderecosIPV4, porta);
		Future<Object[]> ipv6Futuro = EXECUTOR.submit(ipv6Tarefa);
		Future<Object[]> ipv4Futuro = EXECUTOR.submit(ipv4Tarefa);
		// Dispara as tarefas de verificar o melhor IP em paralelo
		try {
			melhorIPV6 = ipv6Futuro.get();
		} catch (InterruptedException e) {
			System.out.println("Rede IPV6 fora do ar");
		} catch (ExecutionException e) {
			System.out.println("Não foi possível conectar-se IPV6: " + e.getCause().getLocalizedMessage());
		}
		try {
			melhorIPV4 = ipv4Futuro.get();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			System.out.println("Não foi possível conectar-se IPV4: " + e.getCause().getLocalizedMessage());
		}
		// Verifica se existem endereços IPV6
		if (melhorIPV6 != null && melhorIPV6[0] != null) {
			// Verifica tempo de conexão
			if ((Long) melhorIPV4[1] < (Long) melhorIPV6[1]) {
				melhor = (InetAddress) melhorIPV4[0];
			} else {
				melhor = (InetAddress) melhorIPV6[0];
			}
		} else {
			// Caso não existam IPV6 o melhor IPV4 é escolhido.
			melhor = (InetAddress) melhorIPV4[0];
		}
		return melhor;
	}

	// Simples teste
	public static void main(String[] args) {
		try {
			HappyEyeballs.getSingleHappyEyeballs().obterIP("www.google.com.br", 80);
			HappyEyeballs.getSingleHappyEyeballs().obterIP("www.facebook.com.br", 80);
			HappyEyeballs.getSingleHappyEyeballs().obterIP("www.yahoo.com.br", 80);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			HappyEyeballs.getSingleHappyEyeballs().terminarPoolThread();
		}

	}

}
