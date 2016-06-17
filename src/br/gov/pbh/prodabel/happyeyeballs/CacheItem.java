package br.gov.pbh.prodabel.happyeyeballs;

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
  public CacheItem(final InetAddress endereco, final long tempoAcesso) {
    super();
    this.endereco = endereco;
    this.tempoAcesso = tempoAcesso;
  }

  /**
   * Retorna o endereço.
   * @return endereço ip
   */
  public InetAddress getEndereco() {
    return endereco;
  }

  /**
   * Alterao endereço.
   * @param endereco novo endereço
   */
  public void setEndereco(final InetAddress endereco) {
    this.endereco = endereco;
  }

  /**
   * Retorna o tempo de acesso.
   * @return tempo de acesso
   */
  public Long getTempoAcesso() {
    return tempoAcesso;
  }

  /**
   * Altera o tempo de acesso.
   * @param tempoAcesso novo tempo de acesso
   */
  public void setTempoAcesso(final Long tempoAcesso) {
    this.tempoAcesso = tempoAcesso;
  }

}
