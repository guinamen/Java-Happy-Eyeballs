package br.gov.pbh.prodabel.happyeyeballs;

/**
 * Exceção para informar que o item não está no cache.
 * 
 * @author guilherme
 *
 */
public class ItemNaoEncontrado extends Exception {

  /**
   * Número serial.
   */
  private static final long serialVersionUID = 1L;

  /**
   * Construtor simples.
   */
  public ItemNaoEncontrado() {
    super();
  }

}
