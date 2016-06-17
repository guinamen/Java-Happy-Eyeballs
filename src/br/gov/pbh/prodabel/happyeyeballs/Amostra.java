package br.gov.pbh.prodabel.happyeyeballs;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Amostra de tempo de conecção.
 * 
 * @author guilherme
 *
 */
public class Amostra implements Comparable<Amostra>, Serializable {

  /**
   * Número serial.
   */
  private static final long serialVersionUID = 1130348813518387677L;

  /**
   * Endereço Ip.
   */
  private final InetAddress enderecoIp;

  /**
   * Tempo para conectar ao endereço em milisegundos.
   */
  private final long tempoConeccao;

  /**
   * Construtor.
   * 
   * @param ip
   *          endereço ip
   * @param tempoConeccao
   *          tempo para se conectar ao ip
   */
  public Amostra(final InetAddress enderecoIp, final long tempoConeccao) {
    super();
    this.enderecoIp = enderecoIp;
    this.tempoConeccao = tempoConeccao;
  }

  /**
   * Ip da amostra.
   * 
   * @return endereço ip da amaostra
   */
  public InetAddress getEnderecoIp() {
    return enderecoIp;
  }

  /**
   * Tempo de conecção.
   * 
   * @return tempo para se conectar ao endereço em milisegundos.
   */
  public long getTempoConeccao() {
    return tempoConeccao;
  }

  /**
   * Compara duas amostras.
   * 
   * @param outro
   *          a amostra para ser comparada
   * @return número negativo, zero, ou positivo sendo esse objeto menor que, igual a, ou maior do
   *         que o outro objeto
   */
  @Override
  public final int compareTo(final Amostra outro) {
    return outro == null ? 1 : Long.compare(tempoConeccao, outro.tempoConeccao);
  }

}
