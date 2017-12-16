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

  private FlowableProcessor<Object> flowableProcessor;

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
    flowableProcessor = PublishProcessor.create().toSerialized();
  }

  public void post(Object target) {
    flowableProcessor.onNext(target);
  }

  /**
   * RxBusProxy的register中会调用
   */
  public Disposable register(Class event, Consumer observer, Scheduler scheduler) {
    Flowable flowable = flowableProcessor.ofType(event).observeOn(scheduler);
    Disposable disposable = flowable.subscribe(observer);
    return disposable;
  }
}
