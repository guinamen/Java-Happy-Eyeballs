package br.gov.pbh.prodabel.happyeyeballs.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Classe para gerenciar itens em cache.
 * 
 * @author guilherme
 *
 * @param <T>
 *          item a ser armazenado.
 */
public class Cache<T> {
  /**
   * Cache para armazenar as resoluções dos nomes.
   */
  private final Map<String, CacheItem<T>> mapa = new ConcurrentHashMap<String, CacheItem<T>>();

  /**
   * Tempo de expiração em milisegundos.
   */
  private final long tempoExpiracao;
  /**
   * Semáforo para bloquear as threads para cálculo de tempo de expiração.
   */
  private static final Object MUTEX = new Object();

  /**
   * Construtor do cache.
   * 
   * @param tempoExpiracao
   *          tempo para expiração em milisegundos
   */
  public Cache(final long tempoExpiracao) {
    super();
    this.tempoExpiracao = tempoExpiracao;
  }

  /**
   * Busca itens no cache.
   * 
   * @param nome
   *          nome e porta do serviço
   * @return endereço ip, ou nulo caso não exista no cache
   * @throws ItemNaoEncontrado
   *           caso o item não exista.
   */
  public T obtem(final String nome) throws ItemNaoEncontrado {
    // Varre o cache
    if (mapa.containsKey(nome)) {
      final CacheItem<T> item = mapa.get(nome);
      // Verifica se o tempo expirou
      boolean expirou = false;
      synchronized (MUTEX) {
        expirou = System.currentTimeMillis() - item.getTempoAcesso() > tempoExpiracao;
      }
      if (expirou) {
        return item.getItem();
      } else {
        mapa.remove(nome);
      }
    }
    throw new ItemNaoEncontrado();
  }

  /**
   * Adiciona item no cache
   * 
   * @param nome
   *          nome
   * @param item
   */
  public void adiciona(final String nome, final CacheItem<T> item) {
    mapa.put(nome, item);
  }

  /**
   * Limpa o cache.
   */
  public void limpa() {
    synchronized (MUTEX) {
      mapa.clear();
    }
  }

  /**
   * Retorna o tempo de expiração.
   * 
   * @return tempo em milisegundos
   */
  public long getTempoExpiracao() {
    return tempoExpiracao;
  }

}
