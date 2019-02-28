package com.david.rxbus_processor;

import com.david.rxbus.RxBus;
import com.david.rxbus.RxBusImpl;
import com.david.rxbus.RxBusProxy;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by david on 2017/12/8.
 */

public class ProxyClassInfo {

  private static final String COMPOSITE_DISPOSABLE_FIELD_NAME = "compositeDisposable";

  private TypeElement sourceClassTypeElement;
  private String proxyClassSimpleName;
  private String packageName;
  private List<SourceMethodInfo> sourceMethodInfoList = new ArrayList<>();

  public ProxyClassInfo(TypeElement sourceClassTypeElement, String packageName) {
    this.sourceClassTypeElement = sourceClassTypeElement;
    this.packageName = packageName;
    proxyClassSimpleName = getClassSimpleName(sourceClassTypeElement) + RxBus.PROXY_CLASS_SUFFIX;
  }

  public void addSourceMethodInfo(SourceMethodInfo methodInfo) {
    sourceMethodInfoList.add(methodInfo);
  }

  public void generateJavaFile(Filer filer) throws IOException {
    TypeSpec classType = TypeSpec.classBuilder(proxyClassSimpleName)
        .addModifiers(Modifier.PUBLIC)
        .addSuperinterface(ParameterizedTypeName.get(ClassName.get(RxBusProxy.class),
            TypeVariableName.get(getTypeSimpleName(sourceClassTypeElement))))
        .addField(generateCompositeDisposableField())
        .addMethod(generateRegisterMethod())
        .addMethod(generateUnregisterMethod())
        .build();
    JavaFile javaFile = JavaFile.builder(packageName, classType).build();
    javaFile.writeTo(filer);
  }

  private MethodSpec generateRegisterMethod() {
    MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("register")
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .addParameter(TypeVariableName.get(getTypeSimpleName(sourceClassTypeElement)), "source", Modifier.FINAL);

    ClassName rxBusImplClassName = ClassName.get(RxBusImpl.class);
    ClassName disposableClassName = ClassName.get(Disposable.class);
    ClassName consumerClassName = ClassName.get(Consumer.class);

    methodBuilder.addStatement("$T rxBusImpl = $T.getInstance()", rxBusImplClassName, rxBusImplClassName);
    for (SourceMethodInfo sourceMthodInfo : sourceMethodInfoList) {
      String sourceMethodName = sourceMthodInfo.getMethodName();
      String disposableName = sourceMethodName + "_disposable";

      ClassName eventClassName = sourceMthodInfo.getEventClassName();
      String schedulersCodeString = sourceMthodInfo.getSchedulersCodeString();

      methodBuilder.addCode(getRegisterCodeString(disposableName, sourceMethodName, schedulersCodeString),
          disposableClassName, eventClassName, consumerClassName, eventClassName, eventClassName);
      methodBuilder.addStatement(String.format("%s.add(%s)", COMPOSITE_DISPOSABLE_FIELD_NAME, disposableName));
    }
    return methodBuilder.build();
  }

  private String getRegisterCodeString(String disposableName, String sourceMethodName, String schedulersCode) {
    return String.format("$T %s = rxBusImpl.register($T.class, new $T<$T>() {\n"
        + "      @Override public void accept($T o) throws Exception {\n"
        + "        source.%s(o);\n"
        + "      }\n"
        + "    }, %s);\n", disposableName, sourceMethodName, schedulersCode);
  }

  private MethodSpec generateUnregisterMethod() {
    return MethodSpec.methodBuilder("unregister")
        .addModifiers(Modifier.PUBLIC)
        .returns(void.class)
        .beginControlFlow(String.format("if (%s != null && !%s.isDisposed())", COMPOSITE_DISPOSABLE_FIELD_NAME,
            COMPOSITE_DISPOSABLE_FIELD_NAME))
        .addStatement(String.format("%s.dispose()", COMPOSITE_DISPOSABLE_FIELD_NAME))
        .endControlFlow()
        .build();
  }

  private FieldSpec generateCompositeDisposableField() {
    return FieldSpec.builder(CompositeDisposable.class, COMPOSITE_DISPOSABLE_FIELD_NAME, Modifier.PRIVATE)
        .initializer("new $T()", ClassName.get(CompositeDisposable.class))
        .build();
  }

  private boolean isInternalClass(TypeElement classElement) {
    Element element = classElement.getEnclosingElement();
    return element instanceof TypeElement;
  }

  private String getTypeSimpleName(TypeElement classElement) {
    String name;
    if (isInternalClass(classElement)) {
      if (classElement.getModifiers().contains(Modifier.PRIVATE)) {
        throw new RuntimeException("internal class should be decorate by protected or public , private is not allow");
      }
      name = getTypeSimpleName((TypeElement) classElement.getEnclosingElement()) + "." + classElement.getSimpleName()
          .toString();
    } else {
      name = classElement.getSimpleName().toString();
    }
    return name;
  }

  private String getClassSimpleName(TypeElement classElement) {
    String name;
    if (isInternalClass(classElement)) {
      if (classElement.getModifiers().contains(Modifier.PRIVATE)) {
        throw new RuntimeException("internal class should be decorate by protected or public , private is not allow");
      }
      name = getClassSimpleName((TypeElement) classElement.getEnclosingElement()) + "$" + classElement.getSimpleName()
          .toString();
    } else {
      name = classElement.getSimpleName().toString();
    }
    return name;
  }
}
