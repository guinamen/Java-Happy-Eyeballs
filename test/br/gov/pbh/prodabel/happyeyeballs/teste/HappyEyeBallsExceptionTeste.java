package br.gov.pbh.prodabel.happyeyeballs.teste;

import org.junit.Before;
import org.junit.Test;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeBallsException;

public class HappyEyeBallsExceptionTeste {

  @Before
  public void setUp() throws Exception {
  }

  @Test
  public void testHappyEyeBallsExceptionStringThrowable() {
    final HappyEyeBallsException teste = new HappyEyeBallsException("teste");
    teste.toString();
  }

  @Test
  public void testHappyEyeBallsExceptionString() {
    final HappyEyeBallsException teste = new HappyEyeBallsException("teste",
        new Throwable("teste"));
    teste.toString();
  }

}
