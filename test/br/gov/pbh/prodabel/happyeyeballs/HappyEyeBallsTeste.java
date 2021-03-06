package br.gov.pbh.prodabel.happyeyeballs;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeBallsException;
import br.gov.pbh.prodabel.happyeyeballs.HappyEyeballs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class HappyEyeBallsTeste {

  private HappyEyeballs singleton;
  private static final Logger LOGGER = LoggerFactory.getLogger(HappyEyeBallsTeste.class);

  @Before
  public void setUp() throws Exception {
    singleton = HappyEyeballs.getHappyEyeballsPadrao();
  }

  @Test
  public void testaConeccao() throws HappyEyeBallsException {
    try {
      LOGGER.info(singleton.obterIp("www.google.com.br", 80).toString());
    } catch (Exception excecao) {
      LOGGER.error("Erro ao obter www.google.com.br", excecao);
      Assert.fail();
    }
  }

  @Test(expected = HappyEyeBallsException.class)
  public void testaConeccaoErro() throws HappyEyeBallsException {
    LOGGER.info(singleton.obterIp("www.facasfjlerjwl.com.br", 80).toString());
  }

}
