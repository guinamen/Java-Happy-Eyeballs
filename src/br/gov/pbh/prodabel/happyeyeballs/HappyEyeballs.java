package br.gov.pbh.prodabel.happyeyeballs;

import java.net.InetAddress;

public interface HappyEyeballs {
  
  public InetAddress obterIp(final String nomeRede, final int porta) throws HappyEyeBallsException;
  
  public static HappyEyeballs getDefaultHappyEyeballs() {
    return HappyEyeballsImpl.getSingleHappyEyeballs();
  }

  public static void terminarPoolThread() {
    HappyEyeballsImpl.terminarPoolThread();
  }

}
