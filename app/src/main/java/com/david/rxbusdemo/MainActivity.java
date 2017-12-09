package com.david.rxbusdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.david.rxbus.RxBus;
import com.david.rxbus.Subscribe;
import com.david.rxbus.ThreadMode;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private TextView numberView;
  private TextView addBtn;
  private TextView innerNumberView;
  private TextView innerAddBtn;
  private int number = 0;
  private Inner inner = new Inner();

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    numberView = (TextView) findViewById(R.id.tv_number);
    addBtn = (TextView) findViewById(R.id.tv_add);
    numberView.setOnClickListener(this);
    addBtn.setOnClickListener(this);

    innerNumberView = (TextView) findViewById(R.id.tv_number_inner);
    innerAddBtn = (TextView) findViewById(R.id.tv_add_inner);
    innerNumberView.setOnClickListener(this);
    innerAddBtn.setOnClickListener(this);

    RxBus.register(this);
    RxBus.register(inner);
  }

  @Subscribe(thread = ThreadMode.MAIN) public void showToast(RxBusEvent.EventShowNumber event) {
    Toast.makeText(this, "" + event.number, Toast.LENGTH_SHORT).show();
  }

  @Subscribe(thread = ThreadMode.MAIN) public void addNumber(RxBusEvent.EventAddNumber event) {
    number++;
    numberView.setText("" + number);
  }

  @Override public void onClick(View v) {
    switch (v.getId()) {
      case R.id.tv_number:
        RxBus.post(new RxBusEvent.EventShowNumber(number));
        break;
      case R.id.tv_add:
        RxBus.post(new RxBusEvent.EventAddNumber());
        break;
      case R.id.tv_number_inner:
        RxBus.post(new RxBusEvent.EventInnerShowNumber(inner.innerNumber));
        break;
      case R.id.tv_add_inner:
        RxBus.post(new RxBusEvent.EventInnerAddNumber());
        break;
    }
  }

  @Override protected void onDestroy() {
    super.onDestroy();
    RxBus.unregister(this);
    RxBus.unregister(inner);
  }

  public class Inner {

    private int innerNumber = 0;

    @Subscribe(thread = ThreadMode.MAIN) public void innerShowToast(RxBusEvent.EventInnerShowNumber event) {
      Toast.makeText(MainActivity.this, "inner:" + event.number, Toast.LENGTH_SHORT).show();
    }

    @Subscribe(thread = ThreadMode.MAIN) public void innerAddNumber(RxBusEvent.EventInnerAddNumber event) {
      innerNumber++;
      innerNumberView.setText("inner:" + innerNumber);
    }
  }
}
