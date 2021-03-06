package br.gov.pbh.prodabel.happyeyeballs;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeBallsException;
import br.gov.pbh.prodabel.happyeyeballs.MelhorIp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;



public class MelhorIpTeste {

  private static final Logger LOGGER = LoggerFactory.getLogger(MelhorIpTeste.class);

  @Test(expected = HappyEyeBallsException.class)
  public void testMelhorIpNulo() throws HappyEyeBallsException {
    MelhorIp ip = new MelhorIp(10L, null, 99);
    LOGGER.info(ip.toString());
  }

  @Test(expected = HappyEyeBallsException.class)
  public void testMelhorIpPortaErrada() throws HappyEyeBallsException, UnknownHostException {
    List<InetAddress> teste = new LinkedList<InetAddress>();
    teste.add(InetAddress.getByName("127.0.0.1"));
    MelhorIp ip = new MelhorIp(10L, teste, Integer.MAX_VALUE);
    LOGGER.info(ip.toString());
  }

}
