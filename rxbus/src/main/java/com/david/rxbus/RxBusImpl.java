package com.david.rxbus;

import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by david on 2017/5/13.
 */

public class RxBusImpl {

  private static volatile RxBusImpl instance;

  private FlowableProcessor<Object> mBus;
  private Map<Class, Disposable> eventDisposableMap;

  public static RxBusImpl getInstance() {
    if (instance != null) {
      return instance;
    } else {
      synchronized (RxBusImpl.class) {
        if (instance == null) {
          instance = new RxBusImpl();
        }
      }
      return instance;
    }
  }

  private RxBusImpl() {
    mBus = PublishProcessor.create().toSerialized();
    eventDisposableMap = new HashMap<>();
  }

  public void post(Object target) {
    mBus.onNext(target);
  }

  /**
   * RxBusProxy的register中会调用
   */
  public Disposable register(Class event, Consumer observer, Scheduler scheduler) {
    Flowable flowable = mBus.ofType(event).observeOn(scheduler);
    Disposable disposable = flowable.subscribe(observer);
    eventDisposableMap.put(event, disposable);
    return disposable;
  }

  /**
   * RxBusProxy的unregister中会调用
   */
  public void unregister(Class event) {
    Disposable disposable = eventDisposableMap.get(event);
    if (disposable != null) {
      if (!disposable.isDisposed()) {
        disposable.dispose();
      }
      eventDisposableMap.remove(event);
    }
  }
}
