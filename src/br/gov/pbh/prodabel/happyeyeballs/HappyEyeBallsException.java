/**
 * 
 */
package br.gov.pbh.prodabel.happyeyeballs;

/**
 * Exceção para as atividades do algoritmo Happy Eye Balls.
 * @author guilherme
 *
 */
public class HappyEyeBallsException extends Exception {

  /**
   * Serial Version ID
   */
  private static final long serialVersionUID = -7992763649460204317L;

  /**
   * Cria uma exceção com uma mensagem e a causa.
   * @param message mensagem
   * @param cause causa
   */
  public HappyEyeBallsException(final String mensagem, final Throwable causa) {
    super(mensagem, causa);
  }

  /**
   * Cria uma exceção com uma mensagem.
   * @param message mensagem
   */
  public HappyEyeBallsException(final String mensagem) {
    super(mensagem);
  }

}
