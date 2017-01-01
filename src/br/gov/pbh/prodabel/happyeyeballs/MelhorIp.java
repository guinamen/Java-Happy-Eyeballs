package br.gov.pbh.prodabel.happyeyeballs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;

/**
 * Tarefa para coletar os IPs que conectaram no menor tempo.
 * 
 * @author guilherme
 *
 */
public class MelhorIp implements Callable<Amostra> {

  /**
   * Interface de log.
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(MelhorIp.class);
  /**
   * Tempo de expiração da atividade.
   */
  private final transient long tempoTimeOut;
  /**
   * Lista de IPs.
   */
  private final transient List<? extends InetAddress> enderecosIp;
  /**
   * Porta para teste de conectividade.
   */
  private final transient int porta;

  /**
   * Lista de conexões assíncronas.
   */
  private transient List<SocketChannel> canais;
  /**
   * Gerenciador de conexões assíncronas.
   */
  private transient Selector selector;

  /**
   * Construtor simples.
   * 
   * @param tempoTimeOut Tempo de expiração de teste de conexão.
   * @param enderecosIpV Lista de IPs para testar.
   * @param porta Porta para teste de conectividade
   * @throws HappyEyeBallsException caso a lista estiver vazia ou nula, ou porta fora do range
   */
  public MelhorIp(final long tempoTimeOut, final List<? extends InetAddress> enderecosIpV,
      final int porta) throws HappyEyeBallsException {
    super();
    if (enderecosIpV == null || enderecosIpV.isEmpty()) {
      throw new HappyEyeBallsException("Lista de endereços não pode estar vazia.");
    }
    if (porta <= 0 || porta > Short.MAX_VALUE) {
      throw new HappyEyeBallsException("A porta de conexão deve ser válida.");
    }
    this.tempoTimeOut = tempoTimeOut;
    this.enderecosIp = enderecosIpV;
    this.porta = porta;
  }

  /**
   * Fecha todas as conexões.
   * 
   * @throws HappyEyeBallsException exceção de entrada ou saída
   */
  private void fechaConexoes() throws HappyEyeBallsException {
    try {
      for (final SocketChannel canal : canais) {
        if (canal.isConnected()) {
          canal.finishConnect();
        }
        if (canal.isOpen()) {
          canal.close();
        }
      }
      selector.close();
      canais.clear();
    } catch (IOException excep) {
      throw new HappyEyeBallsException("Erro ao fechar os canais", excep);
    }
  }

  /**
   * Inicializa os canais para conecções asincronas.
   * 
   * @throws HappyEyeBallsException caso ocorra algum erro
   */
  private void inicializaCanais() throws HappyEyeBallsException {
    // Cria o selector para realizar as conexões de forma assíncrona.
    try {
      selector = Selector.open();
      canais = new ArrayList<SocketChannel>(enderecosIp.size());
      for (final InetAddress endereco : enderecosIp) {
        // Cria os canais e os registram no selector
        final SocketChannel canal = SocketChannel.open();
        canal.configureBlocking(false);
        canal.register(selector, SelectionKey.OP_CONNECT,
            new Amostra(endereco, System.currentTimeMillis()));
        canal.connect(new InetSocketAddress(endereco, porta));
        canais.add(canal);
      }
    } catch (IOException excep) {
      throw new HappyEyeBallsException("Erro ao inicializar os canais", excep);
    }
  }

  /**
   * Verifica as conecções e retorna o melhor Ip.
   * 
   * @return Amostra com melhor tempo de conecção
   * @throws HappyEyeBallsException caso o tempo de conecção tenha expirado
   */
  private Amostra checaCanais() throws HappyEyeBallsException {
    final SortedSet<Amostra> amostras = new TreeSet<Amostra>();
    try {
      if (selector.select(tempoTimeOut) > 0) {
        final long fim = System.currentTimeMillis();
        final Set<SelectionKey> chaves = selector.selectedKeys();
        for (final SelectionKey chave : chaves) {
          final Amostra dados = (Amostra) chave.attachment();
          dados.setTempoFim(fim);
          amostras.add(dados);
        }
      } else {
        throw new HappyEyeBallsException("Tempo de conexão expirado");
      }
      LOGGER.debug("Amostras: {}", amostras.toString());
      final Amostra ret = amostras.first();
      LOGGER.debug("Amostra de melhor tempo: {}", ret);
      amostras.clear();
      return ret;
    } catch (IOException excep) {
      throw new HappyEyeBallsException("Erro ao checar canais", excep);
    }
  }

  /**
   * Busca o melhor IP.
   * 
   * @return Tupla IP e tempo para conexão.
   * @throws IOException Exceção caso ocorra algum problema.
   */
  private Amostra melhorIp() throws HappyEyeBallsException {
    try {
      inicializaCanais();
      return checaCanais();
    } finally {
      fechaConexoes();
    }
  }

  /**
   * Executa a tarefa de buscar o melhor Ip.
   * 
   * @return o ip com o menor tempo de conecção ou nulo caso a lista esteja vazia
   */
  @Override
  public Amostra call() throws HappyEyeBallsException {
    return melhorIp();
  }

}
