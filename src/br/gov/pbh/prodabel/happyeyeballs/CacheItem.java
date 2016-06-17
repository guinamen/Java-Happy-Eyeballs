package br.gov.pbh.prodabel.happyeyeballs;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Item do cache para o algoritmo Happy Eyeballs.
 * 
 * @author guilherme
 *
 */
public class CacheItem implements Serializable {
  /**
   * Número serial.
   */
  private static final long serialVersionUID = 2466334712384701929L;
  /**
   * Endereço.
   */
  private InetAddress endereco;
  /**
   * Tempo em que o endereço foi adicionado no cache.
   */
  private long tempoAcesso;

  /**
   * Construtor simples.
   * 
   * @param endereco
   *          Endereço
   * @param tempoAcesso
   *          Tempo em que o item foi adicionado.
   */
  public CacheItem(final InetAddress endereco, final long tempoAcesso) {
    super();
    this.endereco = endereco;
    this.tempoAcesso = tempoAcesso;
  }

  /**
   * Retorna o endereço.
   * 
   * @return endereço ip
   */
  public InetAddress getEndereco() {
    return endereco;
  }

  /**
   * Retorna o tempo de acesso.
   * 
   * @return tempo de acesso
   */
  public Long getTempoAcesso() {
    return tempoAcesso;
  }

}
