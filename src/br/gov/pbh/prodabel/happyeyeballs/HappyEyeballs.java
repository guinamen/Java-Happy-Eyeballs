package br.gov.pbh.prodabel.happyeyeballs;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import br.gov.pbh.prodabel.happyeyeballs.cache.Cache;
import br.gov.pbh.prodabel.happyeyeballs.cache.CacheItem;
import br.gov.pbh.prodabel.happyeyeballs.cache.ItemNaoEncontrado;

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
  private static Cache<InetAddress> cache;
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
   * Tempo de expiração de conecção.
   */
  private final long coneccaoExpiracao;

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
  private HappyEyeballs(final long cacheExpiracao, final long coneccaoExpiracao,
      final int numThread) {
    super();
    synchronized (MUTEX) {
      if (cache == null) {
        cache = new Cache<InetAddress>(cacheExpiracao);
      }
    }
    this.coneccaoExpiracao = coneccaoExpiracao;
    this.executor = Executors.newFixedThreadPool(numThread);
  }

  /**
   * Retorna o tempo de expiração da resolução do nome.
   * 
   * @return tempo em milisegundos
   */
  public long getCacheExpiracao() {
    return cache.getTempoExpiracao();
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
        single = new HappyEyeballs(300L, 3000L, 4);
      }
    }
    return single;
  }

  /**
   * Finaliza o pool de threads e limpa o cache. Executar ao finalizar o programa.
   */
  public void terminarPoolThread() {
    executor.shutdown();
    synchronized (MUTEX) {
      single = null;
    }
    cache.limpa();
  }

  /**
   * Obtem todos os ip de um nome.
   * 
   * @param nome
   *          nome do servidor
   * @param enderecosIpV4
   *          lista para adicionar os endereçõs IPv4
   * @param enderecosIpV6
   *          lista para adicionar os endereçõs IPv6
   * @throws UnknownHostException
   *           caso não encontre o servidor.
   */
  private void obtemIpsPeloNome(final String nome, final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6) throws UnknownHostException {
    // Separa os IPs
    for (final InetAddress endereco : InetAddress.getAllByName(nome)) {
      if (endereco instanceof Inet4Address) {
        enderecosIpV4.add((Inet4Address) endereco);
      } else {
        enderecosIpV6.add((Inet6Address) endereco);
      }
    }
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
    final String nome = new StringBuffer(nomeRede).append(":").append(porta).toString();
    InetAddress enderecoIp;
    try {
      enderecoIp = cache.obtem(nome);
    } catch (ItemNaoEncontrado e) {
      // Busca todos os ips
      final List<Inet4Address> enderecosIpV4 = new LinkedList<Inet4Address>();
      final List<Inet6Address> enderecosIpV6 = new LinkedList<Inet6Address>();
      obtemIpsPeloNome(nomeRede, enderecosIpV4, enderecosIpV6);
      // Busca o melhor tempo de conecção
      enderecoIp = obterMelhorIp(enderecosIpV4, enderecosIpV6, porta);
      cache.adiciona(nome, new CacheItem<InetAddress>(enderecoIp, System.currentTimeMillis()));
    }
    return enderecoIp;
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
    Future<Amostra> ipv6Futuro = null;
    Future<Amostra> ipv4Futuro = null;
    if (enderecosIpV6 != null && !enderecosIpV6.isEmpty()) {
      try {
        final MelhorIp ipv6Tarefa = new MelhorIp(coneccaoExpiracao, enderecosIpV6, porta);
        ipv6Futuro = executor.submit(ipv6Tarefa);
      } catch (HappyEyeBallsException e) {
        // TODO implantar log
        e.printStackTrace();
      }
    }

    if (enderecosIpV4 != null && !enderecosIpV4.isEmpty()) {
      try {
        final MelhorIp ipv4Tarefa = new MelhorIp(coneccaoExpiracao, enderecosIpV4, porta);
        ipv4Futuro = executor.submit(ipv4Tarefa);
      } catch (HappyEyeBallsException e) {
        // TODO implantar log
        e.printStackTrace();
      }
    }

    // Dispara as tarefas de verificar o melhor IP em paralelo
    if (ipv6Futuro != null) {
      try {
        melhorIpV6 = ipv6Futuro.get();
      } catch (InterruptedException exce) {
        System.out.println("Rede IPV6 fora do ar");
      } catch (ExecutionException exce) {
        // TODO implantar log
        System.out
            .println("Não foi possível conectar-se IPV6: " + exce.getCause().getLocalizedMessage());
      }
    }
    if (ipv4Futuro != null) {
      try {
        melhorIpV4 = ipv4Futuro.get();
      } catch (InterruptedException exce) {
        exce.printStackTrace();
      } catch (ExecutionException excep) {
        // TODO implantar log
        System.out.println(
            "Não foi possível conectar-se IPV4: " + excep.getCause().getLocalizedMessage());
      }
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
    HappyEyeballs singleton = HappyEyeballs.getSingleHappyEyeballs();
    try {
      singleton.obterIp("www.google.com.br", 80);
      singleton.obterIp("www.facebook.com.br", 80);
      singleton.obterIp("www.yahoo.com.br", 80);
    } catch (IOException exce) {
      exce.printStackTrace();
    } finally {
      singleton.terminarPoolThread();
    }

  }

}
