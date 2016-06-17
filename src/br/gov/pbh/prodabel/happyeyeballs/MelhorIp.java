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
      final int porta) {
    super();
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
          new Object[] { endereco, System.currentTimeMillis() });
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
    InetAddress melhor = null;
    long tempoFim = 0;
    if (selector.select(tempoTimeOut) > 0) {
      final long fim = System.currentTimeMillis();
      long melhorTempo = Long.MAX_VALUE;
      final Set<SelectionKey> chaves = selector.selectedKeys();
      for (final SelectionKey chave : chaves) {
        final Object[] dados = (Object[]) chave.attachment();
        final long tempo = fim - (Long) (dados[1]);
        if (tempo < melhorTempo) {
          melhorTempo = tempo;
          melhor = (InetAddress) (dados[0]);
        }
      }
      tempoFim = melhorTempo;
      System.out.println("Ip: " + melhor.getHostAddress() + " em " + tempoFim + " milisegundos");
    } else {
      throw new HappyEyeBallsException("Tempo de conexão expirado");
    }
    return new Amostra(melhor, tempoFim);
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
   */
  @Override
  public Amostra call() throws HappyEyeBallsException {
    return melhorIp();
  }

}
