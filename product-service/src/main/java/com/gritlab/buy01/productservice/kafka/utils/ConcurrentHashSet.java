package com.gritlab.buy01.productservice.kafka.utils;

import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashSet<E> {

  private static final Object DUMMY_VALUE = new Object();
  private final ConcurrentHashMap<E, Object> map;

  public ConcurrentHashSet() {
    map = new ConcurrentHashMap<>();
  }

  public boolean add(E element) {
    return map.put(element, DUMMY_VALUE) == null;
  }

  public boolean remove(E element) {
    return map.remove(element) != null;
  }

  public boolean contains(E element) {
    return map.containsKey(element);
  }

  public int size() {
    return map.size();
  }
}
