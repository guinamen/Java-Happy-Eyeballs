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
public class MelhorIp implements Callable<Object[]> {

  /**
   * Tempo de expiração da atividade.
   */
  private long tempoTimeOut;
  /**
   * Lista de IPs.
   */
  private List<? extends InetAddress> enderecosIp;
  /**
   * Porta para teste de conectividade.
   */
  private int porta;

  /**
   * Lista de conexões assíncronas.
   */
  private List<SocketChannel> canais;
  /**
   * Gerenciador de conexões assíncronas.
   */
  Selector selector;

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
  public MelhorIp(long tempoTimeOut, List<? extends InetAddress> enderecosIpV, int porta) {
    super();
    this.tempoTimeOut = tempoTimeOut;
    this.enderecosIp = enderecosIpV;
    this.porta = porta;
  }

  /**
   * Fecha todas as conexões.
   * 
   * @throws IOException exceção de entrada ou saída 
   */
  private void fechaConexoes() throws IOException {
    for (SocketChannel canal : canais) {
      canal.finishConnect();
      canal.close();
    }
    selector.close();
    canais.clear();
  }

  /**
   * Busca o melhor IP.
   * 
   * @return Tupla IP e tempo para conexão.
   * @throws IOException
   *           Exceção caso ocorra algum problema.
   */
  private Object[] melhorIp() throws IOException {
    InetAddress melhor = null;
    long tempoFim = 0;
    // Cria o selector para realizar as conexões de forma assíncrona.
    selector = Selector.open();
    canais = new ArrayList<SocketChannel>(enderecosIp.size());
    for (InetAddress endereco : enderecosIp) {
      // Cria os canais e os registram no selector
      SocketChannel canal = SocketChannel.open();
      canal.configureBlocking(false);
      Long inicio = System.currentTimeMillis();
      canal.connect(new InetSocketAddress(endereco, porta));
      canal.register(selector, SelectionKey.OP_CONNECT, new Object[] { endereco, inicio });
      canais.add(canal);
    }
    // Espera uma ou mais conexões ocorrerem ou o tempo expirar.
    if (selector.select(tempoTimeOut) > 0) {
      long fim = System.currentTimeMillis();
      long melhorTempo = Long.MAX_VALUE;
      Set<SelectionKey> chaves = selector.selectedKeys();
      for (SelectionKey chave : chaves) {
        Object[] dados = (Object[]) chave.attachment();
        long tempo = fim - (Long) (dados[1]);
        if (tempo < melhorTempo) {
          melhorTempo = tempo;
          melhor = (InetAddress) (dados[0]);
        }
      }
      tempoFim = melhorTempo;
      System.out.println("Ip: " + melhor.getHostAddress() + " em " + tempoFim + " milisegundos");
    } else {
      fechaConexoes();
      throw new IOException("Tempo de conexão expirado");
    }
    fechaConexoes();
    Object[] ret = new Object[2];
    ret[0] = melhor;
    ret[1] = tempoFim;
    return ret;
  }

  @Override
  public Object[] call() throws Exception {
    return melhorIp();
  }

}
