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
  private static HappyEyeballs single;

  /**
   * Pool de threads para paralelizar a resolução de nomes.
   */
  private transient final ExecutorService executor;
  /**
   * Tempo de expiração da resolução do nome.
   */
  private final long dnsExpiracao;
  /**
   * Tempo de time out da conexão.
   */
  private final long coneccaoExpiracao;

  /**
   * Número de threads.
   */
  private final int numThread;

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
    this.dnsExpiracao = tempoExpiracao;
    this.coneccaoExpiracao = tempoTimeOut;
    this.numThread = numThread;
    this.executor = Executors.newFixedThreadPool(this.numThread);
  }

  /**
   * Retorna o tempo de expiração da resolução do nome.
   * 
   * @return tempo em milisegundos
   */
  public long getDnsExpiracao() {
    return dnsExpiracao;
  }

  /**
   * Retorna o tempo expiração da conecção.
   * 
   * @return tempo em milisegundos
   */
  public long getConeccaoExpiracao() {
    return coneccaoExpiracao;
  }

  /**
   * Número de threads.
   * 
   * @return número de threads
   */
  public int getNumThread() {
    return numThread;
  }

  /**
   * Retorna a única instancia da classe (Padrão singleton).
   * 
   * @return Única instância da classe.
   */
  public static HappyEyeballs getSingleHappyEyeballs() {
    // TODO Forma melhor de fornecer os parâmetros de configuração do
    // algoritmo.
    synchronized (MUTEX) {
      if (single == null) {
        single = new HappyEyeballs(5000L, 300L, 4);
      }
    }
    return single;
  }

  /**
   * Finaliza o pool de threads e limpa o cache para evitar memory leak. Executar ao finalizar o
   * programa.
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
      long tempoDecorrido;
      // Verifica se o tempo expirou
      synchronized (MUTEX) {
        tempoDecorrido = System.currentTimeMillis() - item.getTempoAcesso();
      }
      if (tempoDecorrido < dnsExpiracao) {
        return item.getEndereco();
      } else {
        CACHE.remove(nomeRede);
      }
    }
    // Busca todos os ips
    final InetAddress[] enderecos = InetAddress.getAllByName(nomeRede);
    final List<Inet4Address> enderecosIpV4 = new ArrayList<Inet4Address>(enderecos.length);
    final List<Inet6Address> enderecosIpV6 = new ArrayList<Inet6Address>(enderecos.length);
    // Separa os IPs
    for (final InetAddress endereco : enderecos) {
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
    final CacheItem item = new CacheItem(melhorIp, System.currentTimeMillis());
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
  private InetAddress obterMelhorIp(final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6, final int porta) throws IOException {
    InetAddress melhor = null;
    Amostra melhorIpV6 = null;
    Amostra melhorIpV4 = null;
    final MelhorIp ipv6Tarefa = new MelhorIp(coneccaoExpiracao, enderecosIpV6, porta);
    final MelhorIp ipv4Tarefa = new MelhorIp(coneccaoExpiracao, enderecosIpV4, porta);
    final Future<Amostra> ipv6Futuro = executor.submit(ipv6Tarefa);
    final Future<Amostra> ipv4Futuro = executor.submit(ipv4Tarefa);
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
    if (melhorIpV6 == null) {
      if (melhorIpV4 != null) {
        melhor = melhorIpV4.getEnderecoIp();
      }
    } else {
      if (melhorIpV4 == null) {
        melhor = melhorIpV6.getEnderecoIp();
      } else {
        melhor = melhorIpV4.compareTo(melhorIpV6) >= 0
            ? melhorIpV6.getEnderecoIp()
            : melhorIpV4.getEnderecoIp();
      }
    }
    return melhor;
  }

  /**
   * Simples teste.
   * 
   * @param args
   *          parâmetros de inicialização do teste
   */
  public static void main(final String... args) {
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
