package com.seatwise.booking.messaging.rebalancer;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ConsistentHash<T> {

  private final int virtualNodes;
  private final NavigableMap<Long, T> circle = new ConcurrentSkipListMap<>();

  public ConsistentHash(int virtualNodes, Collection<T> nodes) {
    this.virtualNodes = virtualNodes;
    for (T node : nodes) {
      add(node);
    }
  }

  public void add(T node) {
    for (int i = 0; i < virtualNodes; i++) {
      long hash = hash(node.toString() + ":" + i);
      circle.put(hash, node);
    }
  }

  public void remove(T node) {
    for (int i = 0; i < virtualNodes; i++) {
      long hash = hash(node.toString() + ":" + i);
      circle.remove(hash);
    }
  }

  public T get(Object key) {
    if (circle.isEmpty()) {
      return null;
    }
    long hash = hash(key.toString());
    Entry<Long, T> ceilingEntry = circle.ceilingEntry(hash);
    if (ceilingEntry == null) {
      ceilingEntry = circle.firstEntry();
    }
    return ceilingEntry.getValue();
  }

  private long hash(String key) {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] digest = md5.digest(key.getBytes(StandardCharsets.UTF_8));
      ByteBuffer buf = ByteBuffer.wrap(digest);
      return buf.getLong();
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("MD5 알고리즘을 찾을 수 없습니다.", e);
    }
  }
}
