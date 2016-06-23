package br.gov.pbh.prodabel.happyeyeballs;

import java.io.IOException;
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

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.xml.XmlConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
   * Cache para armazenar as resoluções dos nomes.
   */
  private Cache<String, InetAddress> cache;
  /**
   * Semáforo para bloquear as threads para cálculo de tempo de expiração.
   */
  private static final Object MUTEX = new Object();
  /**
   * Instância única do objeto (Singleton).
   */
  private static HappyEyeballs single;

  /**
   * Tempo de expiração da conecção.
   */
  private final long coneccaoExpiracao;

  /**
   * Pool de threads para paralelizar a resolução de nomes.
   */
  private transient final ExecutorService executor;

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
    final URL myUrl = ClassLoader.class.getResource("/cache.xml");
    final Configuration xmlConfig = new XmlConfiguration(myUrl);
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
    cacheManager.init();
    synchronized (MUTEX) {
      if (single == null) {
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
    InetAddress enderecoIp = cache.get(nome);
    if (enderecoIp == null) {
      // Busca todos os ips
      final List<Inet4Address> enderecosIpV4 = new LinkedList<Inet4Address>();
      final List<Inet6Address> enderecosIpV6 = new LinkedList<Inet6Address>();
      obtemIpsPeloNome(nomeRede, enderecosIpV4, enderecosIpV6);
      // Busca o melhor tempo de conecção
      Amostra amostra = obterMelhorIp(enderecosIpV4, enderecosIpV6, porta);
      if (amostra != null) {
        enderecoIp = amostra.getEnderecoIp();
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
      final int porta) {
    Future<Amostra> tarefa = null;
    if (enderecosIp != null && !enderecosIp.isEmpty()) {
      try {
        final MelhorIp melhorIp = new MelhorIp(coneccaoExpiracao, enderecosIp, porta);
        tarefa = executor.submit(melhorIp);
      } catch (HappyEyeBallsException exec) {
        LOGGER.error("Erro ao obter ip", exec);
      }
    }
    return tarefa;
  }

  /**
   * Executa a tarefa e retorna a Amosta.
   * 
   * @param tarefa
   *          tarefa para buscar o tempo de execução
   * @return amostra do tempo de conecção
   */
  private Amostra executarTarefa(final Future<Amostra> tarefa) {
    Amostra ret = null;
    try {
      if (tarefa != null) {
        ret = tarefa.get();
      }
    } catch (InterruptedException exce) {
      LOGGER.error("Thread interrmpida", exce);
    } catch (ExecutionException exce) {
      LOGGER.error("Erro de execução da thread", exce);
    }
    return ret;
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
  private Amostra obterMelhorIp(final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6, final int porta) throws IOException {
    
    final Future<Amostra> ipv6Futuro = criaAtividade(enderecosIpV6, porta);
    final Future<Amostra> ipv4Futuro = criaAtividade(enderecosIpV4, porta);
    final Amostra melhorIpV6 = executarTarefa(ipv6Futuro);
    final Amostra melhorIpV4 = executarTarefa(ipv4Futuro);
    // Verifica se existem endereços IPV6
    Amostra melhor;
    if (melhorIpV6 != null && melhorIpV4 != null) {
      melhor = melhorIpV4.compareTo(melhorIpV6) >= 0 ? melhorIpV6 : melhorIpV4;
    } else if (melhorIpV6 == null) {
      melhor = melhorIpV4;
    } else {
      melhor = melhorIpV4;
    } 
    return melhor;
  }
}
