package br.gov.pbh.prodabel.happyeyballs.teste;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeBallsException;
import br.gov.pbh.prodabel.happyeyeballs.HappyEyeballs;

public class HappyEyeBallsTeste {

  private HappyEyeballs singleton;
  private static final Logger LOGGER = LoggerFactory.getLogger(HappyEyeBallsTeste.class);

  @Before
  public void setUp() throws Exception {
    singleton = HappyEyeballs.getSingleHappyEyeballs();
  }

  @Test
  public void testaConeccao() {
    try {
      LOGGER.info(singleton.obterIp("www.google.com.br", 80).toString());
    } catch (HappyEyeBallsException exc) {
      LOGGER.error("Erro:", exc);
      fail("Erro de io");
    } finally {
      singleton.terminarPoolThread();
    }
  }

}
