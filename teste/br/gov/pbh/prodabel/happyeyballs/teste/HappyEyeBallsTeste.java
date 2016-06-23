package br.gov.pbh.prodabel.happyeyballs.teste;

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
  public void testaConeccao() throws HappyEyeBallsException {
    LOGGER.info(singleton.obterIp("www.google.com.br", 80).toString());
  }
  
  @Test(expected=HappyEyeBallsException.class)
  public void testaConeccaoErro() throws HappyEyeBallsException {
    LOGGER.info(singleton.obterIp("www.facasfjlerjwl.com.br", 80).toString());
  }

}
