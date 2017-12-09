package com.meimeifa.rxbus_processor;

import com.david.rxbus.Subscribe;
import com.squareup.javapoet.ClassName;
import java.lang.annotation.Annotation;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Created by david on 2017/12/8.
 */

public class SourceMethodInfo {
  private ExecutableElement sourceMethodElement;

  public SourceMethodInfo(ExecutableElement executableElement) {
    this.sourceMethodElement = executableElement;
  }

  public String getMethodName() {
    return sourceMethodElement.getSimpleName().toString();
  }

  public ClassName getEventClassName() {
    List<? extends VariableElement> parameterList = sourceMethodElement.getParameters();
    VariableElement parameter = parameterList.get(0);
    return ClassName.bestGuess(parameter.asType().toString());
  }

  public String getSchedulersCodeString() {
    Annotation annotation = sourceMethodElement.getAnnotation(Subscribe.class);
    Subscribe subscribeAnnotation = (Subscribe) annotation;
    String codeString;
    switch (subscribeAnnotation.thread()) {
      case NEW:
        codeString = "io.reactivex.schedulers.Schedulers.newThread()";
        break;
      case MAIN:
        codeString = "io.reactivex.android.schedulers.AndroidSchedulers.mainThread()";
        break;
      case IO:
        codeString = "io.reactivex.schedulers.Schedulers.io()";
        break;
      case COMPUTATION:
        codeString = "io.reactivex.schedulers.Schedulers.computation()";
        break;
      case CURRENT:
      default:
        codeString = "io.reactivex.schedulers.Schedulers.trampoline()";
        break;
    }
    return codeString;
  }
}
