package br.gov.pbh.prodabel.happyEyeballs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Tarefa para coletar os IPs que conectaram no menor tempo.
 * 
 * @author guilherme
 *
 */
public class MelhorIP implements Callable<Object[]> {

	/**
	 * Tempo de expiração da atividade.
	 */
	private long TEMPO_TIMEOUT;
	/**
	 * Lista de IPs
	 */
	private List<? extends InetAddress> enderecosIPV;
	/**
	 * Porta para teste de conectividade.
	 */
	private int porta;

	/**
	 * Lista de conexões assíncronas.
	 */
	private List<SocketChannel> canais;
	/**
	 * Gerenciador de conexões assíncronas;
	 */
	Selector selector;

	/**
	 * Construtor simples.
	 * 
	 * @param TEMPO_TIMEOUT
	 *            Tempo de expiração de teste de conexão.
	 * @param enderecosIPV
	 *            Lista de IPs para testar.
	 * @param porta
	 *            Porta para teste de conectividade.
	 */
	public MelhorIP(long TEMPO_TIMEOUT, List<? extends InetAddress> enderecosIPV, int porta) {
		super();
		this.TEMPO_TIMEOUT = TEMPO_TIMEOUT;
		this.enderecosIPV = enderecosIPV;
		this.porta = porta;
	}

	private void fechaConexoes() throws IOException {
		for (SocketChannel canal : canais) {
			canal.finishConnect();
			canal.close();
		}
		selector.close();
		canais.clear();
	}

	/**
	 * Busca o melhor IP
	 * 
	 * @return Tupla IP e tempo para conexão.
	 * @throws IOException
	 *             Exceção caso ocorra algum problema.
	 */
	private Object[] melhorIPV() throws IOException {
		Object[] ret = new Object[2];
		InetAddress melhor = null;
		long tempo = 0;
		// Cria o selector para realizar as conexões de forma assíncrona.
		selector = Selector.open();
		canais = new ArrayList<SocketChannel>(enderecosIPV.size());
		for (InetAddress endereco : enderecosIPV) {
			// Cria os canais e os registram no selector
			SocketChannel canal = SocketChannel.open();
			canal.configureBlocking(false);
			canal.register(selector, SelectionKey.OP_CONNECT, endereco);
			canal.connect(new InetSocketAddress(endereco, porta));
			canais.add(canal);
		}
		long inicio = System.currentTimeMillis();
		// Espera uma ou mais conexões ocorrerem ou o tempo expirar.
		if (selector.select(TEMPO_TIMEOUT) > 0) {
			tempo = System.currentTimeMillis() - inicio;
			Set<SelectionKey> chaves = selector.selectedKeys();
			melhor = (InetAddress) chaves.iterator().next().attachment();
			System.out.println("Ip: " + melhor.getHostAddress() + " em " + tempo + " milisegundos");
		} else {
			fechaConexoes();
			throw new IOException("Tempo de conexão expirado");
		}
		fechaConexoes();
		ret[0] = melhor;
		ret[1] = tempo;
		return ret;
	}

	@Override
	public Object[] call() throws Exception {
		return melhorIPV();
	}

}
