package br.gov.pbh.prodabel.happyeyeballs;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Arrays;

/**
 * Amostra de tempo de conexão.
 * 
 * @author Guilherme
 * @version 0.1
 */
class Amostra implements Comparable<Amostra>, Serializable {

  /**
   * Número serial.
   */
  private static final long serialVersionUID = 1130348813518387677L;

  /**
   * Endereço IP.
   */
  private final InetAddress enderecoIp;

  /**
   * Tempo para conectar ao endereço em milissegundos.
   */
  private final long tempoInicio;

  /**
   * Tempo em que terminou a conexão.
   */
  private long tempoFim;

  /**
   * Construtor.
   * 
   * @param enderecoIp endereço IP
   * @param tempoInicio tempo em milissegundos do inicio para conectar.
   */
  public Amostra(final InetAddress enderecoIp, final long tempoInicio) {
    super();
    this.enderecoIp = enderecoIp;
    this.tempoInicio = tempoInicio;
  }

  /**
   * IP da amostra.
   * 
   * @return endereço IP da amostra
   */
  public InetAddress getEnderecoIp() {
    return enderecoIp;
  }

  /**
   * Retorna o tempo em que a conexão terminou.
   * 
   * @return tempo em milissegundos
   */
  public long getTempoFim() {
    return tempoFim;
  }

  /**
   * Define o tempo em que a conexão terminou.
   * 
   * @param tempoFim tempo em milissegundos
   */
  public void setTempoFim(final long tempoFim) {
    this.tempoFim = tempoFim;
  }

  /**
   * Tempo de conexão.
   * 
   * @return tempo para se conectar ao endereço em milissegundos.
   */
  public long getTempoInicio() {
    return tempoInicio;
  }

  /**
   * Retorna o tempo total para conectar ao servidor.
   * 
   * @return tempo em milissegundos
   */
  public long getTempoTotal() {
    return tempoFim == 0 ? Long.MAX_VALUE : tempoFim - tempoInicio;
  }

  /**
   * Compara duas amostras.
   * 
   * @param outro a amostra para ser comparada
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
    final StringBuffer buffer = new StringBuffer(Mensagens.HAPPYEYEBALLS_13).append(enderecoIp)
        .append(Mensagens.HAPPYEYEBALLS_14).append(getTempoTotal())
        .append(Mensagens.HAPPYEYEBALLS_15);
    return buffer.substring(0);
  }

  /**
   * Retorna o hash code deste objeto.
   * 
   * @return valor de hash desse objeto.
   */
  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {super.hashCode(), enderecoIp, tempoFim, tempoInicio});
  }

  /**
   * Verifica se esse objeto é igual ao parâmetro.
   * 
   * @param objeto outro objeto
   * @return verdadeiro caso os objetos sejam iguais.
   */
  @Override
  public boolean equals(final Object objeto) {
    boolean igual = false;
    if (this == objeto) {
      igual = true;
    } else if (objeto != null && getClass().equals(objeto.getClass())) {
      final Amostra outro = (Amostra) objeto;
      if (enderecoIp != null) {
        igual = enderecoIp.equals(outro.enderecoIp) && tempoInicio == outro.tempoInicio
            && tempoFim == outro.tempoFim;
      }
    }
    return igual;
  }

}
