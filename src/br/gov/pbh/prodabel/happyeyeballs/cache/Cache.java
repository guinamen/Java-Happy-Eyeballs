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
  private final Map<String, CacheItem<T>> mapa;

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
    this.mapa = new ConcurrentHashMap<String, CacheItem<T>>();
  }

  /**
   * Verifica se o tempo do item no cache expirou.
   * 
   * @param nome
   *          nome do serviço
   * @param item
   *          item a ser verificado
   * @return verdadadeiro caso o item expirou, ou falço caso contrário.
   */
  private boolean expirou(final String nome, final CacheItem<T> item) {
    final boolean expirou = item.getTempoAcesso() > tempoExpiracao ? true : false;
    if (expirou) {
      mapa.remove(nome);
    }
    return expirou;
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
  public CacheItem<T> obtem(final String nome) throws ItemNaoEncontrado {
    // Varre o cache
    if (mapa.containsKey(nome)) {
      final CacheItem<T> item = mapa.get(nome);
      // Verifica se o tempo expirou
      if (!expirou(nome, item)) {
        return item;
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
