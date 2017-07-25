package br.gov.pbh.prodabel.happyeyeballs;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Interface responsável pela internalização de mensagens.
 * 
 * @author Guilherme
 * @version 1.0
 *
 */
interface Mensagens {

  /**
   * Nome do bundle de mensagens.
   */
  String BUNDLE_NAME = "br.gov.pbh.prodabel.happyeyeballs.mensagens";

  /**
   * Resource bundle de mensagens.
   */
  ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());

  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_0 = Mensagens.getString("HappyEyeballs.0");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_1 = Mensagens.getString("HappyEyeballs.1");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_2 = Mensagens.getString("HappyEyeballs.2");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_3 = Mensagens.getString("HappyEyeballs.3");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_4 = Mensagens.getString("HappyEyeballs.4");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_5 = Mensagens.getString("HappyEyeballs.5");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_6 = Mensagens.getString("HappyEyeballs.6");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_7 = Mensagens.getString("HappyEyeballs.7");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_8 = Mensagens.getString("HappyEyeballs.8");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_9 = Mensagens.getString("HappyEyeballs.9");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_10 = Mensagens.getString("HappyEyeballs.10");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_11 = Mensagens.getString("HappyEyeballs.11");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_12 = Mensagens.getString("HappyEyeballs.12");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_13 = Mensagens.getString("HappyEyeballs.13");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_14 = Mensagens.getString("HappyEyeballs.14");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_15 = Mensagens.getString("HappyEyeballs.15");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_16 = Mensagens.getString("HappyEyeballs.16");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_17 = Mensagens.getString("HappyEyeballs.17");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_18 = Mensagens.getString("HappyEyeballs.18");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_19 = Mensagens.getString("HappyEyeballs.19");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_20 = Mensagens.getString("HappyEyeballs.20");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_21 = Mensagens.getString("HappyEyeballs.21");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_22 = Mensagens.getString("HappyEyeballs.22");
  /**
   * Mensagem.
   */
  String HAPPYEYEBALLS_23 = Mensagens.getString("HappyEyeballs.23");


  /**
   * Obtem a string para a Mensagem. a partir de uma chave.
   * 
   * @param chave chave a ser procurada
   * @return Mensagem.
   */
  static String getString(String chave) {
    return RESOURCE_BUNDLE.getString(chave);
  }

}
