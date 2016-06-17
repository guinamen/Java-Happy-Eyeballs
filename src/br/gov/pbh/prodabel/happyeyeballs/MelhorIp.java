package br.gov.pbh.prodabel.happyeyeballs;

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
   * Tempo de expiração da atividade.
   */
  private transient final long tempoTimeOut;
  /**
   * Lista de IPs.
   */
  private transient final List<? extends InetAddress> enderecosIp;
  /**
   * Porta para teste de conectividade.
   */
  private transient final int porta;

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
   * @param tempoTimeOut
   *          Tempo de expiração de teste de conexão.
   * @param enderecosIpV
   *          Lista de IPs para testar.
   * @param porta
   *          Porta para teste de conectividade.
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
   * @throws IOException
   *           exceção de entrada ou saída
   */
  private void fechaConexoes() throws HappyEyeBallsException {
    try {
      for (final SocketChannel canal : canais) {
        canal.finishConnect();
        canal.close();
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
   * @throws IOException
   *           exceção de interrupção
   */
  private void inicializaCanais() throws IOException {
    // Cria o selector para realizar as conexões de forma assíncrona.
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
  }

  /**
   * Verifica as conecções e retorna o melhor Ip.
   * 
   * @return
   * @throws IOException
   * @throws HappyEyeBallsException
   */
  private Amostra checaCanais() throws IOException, HappyEyeBallsException {
    final SortedSet<Amostra> amostras = new TreeSet<Amostra>();
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
    System.out.println(amostras);
    System.out.println(amostras.first());
    return amostras.first();
  }

  /**
   * Busca o melhor IP.
   * 
   * @return Tupla IP e tempo para conexão.
   * @throws IOException
   *           Exceção caso ocorra algum problema.
   */
  private Amostra melhorIp() throws HappyEyeBallsException {
    try {
      inicializaCanais();
      return checaCanais();
    } catch (IOException excep) {
      throw new HappyEyeBallsException("Erro ao verificar os IPs.", excep);
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
