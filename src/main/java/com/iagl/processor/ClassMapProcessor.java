package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ClassMapProcessor extends AbstractManualProcessor {

  public static final String ANNOTATION_OVERRIDE = "@java.lang.Override";
  public static final String ANNOTATION_PROVIDER = "@com.google.inject.Provides";

  @Override
  public void process() {

      // Get all classes
	  List<CtClass> classes = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtClass.class));
	  for(CtClass cls: classes){
		  		  
		  if(extendsFromAbstractModule(cls)){
			  Class<CtMethod> filterClass = CtMethod.class;
			  TypeFilter<CtMethod> statementFilter = new TypeFilter<CtMethod>(filterClass);
			  List<CtMethod> ctors = cls.getElements(statementFilter);			  
			    
			    for (CtMethod method : ctors) {
			    	// create BindMap
			    	doGenerateBindMap(method);
			    	
			    	//get @Provider methods
			    	doGenerateProviderMap(method,cls);
			    }			    
		  }	  
	  }    
  }
  
  /**
   * Create the providerMethodsMap
   */
  private void doGenerateProviderMap(CtMethod<?> method,CtClass<?> className){
	  
	  if(isAnnotatedWithProvider(method)){
		  //remove annotations
		  removeAnnotations(method);
		  //save on providerMethodsMap
		  saveProvidesMethod(method,className);
		  //change visibility
		  method.setVisibility(ModifierKind.PUBLIC);
	  }
  }
  
  private void saveProvidesMethod(CtMethod<?> method,CtClass<?> className){
	  String returnType = method.getSignature().replace(method.getSimpleName()+"()", "").replace(" ", "");
	  if(!SaveMap.containsProviderMethod(returnType)){
		  SaveMap.saveProvidesMethod(returnType, className.getQualifiedName(), method.getSimpleName());
	  }
  }
  
  /***
   * Creates the bindMap
   * @param method
   */
  private void doGenerateBindMap(CtMethod<?> method) {
    if (method.getSimpleName().equals("configure") && isAnnotatedOverride(method)
      && method.getType().getQualifiedName().equals("void")) {

      List<CtStatement> statements = method.getBody().getStatements();

      for (CtStatement statement : statements) {
        String classTobind = "";
        String bindTo = "";
        String instance = "";

        // Get all invocations inside the method
        Class<CtInvocation> filterClass = CtInvocation.class;
        TypeFilter<CtInvocation> statementFilter = new TypeFilter<CtInvocation>(filterClass);
        List<CtInvocation> invocations = statement.getElements(statementFilter);
        for (CtInvocation invocation : invocations) {

          Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
          TypeFilter<CtVariableAccess> statementFilter2 = new TypeFilter<CtVariableAccess>(filterClass2);
          List<CtVariableAccess> variables = statement.getElements(statementFilter2);

          if (variables.size() > 0) {
            if (invocation.getType().getSimpleName().equals("AnnotatedBindingBuilder"))
              classTobind = variables.get(0).getType().toString();
            if (invocation.getType().getSimpleName().equals("ScopedBindingBuilder"))
              bindTo = variables.get(1).getType().toString();
            if (invocation.getType().getSimpleName().equals("void"))
              if (invocation.getArguments().size() > 0)
                instance = invocation.getArguments().get(0).toString();
          }
        }

        if (!classTobind.equals("")) {
          // case: bind().toInstance()
          if (!instance.equals("")) {
            if (!SaveMap.containsClassToInstance(classTobind)) {
              SaveMap.saveBindsToInstance(classTobind, instance);
              if (verifyInstanceIsMethod(instance))
                SaveMethodClassMap(instance);

            }
          } else {
            // case: bind();
            if (bindTo.equals("") && instance.equals(""))
              bindTo = classTobind;
            // case: bind() or bind().to()
            if (!SaveMap.containsClass(classTobind)) {
              SaveMap.saveBinds(classTobind, bindTo);
            }
          }
        }
      }
    }
  }
  

  /**
   * Save the class on the MethodMap from the given method
   * @param instance
   */
  private void SaveMethodClassMap(String methodInstance) {
    if (methodInstance.contains(".")) {
      String[] s = methodInstance.split(".");
      methodInstance = s[s.length - 1];
    }

    String methodName = methodInstance.replace("()", "");
    List<CtClass> allClasses = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtClass.class));

    for (CtClass cls : allClasses) {
      List<CtMethod> methods = cls.getMethodsByName(methodName);
      for (CtMethod method : methods) {
        if (method.getSimpleName().equals(methodName))
          method.setVisibility(ModifierKind.PUBLIC);
        if (!SaveMap.containsMethod(methodName))
          SaveMap.saveMethod(methodInstance, cls.getQualifiedName());
      }
    }
  }

  /**
   * Return true if the constructor is annotated with @java.lang.Override, false if is not
   * @param constructor
   * @return
   */
  private Boolean isAnnotatedOverride(CtMethod<? extends Object> constructor) {
    return !constructor.getAnnotations().isEmpty()
      && ANNOTATION_OVERRIDE.equals(constructor.getAnnotations().get(0).getSignature());
  }
  
  /**
   * Return true if the method is annotated with @com.google.inject.Provides, false if is not
   * @param method
   * @return
   */
  private Boolean isAnnotatedWithProvider(CtMethod<?> method){
	  List<CtAnnotation<?>> annotations = method.getAnnotations();
	    
	    Boolean injectFound = false;	    
	    if (!annotations.isEmpty()) {
	      for (int index = 0; index < annotations.size(); index++) {
	        if (ANNOTATION_PROVIDER.equals(annotations.get(index).getSignature()))
	        	injectFound = true;
	      }
	    }	    
	    return injectFound;
  }
  
  /**
   * remove all annotations
   * @param method
   */
  private void removeAnnotations(CtMethod<? extends Object> method) {
	    for (CtAnnotation annotation : method.getAnnotations()) {
	    	method.removeAnnotation(annotation);	      
	    }
  }
  
  /**
   * return true if the Class extends from com.google.inject.AbstractModule, false if not.
   * @return
   */
  private Boolean extendsFromAbstractModule(CtClass<?> cls){
	  //writer.println("super " + cls.getSuperclass() );
	  if(cls.getSuperclass()!=null){
		  if(cls.getSuperclass().toString().equals("com.google.inject.AbstractModule")){
			  return true;
		  }
		  else
			  return false;		  
	  }		  	  
	  else
		  return false;
  }
  
  /**
   * Verify if the instance is a method
   * @param instance
   */
  private Boolean verifyInstanceIsMethod(String instance) {
    if (instance.contains("()"))
      return true;
    else return false;

  }
  
}
