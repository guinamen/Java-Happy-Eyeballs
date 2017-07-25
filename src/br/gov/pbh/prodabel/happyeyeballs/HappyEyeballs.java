package br.gov.pbh.prodabel.happyeyeballs;

import java.net.InetAddress;

public interface HappyEyeballs {

  /**
   * Obtém o melhor IP para conexão.
   * 
   * @param nomeRede nome do servidor
   * @param porta porta do servidor
   * @return o melhor IP para conexão
   * @throws HappyEyeBallsException caso ocorra algum problema.
   */
  InetAddress obterIp(final String nomeRede, final int porta) throws HappyEyeBallsException;

  /**
   * Obtém e retorna a implementação do algoritmo Happy EyeBalls padrão.
   * 
   * @return a implementação do algoritmo Happy EyeBalls padrão.
   */
  static HappyEyeballs getHappyEyeballsPadrao() {
    return HappyEyeballsImpl.getSingleHappyEyeballs();
  }

  /**
   * Termina o pool de threads que executa as consultas de conexão. Executar esse comando no final
   * da execução da sua aplicação.
   */
  static void terminarPoolThread() {
    HappyEyeballsImpl.terminarPoolThread();
  }

}
