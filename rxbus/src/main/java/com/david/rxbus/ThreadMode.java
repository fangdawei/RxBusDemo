package com.david.rxbus;

/**
 * Created by david on 2017/5/13.
 */

public enum ThreadMode {
  CURRENT,//当前线程
  MAIN,//主线程（UI线程）
  IO,//对应 Schedulers.io()
  COMPUTATION,//对应 Schedulers.computation()
  NEW//创建一个新的线程执行,对应 Schedulers.newThread()
}
