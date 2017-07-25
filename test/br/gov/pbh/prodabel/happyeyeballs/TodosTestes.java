package br.gov.pbh.prodabel.happyeyeballs;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeballs;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;



@RunWith(Suite.class)
@SuiteClasses({AmostraTeste.class, HappyEyeBallsExceptionTeste.class, HappyEyeBallsTeste.class,
    MelhorIpTeste.class})
public class TodosTestes {

  @BeforeClass
  public static void setUp() {
    HappyEyeballs.getHappyEyeballsPadrao();
  }

  @AfterClass
  public static void tearDown() {
    HappyEyeballs.terminarPoolThread();
  }
}
