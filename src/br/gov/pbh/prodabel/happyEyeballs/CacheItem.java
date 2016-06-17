package br.gov.pbh.prodabel.happyEyeballs;

import java.net.InetAddress;

/**
 * Item do cache para o algoritmo Happy Eyeballs.
 * 
 * @author guilherme
 *
 */
public class CacheItem {
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
  public CacheItem(InetAddress endereco, long tempoAcesso) {
    super();
    this.endereco = endereco;
    this.tempoAcesso = tempoAcesso;
  }

  public InetAddress getEndereco() {
    return endereco;
  }

  public void setEndereco(InetAddress endereco) {
    this.endereco = endereco;
  }

  public Long getTempoAcesso() {
    return tempoAcesso;
  }

  public void setTempoAcesso(Long tempoAcesso) {
    this.tempoAcesso = tempoAcesso;
  }

}
