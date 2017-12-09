package com.david.rxbus;

/**
 * Created by david on 2017/5/15.
 */

public interface RxBusProxy<S> {

  void register(S source);

  void unregister();
}
