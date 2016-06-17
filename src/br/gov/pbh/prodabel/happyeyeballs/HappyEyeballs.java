package br.gov.pbh.prodabel.happyeyeballs;

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
 * Happy EyeBalls em Java. Algoritmo RFC 6555.
 * 
 * @author guilherme
 * @version 0.1
 */
public final class HappyEyeballs {

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
  private static HappyEyeballs singleHappyEyeballs = null;

  /**
   * Pool de threads para paralelizar a resolução de nomes.
   */
  private final ExecutorService executor;
  /**
   * Tempo de expiração da resolução do nome.
   */
  private final long tempoExpiracao;
  /**
   * Tempo de time out da conexão.
   */
  private final long tempoTimeOut;

  /**
   * Construtor privado para garantir única instancia da classe.
   * 
   * @param TEMPO_EXPIRACAO
   *          Tempo de expiração da resolução de nome.
   * @param TEMPO_TIMEOUT
   *          Tempo de time out de conexão.
   * @param numThread
   *          Número de threads do pool de threads.
   */
  private HappyEyeballs(final long tempoExpiracao, final long tempoTimeOut, final int numThread) {
    super();
    this.tempoExpiracao = tempoExpiracao;
    this.tempoTimeOut = tempoTimeOut;
    this.executor = Executors.newFixedThreadPool(numThread);
  }

  /**
   * Retorna a única instancia da classe (Padrão singleton).
   * 
   * @return Única instância da classe.
   */
  public static HappyEyeballs getSingleHappyEyeballs() {
    // TODO Forma melhor de fornecer os parâmetros de configuração do
    // algoritmo.
    if (singleHappyEyeballs == null) {
      singleHappyEyeballs = new HappyEyeballs(5000L, 300L, 4);
    }
    return singleHappyEyeballs;
  }

  /**
   * Finaliza o pool de threads e limpa o cache para evitar memory leak.
   * Executar ao finalizar o programa.
   */
  public void terminarPoolThread() {
    executor.shutdown();
    CACHE.clear();
  }

  /**
   * Obtem o ip segundo o algoritmo Happy Eyeballs.
   * 
   * @param nomeRede
   *          Nome do servidor a ser resolvido.
   * @param porta
   *          Porta para teste de conexão.
   * @return O ip resolvido ou null caso ocorra algum problema.
   * @throws IOException
   *           Caso ocorra alguma exceção.
   */
  public InetAddress obterIp(final String nomeRede, final int porta) throws IOException {
    // Varre o cache
    if (CACHE.containsKey(nomeRede)) {
      final CacheItem item = CACHE.get(nomeRede);
      long tempoDecorrido = 0L;
      // Verifica se o tempo expirou
      synchronized (MUTEX) {
        tempoDecorrido = System.currentTimeMillis() - item.getTempoAcesso();
      }
      if (tempoDecorrido < tempoExpiracao) {
        return item.getEndereco();
      } else {
        CACHE.remove(nomeRede);
      }
    }
    // Busca todos os ips
    final InetAddress[] enderecos = InetAddress.getAllByName(nomeRede);
    final List<Inet4Address> enderecosIpV4 = new ArrayList<Inet4Address>();
    final List<Inet6Address> enderecosIpV6 = new ArrayList<Inet6Address>();
    // Separa os IPs
    for (InetAddress endereco : enderecos) {
      if (endereco instanceof Inet4Address) {
        enderecosIpV4.add((Inet4Address) endereco);
      } else {
        enderecosIpV6.add((Inet6Address) endereco);
      }
    }
    // Busca o melhor IP
    final InetAddress melhorIp = obterMelhorIp(enderecosIpV4, enderecosIpV6, porta);
    enderecosIpV4.clear();
    enderecosIpV6.clear();
    CacheItem item = new CacheItem(melhorIp, System.currentTimeMillis());
    CACHE.put(nomeRede, item);
    return melhorIp;
  }

  /**
   * Obtem o melhor IP usando busca em threads e conexão assincrona.
   * 
   * @param enderecosIpV4
   *          Lista de IPV4
   * @param enderecosIpV6
   *          Lista de IPV6
   * @param porta
   *          Porta de conexão
   * @return O melhor IP
   * @throws IOException
   *           Exceção caso ocorra algum problema.
   */
  private InetAddress obterMelhorIp(List<Inet4Address> enderecosIpV4,
      List<Inet6Address> enderecosIpV6, int porta) throws IOException {
    InetAddress melhor = null;
    Object[] melhorIpV6 = null;
    Object[] melhorIpV4 = null;
    MelhorIp ipv6Tarefa = new MelhorIp(tempoTimeOut, enderecosIpV6, porta);
    MelhorIp ipv4Tarefa = new MelhorIp(tempoTimeOut, enderecosIpV4, porta);
    Future<Object[]> ipv6Futuro = executor.submit(ipv6Tarefa);
    Future<Object[]> ipv4Futuro = executor.submit(ipv4Tarefa);
    // Dispara as tarefas de verificar o melhor IP em paralelo
    try {
      melhorIpV6 = ipv6Futuro.get();
    } catch (InterruptedException exce) {
      System.out.println("Rede IPV6 fora do ar");
    } catch (ExecutionException exce) {
      System.out
          .println("Não foi possível conectar-se IPV6: " + exce.getCause().getLocalizedMessage());
    }
    try {
      melhorIpV4 = ipv4Futuro.get();
    } catch (InterruptedException exce) {
      exce.printStackTrace();
    } catch (ExecutionException excep) {
      System.out
          .println("Não foi possível conectar-se IPV4: " + excep.getCause().getLocalizedMessage());
    }
    // Verifica se existem endereços IPV6
    if (melhorIpV6 != null && melhorIpV6[0] != null) {
      // Verifica tempo de conexão
      if ((Long) melhorIpV4[1] < (Long) melhorIpV6[1]) {
        melhor = (InetAddress) melhorIpV4[0];
      } else {
        melhor = (InetAddress) melhorIpV6[0];
      }
    } else {
      // Caso não existam IPV6 o melhor IPV4 é escolhido.
      melhor = (InetAddress) melhorIpV4[0];
    }
    return melhor;
  }

  /**
   * Simples teste.
   * @param args parâmetros de inicialização do teste
   */
  public static void main(String[] args) {
    try {
      HappyEyeballs.getSingleHappyEyeballs().obterIp("www.google.com.br", 80);
      HappyEyeballs.getSingleHappyEyeballs().obterIp("www.facebook.com.br", 80);
      HappyEyeballs.getSingleHappyEyeballs().obterIp("www.yahoo.com.br", 80);
    } catch (IOException exce) {
      exce.printStackTrace();
    } finally {
      HappyEyeballs.getSingleHappyEyeballs().terminarPoolThread();
    }

  }

}
