package br.gov.pbh.prodabel.happyeyeballs;

import java.net.InetAddress;

public interface HappyEyeballs {

  /**
   * Obtem o melhor ip para conecção.
   * 
   * @param nomeRede nome do servidor
   * @param porta porta do servidor
   * @return o melhor ip para conexão
   * @throws HappyEyeBallsException caso ocorra algum problema.
   */
  public InetAddress obterIp(final String nomeRede, final int porta) throws HappyEyeBallsException;

  /**
   * Obtem e retorna a implementação do algoritmo Happy EyeBalls padrão.
   * 
   * @return a implementação do algoritmo Happy EyeBalls padrão.
   */
  public static HappyEyeballs getDefaultHappyEyeballs() {
    return HappyEyeballsImpl.getSingleHappyEyeballs();
  }

  /**
   * Termina o pool de threads que executa as consultas de conexão.
   */
  public static void terminarPoolThread() {
    HappyEyeballsImpl.terminarPoolThread();
  }

}
