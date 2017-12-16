package com.david.rxbus;

import java.util.HashMap;
import java.util.Map;

public class RxBus {

  public static final String PROXY_CLASS_SUFFIX = "_RxBusProxy";

  private static Map<String, RxBusProxy> proxyMap = new HashMap<>();

  public static void register(Object source) {
    RxBusProxy proxy = findRxBusProxy(source);
    if (proxy != null) {
      proxy.register(source);
      addRxBusProxy(source, proxy);
    }
  }

  public static void unregister(Object source) {
    RxBusProxy proxy = findRxBusProxy(source);
    if (proxy != null) {
      proxy.unregister();
      removeRxBusProxy(source);
    }
  }

  public static void post(Object target) {
    RxBusImpl.getInstance().post(target);
  }

  private static RxBusProxy findRxBusProxy(Object source) {
    try {
      Class clazz = source.getClass();
      String className = clazz.getName();
      RxBusProxy proxy = proxyMap.get(className);
      if (proxy == null) {
        Class proxyClass = Class.forName(className + PROXY_CLASS_SUFFIX);
        proxy = (RxBusProxy) proxyClass.newInstance();
      }
      return proxy;
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    } catch (InstantiationException e) {
      e.printStackTrace();
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    throw new RuntimeException(String.format("can not find %s , something when compiler.",
        source.getClass().getSimpleName() + PROXY_CLASS_SUFFIX));
  }

  private static void addRxBusProxy(Object source, RxBusProxy proxy) {
    proxyMap.put(source.getClass().getName(), proxy);
  }

  private static void removeRxBusProxy(Object source) {
    Class clazz = source.getClass();
    String className = clazz.getName();
    RxBusProxy proxy = proxyMap.get(clazz.getName());
    if (proxy != null) {
      proxyMap.remove(className);
    }
  }
}
