package br.gov.pbh.prodabel.happyeyeballs.teste;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import br.gov.pbh.prodabel.happyeyeballs.HappyEyeBallsException;
import br.gov.pbh.prodabel.happyeyeballs.MelhorIp;

public class MelhorIpTeste {

  @Before
  public void setUp() throws Exception {
  }

  @Test(expected=HappyEyeBallsException.class)
  public void testMelhorIpNulo() throws HappyEyeBallsException {
    MelhorIp ip = new MelhorIp(10L, null, 99);
  }
  
  @Test(expected=HappyEyeBallsException.class)
  public void testMelhorIpPortaErrada() throws HappyEyeBallsException, UnknownHostException {
    List<InetAddress> a = new LinkedList<InetAddress>();
    a.add(InetAddress.getByName("127.0.0.1"));
    MelhorIp ip = new MelhorIp(10L, a, Integer.MAX_VALUE);
  }

}
