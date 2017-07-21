/*
 * 
 */
package br.gov.pbh.prodabel.happyeyeballs;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Amostra de tempo de conecção.
 * 
 * @author guilherme
 * @version 0.1
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
    final StringBuffer buffer = new StringBuffer("Amostra [enderecoIp=");
    buffer.append(enderecoIp);
    buffer.append(", ");
    buffer.append(getTempoTotal());
    buffer.append("]");
    return buffer.substring(0);
  }

  /**
   * Retorna o hash code deste objeto.
   * 
   * @return valor de hash desse objeto.
   */
  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] { super.hashCode(), enderecoIp, tempoFim, tempoInicio });
  }

  /**
   * Verfica se esse objeto é igual ao parâmetro.
   * 
   * @param objeto
   *          outro objeto
   * @return verdadeiro caso os objetos sejam iguais.
   */
  @Override
  public boolean equals(final Object objeto) {
    final boolean igual;
    if (this == objeto) {
      igual = true;
    } else if (objeto instanceof Amostra) {
      final Amostra outro = (Amostra) objeto;
      if (enderecoIp == null) {
        igual = false;
      } else {
        igual = enderecoIp.equals(outro.enderecoIp) && tempoInicio == outro.tempoInicio
            && tempoFim == outro.tempoFim;
      }
    } else {
      igual = false;
    }
    return igual;
  }

}
