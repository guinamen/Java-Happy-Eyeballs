package br.gov.pbh.prodabel.happyeyeballs;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Interface responsável pela internalização de mensagens.
 * 
 * @author Guilherme
 * @version 1.0
 *
 */
interface Mensagens {

  public static final String BUNDLE_NAME = "br.gov.pbh.prodabel.happyeyeballs.mensagens";

  public static final ResourceBundle RESOURCE_BUNDLE =
      ResourceBundle.getBundle(BUNDLE_NAME, Locale.getDefault());

  public static final String HAPPYEYEBALLS_0 = Mensagens.getString("HappyEyeballs.0");
  public static final String HAPPYEYEBALLS_1 = Mensagens.getString("HappyEyeballs.1");
  public static final String HAPPYEYEBALLS_2 = Mensagens.getString("HappyEyeballs.2");
  public static final String HAPPYEYEBALLS_3 = Mensagens.getString("HappyEyeballs.3");
  public static final String HAPPYEYEBALLS_4 = Mensagens.getString("HappyEyeballs.4");
  public static final String HAPPYEYEBALLS_5 = Mensagens.getString("HappyEyeballs.5");
  public static final String HAPPYEYEBALLS_6 = Mensagens.getString("HappyEyeballs.6");
  public static final String HAPPYEYEBALLS_7 = Mensagens.getString("HappyEyeballs.7");
  public static final String HAPPYEYEBALLS_8 = Mensagens.getString("HappyEyeballs.8");
  public static final String HAPPYEYEBALLS_9 = Mensagens.getString("HappyEyeballs.9");
  public static final String HAPPYEYEBALLS_10 = Mensagens.getString("HappyEyeballs.10");
  public static final String HAPPYEYEBALLS_11 = Mensagens.getString("HappyEyeballs.11");
  public static final String HAPPYEYEBALLS_12 = Mensagens.getString("HappyEyeballs.12");
  public static final String HAPPYEYEBALLS_13 = Mensagens.getString("HappyEyeballs.13");
  public static final String HAPPYEYEBALLS_14 = Mensagens.getString("HappyEyeballs.14");
  public static final String HAPPYEYEBALLS_15 = Mensagens.getString("HappyEyeballs.15");
  public static final String HAPPYEYEBALLS_16 = Mensagens.getString("HappyEyeballs.16");
  public static final String HAPPYEYEBALLS_17 = Mensagens.getString("HappyEyeballs.17");
  public static final String HAPPYEYEBALLS_18 = Mensagens.getString("HappyEyeballs.18");
  public static final String HAPPYEYEBALLS_19 = Mensagens.getString("HappyEyeballs.19");
  public static final String HAPPYEYEBALLS_20 = Mensagens.getString("HappyEyeballs.20");
  public static final String HAPPYEYEBALLS_21 = Mensagens.getString("HappyEyeballs.21");
  public static final String HAPPYEYEBALLS_22 = Mensagens.getString("HappyEyeballs.22");
  public static final String HAPPYEYEBALLS_23 = Mensagens.getString("HappyEyeballs.23");

  /**
   * Obtem a string para a mensagem a partir de uma chave.
   * 
   * @param chave chave a ser procurada
   * @return mensagem
   */
  public static String getString(String chave) {
    try {
      return RESOURCE_BUNDLE.getString(chave);
    } catch (MissingResourceException exceptioin) {
      return '!' + chave + '!';
    }
  }

}
