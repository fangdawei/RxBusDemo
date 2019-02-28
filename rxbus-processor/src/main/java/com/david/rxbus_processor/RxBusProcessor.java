package com.david.rxbus_processor;

import com.david.rxbus.Subscribe;
import com.google.auto.service.AutoService;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

@AutoService(Processor.class) public class RxBusProcessor extends AbstractProcessor {

  private Filer filer;
  private Messager messager;
  private Elements elementUtils;
  private Map<String, ProxyClassInfo> proxyClassInfoMap = new HashMap<>();

  @Override public synchronized void init(ProcessingEnvironment processingEnvironment) {
    super.init(processingEnvironment);
    filer = processingEnvironment.getFiler();
    messager = processingEnvironment.getMessager();
    elementUtils = processingEnvironment.getElementUtils();
  }

  @Override public Set<String> getSupportedAnnotationTypes() {
    Set<String> supportedAnnotationTypes = new HashSet<>();
    supportedAnnotationTypes.add(Subscribe.class.getCanonicalName());
    return supportedAnnotationTypes;
  }

  @Override public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    Set<? extends Element> subscribeSet = roundEnvironment.getElementsAnnotatedWith(Subscribe.class);
    if (subscribeSet != null && subscribeSet.size() > 0) {
      processSubscribeAnnotations(subscribeSet, roundEnvironment);
      return true;
    } else {
      return false;
    }
  }

  private void processSubscribeAnnotations(Set<? extends Element> set, RoundEnvironment roundEnv) {
    for (Element element : set) {
      ExecutableElement methodElement = (ExecutableElement) element;
      String sourceClassFullName = getClassFullName(methodElement);
      ProxyClassInfo proxyClassInfo = proxyClassInfoMap.get(sourceClassFullName);
      if (proxyClassInfo == null) {
        proxyClassInfo = new ProxyClassInfo(getClassTypeElement(methodElement), getPackageName(methodElement));
        proxyClassInfoMap.put(sourceClassFullName, proxyClassInfo);
      }
      if (checkMethodValid(methodElement)) {
        SourceMethodInfo sourceMethodInfo = new SourceMethodInfo(methodElement);
        proxyClassInfo.addSourceMethodInfo(sourceMethodInfo);
      }
    }
    try {
      for (String sourceClassFullName : proxyClassInfoMap.keySet()) {
        ProxyClassInfo proxyClassInfo = proxyClassInfoMap.get(sourceClassFullName);
        proxyClassInfo.generateJavaFile(filer);
      }
    } catch (IOException e) {
      error(e.getMessage());
    }
  }

  private TypeElement getClassTypeElement(Element element) {
    TypeElement classElement = (TypeElement) element.getEnclosingElement();
    return classElement;
  }

  private String getPackageName(Element element) {
    PackageElement packageElement = elementUtils.getPackageOf(element);
    return packageElement.getQualifiedName().toString();
  }

  private String getClassFullName(Element element) {
    //class type
    TypeElement classElement = getClassTypeElement(element);
    //full class name
    String fqClassName = classElement.getQualifiedName().toString();
    return fqClassName;
  }

  private boolean checkMethodValid(Element annotatedElement) {
    if (annotatedElement.getKind() != ElementKind.METHOD) {
      error(annotatedElement, "%s must be declared on method.", Subscribe.class.getSimpleName());
      return false;
    }
    ExecutableElement executableElement = (ExecutableElement) annotatedElement;
    if (executableElement.getParameters() == null || executableElement.getParameters().size() == 0) {
      error(annotatedElement, "method %s must has at least one parameter.", annotatedElement.getSimpleName());
      return false;
    }
    if (annotatedElement.getModifiers().contains(Modifier.PRIVATE) || annotatedElement.getModifiers()
        .contains(Modifier.ABSTRACT)) {
      error(annotatedElement, "method %s must can not be abstract or private.", annotatedElement.getSimpleName());
      return false;
    }
    return true;
  }

  private void note(String msg) {
    messager.printMessage(Diagnostic.Kind.NOTE, msg);
  }

  private void error(Element e, String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
  }

  private void error(String msg, Object... args) {
    messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args));
  }
}
