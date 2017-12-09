package com.david.rxbusdemo;

/**
 * Created by david on 2017/12/9.
 */

public class RxBusEvent {
  public static class EventShowNumber {
    public int number;

    public EventShowNumber(int number) {
      this.number = number;
    }
  }

  public static class EventAddNumber {

  }

  public static class EventInnerShowNumber {
    public int number;

    public EventInnerShowNumber(int number) {
      this.number = number;
    }
  }

  public static class EventInnerAddNumber {

  }
}
