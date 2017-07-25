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
final class HappyEyeballsImpl implements HappyEyeballs {

  /**
   * Classe de log.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(HappyEyeballsImpl.class);

  /**
   * Semáforo para bloquear as threads para cálculo de tempo de expiração.
   */
  private static final Object MUTEX = new Object();

  /**
   * Cache para armazenar as resoluções dos nomes.
   */
  private static final Cache<String, InetAddress> CACHE;
  /**
   * Tempo de expiração da conexão.
   */
  private static final long TEMPO_EXPIRACAO;

  /**
   * Pool de threads para paralelizar a resolução de nomes.
   */
  private static final ExecutorService EXECUTOR;

  /**
   * Instância única do objeto (Singleton).
   */
  private static HappyEyeballsImpl single;

  /**
   * Construtor estático utilizado para inicializar as variáveis do algoritmo.
   */
  static {
    synchronized (MUTEX) {
      final URL myUrl = ClassLoader.class.getResource(Mensagens.HAPPYEYEBALLS_0);
      final Configuration xmlConfig = new XmlConfiguration(myUrl);
      final CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConfig);
      cacheManager.init();
      CACHE = cacheManager.getCache(Mensagens.HAPPYEYEBALLS_1, String.class, InetAddress.class);
      /* TODO Forma melhor de fornecer os parâmetros de configuração do algoritmo. */
      EXECUTOR = Executors.newFixedThreadPool(4);
      TEMPO_EXPIRACAO = 300L;
    }
  }

  /**
   * Retorna a única instancia da classe (Padrão singleton).
   * 
   * @return Única instância da classe.
   */
  static HappyEyeballsImpl getSingleHappyEyeballs() {
    synchronized (MUTEX) {
      if (single == null) {
        single = new HappyEyeballsImpl();
      }
    }
    return single;
  }

  /**
   * Finaliza o pool de threads e limpa o cache. Executar ao finalizar o programa.
   */
  static void terminarPoolThread() {
    synchronized (MUTEX) {
      EXECUTOR.shutdown();
      single = null;
      CACHE.clear();
    }
  }

  /**
   * Obtem todos os ip de um nome.
   * 
   * @param nome nome do servidor
   * @param enderecosIpV4 lista para adicionar os endereços IPv4
   * @param enderecosIpV6 lista para adicionar os endereços IPv6
   * @throws HappyEyeBallsException caso não encontre o servidor.
   */
  private void obtemIpsPeloNome(final String nome, final List<Inet4Address> enderecosIpV4,
      final List<Inet6Address> enderecosIpV6) throws HappyEyeBallsException {
    // Separa os IPs
    try {
      for (final InetAddress endereco : InetAddress.getAllByName(nome)) {
        LOGGER.debug(Mensagens.HAPPYEYEBALLS_2, nome, endereco);
        if (endereco instanceof Inet4Address) {
          enderecosIpV4.add((Inet4Address) endereco);
        } else {
          enderecosIpV6.add((Inet6Address) endereco);
        }
      }
    } catch (UnknownHostException exp) {
      throw new HappyEyeBallsException(Mensagens.HAPPYEYEBALLS_3, exp);
    }
  }

  /**
   * Obtém o IP segundo o algoritmo Happy Eyeballs.
   * 
   * @param nomeRede Nome do servidor a ser resolvido.
   * @param porta Porta para teste de conexão.
   * @return O IP resolvido ou null caso ocorra algum problema.
   * @throws HappyEyeBallsException Caso ocorra alguma exceção.
   */
  @Override
  public InetAddress obterIp(final String nomeRede, final int porta) throws HappyEyeBallsException {
    final String nome = new StringBuffer(nomeRede).append(':').append(porta).toString();
    final InetAddress enderecoIp;
    if (CACHE.containsKey(nome)) {
      enderecoIp = CACHE.get(nome);
      LOGGER.debug(Mensagens.HAPPYEYEBALLS_4, nomeRede, porta, enderecoIp);
    } else {
      // Busca todos os ips
      final List<Inet4Address> enderecosIpV4 = new LinkedList<Inet4Address>();
      final List<Inet6Address> enderecosIpV6 = new LinkedList<Inet6Address>();
      obtemIpsPeloNome(nomeRede, enderecosIpV4, enderecosIpV6);
      LOGGER.debug(Mensagens.HAPPYEYEBALLS_5, nomeRede, porta, enderecosIpV6);
      LOGGER.debug(Mensagens.HAPPYEYEBALLS_6, nomeRede, porta, enderecosIpV4);
      // Busca o melhor tempo de conecção
      final Amostra amostra = obterMelhorIp(enderecosIpV4, enderecosIpV6, porta);
      if (amostra == null) {
        enderecoIp = null;
        LOGGER.debug(Mensagens.HAPPYEYEBALLS_7);
      } else {
        enderecoIp = amostra.getEnderecoIp();
        LOGGER.debug(Mensagens.HAPPYEYEBALLS_8, nomeRede, porta, enderecoIp);
        CACHE.put(nome, enderecoIp);
      }
    }
    return enderecoIp;
  }

  /**
   * Cria a atividade para buscar os tempo de conexão.
   * 
   * @param enderecosIp Lista de endereços IP
   * @param porta porta do serviço
   * @return tarefa ser executada ou nulo caso não consiga
   * @throws HappyEyeBallsException caso ocorra algum problema.
   */
  private Future<Amostra> criaAtividade(final List<? extends InetAddress> enderecosIp,
      final int porta) throws HappyEyeBallsException {
    if (enderecosIp == null || enderecosIp.isEmpty()) {
      throw new IllegalArgumentException(Mensagens.HAPPYEYEBALLS_9);
    } else {
      final MelhorIp melhorIp = new MelhorIp(TEMPO_EXPIRACAO, enderecosIp, porta);
      return EXECUTOR.submit(melhorIp);
    }
  }

  /**
   * Executa a tarefa e retorna a Amosta.
   * 
   * @param tarefa tarefa para buscar o tempo de execução
   * @return amostra do tempo de conexão
   * @throws HappyEyeBallsException caso ocorra algum problema.
   */
  private Amostra executarTarefa(final Future<Amostra> tarefa) throws HappyEyeBallsException {
    try {
      if (tarefa == null) {
        throw new IllegalArgumentException(Mensagens.HAPPYEYEBALLS_10);
      } else {
        return tarefa.get();
      }
    } catch (InterruptedException exce) {
      throw new HappyEyeBallsException(Mensagens.HAPPYEYEBALLS_11, exce);
    } catch (ExecutionException exce) {
      throw new HappyEyeBallsException(Mensagens.HAPPYEYEBALLS_12, exce);
    }
  }

  /**
   * Obtém o melhor IP usando busca em threads e conexão assíncrona.
   * 
   * @param enderecosIpV4 Lista de IPV4
   * @param enderecosIpV6 Lista de IPV6
   * @param porta Porta de conexão
   * @return O melhor IP
   * @throws HappyEyeBallsException Exceção caso ocorra algum problema.
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
    if (ipv6Futuro != null) {
      melhorIpV6 = executarTarefa(ipv6Futuro);
    }
    if (ipv4Futuro != null) {
      melhorIpV4 = executarTarefa(ipv4Futuro);
    }
    // Verifica se existem endereços IPV6
    Amostra melhor;
    if (melhorIpV6 == null) {
      melhor = melhorIpV4;
    } else {
      melhor = melhorIpV6.compareTo(melhorIpV4) < 0 ? melhorIpV6 : melhorIpV4;
    }
    return melhor;
  }
}
