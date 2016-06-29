package br.gov.pbh.prodabel.happyeyeballs;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;

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
   * @param enderecoIp
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

  /**
   * Retorna o tempo em que a conecção terminou.
   * 
   * @return tempo em milisegundos
   */
  public long getTempoFim() {
    return tempoFim;
  }

  /**
   * Define o tempo em que a conecção terminou.
   * 
   * @param tempoFim
   *          tempo em milisegundos
   */
  public void setTempoFim(final long tempoFim) {
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
   * @return número negativo, zero, ou positivo caso seja menor, igual, ou maior
   */
  @Override
  public final int compareTo(final Amostra outro) {
    return outro == null ? 1 : Long.compare(getTempoTotal(), outro.getTempoTotal());
  }

  /**
   * Retorna a representação em texto do objeto.
   * 
   * @return string que representa o objeto
   */
  @Override
  public String toString() {
    return "Amostra [enderecoIp=" + enderecoIp + ", " + getTempoTotal() + "]";
  }

  /**
   * Retorna o hash code deste objeto.
   * 
   * @return valor de hash desse objeto.
   */
  @Override
  public int hashCode() {
    return Arrays
        .hashCode(new Object[] { super.hashCode(), enderecoIp, this.tempoFim, this.tempoInicio });
  }

  /**
   * Verfica se esse objeto é igual ao parâmetro.
   * 
   * @param other
   *          outro objeto
   * @return verdadeiro caso os objetos sejam iguais.
   */
  @Override
  public boolean equals(Object objeto) {
    boolean igual = false;
    if (objeto == this) {
      igual = true;
    } else if (objeto != null && objeto instanceof Amostra) {
      Amostra outro = (Amostra) objeto;
      boolean enderecoIgual;
      if (enderecoIp == null) {
        enderecoIgual = enderecoIp == outro.enderecoIp;
      } else {
        enderecoIgual = enderecoIp.equals(outro.enderecoIp);
      }
      igual = enderecoIgual & tempoFim == outro.tempoFim & tempoInicio == outro.tempoInicio;
    }
    return igual;
  }

}
