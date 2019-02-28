# RxBusDemo
说到Android中的事件总线，很容易就想到大名鼎鼎的EventBus。他优雅的使用方法，将开发者从广播的繁琐中解脱出来。不过，自从RxJava出来后，在使用了RxJava的项目中，我们已经没有必须的理由引入EventBus了。因为RxJava天生就是观察者模式，支持事件的发布与订阅。
这里演示了如何使用RxJava实现自己的时间总线。同时使用apt和javapoet编译时生成代码，取代反射，提升运行效率。

通过subscribe注解标记事件接收者

``` java
@Subscribe(thread = ThreadMode.MAIN) 
public void showToast(RxBusEvent.EventShowNumber event) {
    Toast.makeText(this, "" + event.number, Toast.LENGTH_SHORT).show();
  }
```

通过RxBus.post()发送事件

``` java
RxBus.post(new RxBusEvent.EventShowNumber(number));
```

当然，别忘记注册事件接受者到RxBus中

``` java
@Override 
protected void onCreate(Bundle savedInstanceState) {
    ...
    RxBus.register(this);
  }
```

在合适的时候取消注册

``` java
@Override 
protected void onDestroy() {
    ...
    RxBus.unregister(this);
  }
```
使用上和EventBus一样，就是这么简单！

