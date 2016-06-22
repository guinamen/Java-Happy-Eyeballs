package br.gov.pbh.prodabel.happyeyeballs.cache;

import java.io.Serializable;

/**
 * Item do cache para o algoritmo Happy Eyeballs.
 * 
 * @author guilherme
 *
 */
public class CacheItem<T> implements Serializable {
  /**
   * Número serial.
   */
  private static final long serialVersionUID = 2466334712384701929L;
  /**
   * Endereço.
   */
  private final T item;
  /**
   * Tempo em que o endereço foi adicionado no cache.
   */
  private final long tempoAcesso;

  /**
   * Construtor simples.
   * 
   * @param endereco
   *          Endereço
   * @param tempoAcesso
   *          Tempo em que o item foi adicionado.
   */
  public CacheItem(final T item, final long tempoAcesso) {
    super();
    this.item = item;
    this.tempoAcesso = tempoAcesso;
  }

  /**
   * Retorna o item armazenado.
   * 
   * @return
   */
  public T getItem() {
    return item;
  }

  /**
   * Retorna o tempo em que o objeto foi criado.
   * 
   * @return tempo em milisegundos
   */
  public long getTempoAcesso() {
    return System.currentTimeMillis() - tempoAcesso;
  }

}
