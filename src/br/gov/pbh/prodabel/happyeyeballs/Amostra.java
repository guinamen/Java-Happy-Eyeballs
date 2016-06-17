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
  private final long tempoInicio;

  /**
   * Tempo em que terminou a conecção.
   */
  private long tempoFim;

  /**
   * Construtor.
   * 
   * @param ip
   *          endereço ip
   * @param tempoInicio
   *          tempo em milisegundos do início para conectar.
   */
  public Amostra(final InetAddress enderecoIp, final long tempoInicio) {
    super();
    this.enderecoIp = enderecoIp;
    this.tempoInicio = tempoInicio;
  }

  /**
   * Ip da amostra.
   * 
   * @return endereço ip da amaostra
   */
  public InetAddress getEnderecoIp() {
    return enderecoIp;
  }

  public long getTempoFim() {
    return tempoFim;
  }

  public void setTempoFim(long tempoFim) {
    this.tempoFim = tempoFim;
  }

  /**
   * Tempo de conecção.
   * 
   * @return tempo para se conectar ao endereço em milisegundos.
   */
  public long getTempoInicio() {
    return tempoInicio;
  }

  /**
   * Retorna o tempo total para conectar ao servidor.
   * 
   * @return tempo em milisegundos
   */
  public long getTempoTotal() {
    return tempoFim == 0 ? Long.MAX_VALUE : tempoFim - tempoInicio;
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
    return outro == null ? 1 : Long.compare(getTempoTotal(), outro.getTempoTotal());
  }

}
