package br.gov.pbh.prodabel.happyeyeballs;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
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
   * Classe de log.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(HappyEyeballs.class);

  /**
   * Semáforo para bloquear as threads para cálculo de tempo de expiração.
   */
  private static final Object MUTEX = new Object();
  /**
   * Instância única do objeto (Singleton).
   */
  private static HappyEyeballs single;
  /**
   * Cache para armazenar as resoluções dos nomes.
   */
  private final Cache<String, InetAddress> cache;
  /**
   * Tempo de expiração da conecção.
   */
  private final transient long coneccaoExpiracao;

  /**
   * Pool de threads para paralelizar a resolução de nomes.
   */
  private final transient ExecutorService executor;

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
  private HappyEyeballs(final long coneccaoExpiracao, final int numThread,
      final Cache<String, InetAddress> cache) {
    super();
    this.coneccaoExpiracao = coneccaoExpiracao;
    this.cache = cache;
    this.executor = Executors.newFixedThreadPool(numThread);
  }

  /**
   * Retorna a única instancia da classe (Padrão singleton).
   * 
   * @return Única instância da classe.
   */
  public static HappyEyeballs getSingleHappyEyeballs() {
    // TODO Forma melhor de fornecer os parâmetros de configuração do algoritmo.
    synchronized (MUTEX) {
      if (single == null) {
        final URL myUrl = ClassLoader.class.getResource("/cache.xml");
        final Configuration xmlConfig = new XmlConfiguration(myUrl);
        final CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
        cacheManager.init();
        single = new HappyEyeballs(300L, 4,
            cacheManager.getCache("happyeyeballs", String.class, InetAddress.class));
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
    cache.clear();
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
   * @throws HappyEyeBallsException
   *           caso não encontre o servidor.
   */
  private void obtemIpsPeloNome(final String nome, final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6) throws HappyEyeBallsException {
    // Separa os IPs
    try {
      for (final InetAddress endereco : InetAddress.getAllByName(nome)) {
        LOGGER.debug("{} -> {}", nome, endereco);
        if (endereco instanceof Inet4Address) {
          enderecosIpV4.add((Inet4Address) endereco);
        } else {
          enderecosIpV6.add((Inet6Address) endereco);
        }
      }
    } catch (UnknownHostException exp) {
      throw new HappyEyeBallsException("Erro ao buscar nome", exp);
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
   * @throws HappyEyeBallsException
   *           Caso ocorra alguma exceção.
   */
  public InetAddress obterIp(final String nomeRede, final int porta) throws HappyEyeBallsException {
    final String nome = new StringBuffer(nomeRede).append(":").append(porta).toString();
    final InetAddress enderecoIp;
    if (cache.containsKey(nome)) {
      enderecoIp = cache.get(nome);
      LOGGER.debug("Menor tempo de conecçao no cache -> {}:{} {}", nomeRede, porta, enderecoIp);
    } else {
      // Busca todos os ips
      final List<Inet4Address> enderecosIpV4 = new LinkedList<Inet4Address>();
      final List<Inet6Address> enderecosIpV6 = new LinkedList<Inet6Address>();
      obtemIpsPeloNome(nomeRede, enderecosIpV4, enderecosIpV6);
      LOGGER.debug("IPV6 -> {}:{} {}", nomeRede, porta, enderecosIpV6);
      LOGGER.debug("IPV4 -> {}:{} {}", nomeRede, porta, enderecosIpV4);
      // Busca o melhor tempo de conecção
      final Amostra amostra = obterMelhorIp(enderecosIpV4, enderecosIpV6, porta);
      if (amostra == null) {
        enderecoIp = null;
        LOGGER.debug("Menor tempo não encontrado");
      } else {
        enderecoIp = amostra.getEnderecoIp();
        LOGGER.debug("Menor tempo de conecçao -> {}:{} {}", nomeRede, porta, enderecoIp);
        cache.put(nome, enderecoIp);
      }
    }
    return enderecoIp;
  }

  /**
   * Cria a atividade para buscar os tempo de conecção.
   * 
   * @param enderecosIp
   *          Lista de endereços IP
   * @param porta
   *          porta do serviço
   * @return tarefa ser executada ou nulo caso não consiga
   */
  private Future<Amostra> criaAtividade(final List<? extends InetAddress> enderecosIp,
      final int porta) throws HappyEyeBallsException {
    if (enderecosIp == null || enderecosIp.isEmpty()) {
      throw new IllegalArgumentException("Lista de endereço inválida.");
    } else {
      final MelhorIp melhorIp = new MelhorIp(coneccaoExpiracao, enderecosIp, porta);
      return executor.submit(melhorIp);
    }
  }

  /**
   * Executa a tarefa e retorna a Amosta.
   * 
   * @param tarefa
   *          tarefa para buscar o tempo de execução
   * @return amostra do tempo de conecção
   */
  private Amostra executarTarefa(final Future<Amostra> tarefa) throws HappyEyeBallsException {
    try {
      if (tarefa == null) {
        throw new IllegalArgumentException("Tarefa nula");
      } else {
        return tarefa.get();
      }
    } catch (InterruptedException exce) {
      throw new HappyEyeBallsException("Thread interrmpida", exce);
    } catch (ExecutionException exce) {
      throw new HappyEyeBallsException("Erro de execução da thread", exce);
    }
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
   * @throws HappyEyeBallsException
   *           Exceção caso ocorra algum problema.
   */
  private Amostra obterMelhorIp(final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6, final int porta) throws HappyEyeBallsException {

    Future<Amostra> ipv6Futuro = null;
    Future<Amostra> ipv4Futuro = null;
    Amostra melhorIpV6 = null;
    Amostra melhorIpV4 = null;

    if (enderecosIpV6 != null && !enderecosIpV6.isEmpty()) {
      ipv6Futuro = criaAtividade(enderecosIpV6, porta);
    }
    if (enderecosIpV4 != null && !enderecosIpV4.isEmpty()) {
      ipv4Futuro = criaAtividade(enderecosIpV4, porta);
    }
    if (ipv6Futuro != null)
      melhorIpV6 = executarTarefa(ipv6Futuro);
    if (ipv4Futuro != null)
      melhorIpV4 = executarTarefa(ipv4Futuro);
    // Verifica se existem endereços IPV6
    Amostra melhor;
    if (melhorIpV6 == null) {
      melhor = melhorIpV4;
    } else {
      melhor = melhorIpV6.compareTo(melhorIpV4) >= 0 ? melhorIpV6 : melhorIpV4;
    }
    return melhor;
  }
}
