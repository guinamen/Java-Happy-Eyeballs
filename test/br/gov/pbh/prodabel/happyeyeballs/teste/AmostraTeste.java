package br.gov.pbh.prodabel.happyeyeballs.teste;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.gov.pbh.prodabel.happyeyeballs.Amostra;

public class AmostraTeste {

  private static final Logger LOGGER = LoggerFactory.getLogger(AmostraTeste.class);
  private Amostra amostra1;
  private Amostra amostra2;
  private Amostra amostra3;
  private static final long AGORA = System.currentTimeMillis();
  
  @Before
  public void setUp() throws Exception {
    testAmostra();
  }

  @Test
  public void testAmostra() throws UnknownHostException, InterruptedException {
    amostra1 = new Amostra(InetAddress.getByName("localhost"), AGORA);
    amostra2 = new Amostra(InetAddress.getByName("localhost"), AGORA);
    amostra3 = new Amostra(InetAddress.getByName("localhost"), AGORA);
    amostra1.setTempoFim(AGORA + 100L);
    amostra2.setTempoFim(AGORA + 200L);
  }

  @Test
  public void testGetEnderecoIp() throws UnknownHostException {
    Assert.assertEquals(amostra1.getEnderecoIp(), InetAddress.getByName("127.0.0.1"));
    Assert.assertEquals(amostra2.getEnderecoIp(), InetAddress.getByName("127.0.0.1"));
  }

  @Test()
  public void testGetTempoFim() {
    Assert.assertEquals(amostra1.getTempoFim(), AGORA + 100L);
    Assert.assertEquals(amostra2.getTempoFim(), AGORA + 200L);
  }

  
  @Test
  public void testGetTempoInicio() {
    Assert.assertEquals(amostra1.getTempoInicio(), AGORA);
    Assert.assertEquals(amostra2.getTempoInicio(), AGORA);
  }

  @Test
  public void testGetTempoTotal() {
    Assert.assertEquals(amostra3.getTempoTotal(), Long.MAX_VALUE);
    Assert.assertEquals(amostra1.getTempoTotal(), 100L);
    Assert.assertEquals(amostra2.getTempoTotal(), 200L);
  }

  @Test
  public void testCompareTo() {
    Assert.assertTrue(amostra1.compareTo(amostra2) < 0);
    Assert.assertTrue(amostra2.compareTo(amostra1) > 0);
    Assert.assertTrue(amostra1.compareTo(null) > 0);
  }

  @Test
  public void testToString() {
    LOGGER.info(amostra1.toString());
    LOGGER.info(amostra2.toString());
  }

}
