package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ClassMapProcessor extends AbstractManualProcessor{
	
	public static final String ANNOTATION_OVERRIDE = "@java.lang.Override";
	
	@Override
	public void process() {	
		PrintWriter writer;
	    try {
	      writer = new PrintWriter("C:/Users/AnaGissel/Desktop/bindInvocations.txt");
	      
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
		
		//create BindMap
		for (CtMethod method : methods){
			doGenerateBindMap(method,writer);	
		}	
		writer.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    }
	}
	
	/***
	 * Creates the bindMap
	 * @param method
	 */
	private void doGenerateBindMap(CtMethod<?> method,PrintWriter writer){		
		if(method.getSimpleName().equals("configure") && isAnnotatedOverride(method) 
				&& method.getType().getQualifiedName().equals("void")){	
			
			List<CtStatement> statements = method.getBody().getStatements();
			
			for(CtStatement statement:statements){				
				String classTobind="";
				String bindTo="";
				String instance = "";
				
				//Get all invocations inside the method
				Class<CtInvocation> filterClass = CtInvocation.class;
				TypeFilter<CtInvocation> statementFilter = new TypeFilter<CtInvocation>(filterClass);
				List<CtInvocation> invocations = statement.getElements(statementFilter);
				for(CtInvocation invocation:invocations){					
					
					Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
					TypeFilter<CtVariableAccess> statementFilter2 = new TypeFilter<CtVariableAccess>(filterClass2);
					List<CtVariableAccess> variables = statement.getElements(statementFilter2);
					
					if(variables.size()>0){
						if(invocation.getType().getSimpleName().equals("AnnotatedBindingBuilder"))
							classTobind = variables.get(0).getType().toString();													
						if(invocation.getType().getSimpleName().equals("ScopedBindingBuilder"))
							bindTo = variables.get(1).getType().toString();							
						if(invocation.getType().getSimpleName().equals("void"))								 
							if(invocation.getArguments().size()>0)
								instance = invocation.getArguments().get(0).toString();						
					}										
				}		
				writer.println("classTobind"+classTobind);
				writer.println("bindTo"+bindTo);
				writer.println("instance"+instance);
				
				if(!classTobind.equals("")){
					//case: bind().toInstance()
					if(!instance.equals("")){
						if(!SaveMap.containsClassToInstance(classTobind)){
							SaveMap.saveBindsToInstance(classTobind, instance);
							writer.println("BIND: "+classTobind+" TOINSTANCE "+instance);
							if(verifyInstanceIsMethod(instance))
								SaveMethodClassMap(instance);
							
						}
					}
					else{
						//case: bind();
						if(bindTo.equals("") && instance.equals(""))
							bindTo = classTobind;
						//case: bind() or bind().to()
						if(!SaveMap.containsClass(classTobind))
							{
							SaveMap.saveBinds(classTobind, bindTo);	
							writer.println("BIND: "+classTobind+" TO "+bindTo);
							}
					}					
				}
			}	
		}					
	}
	
	/**
	 * Verify if the instance is a method
	 * @param instance
	 */
	private Boolean verifyInstanceIsMethod(String instance){
		if(instance.contains("()"))
			return true;
		else return false;
		
	}
	
	/**
	 * Save the class on the MethodMap from the given method
	 * @param instance
	 */
	private void SaveMethodClassMap(String methodInstance){	
		if(methodInstance.contains(".")){
			String[] s = methodInstance.split(".");
			methodInstance = s[s.length-1];
		}
		
		String methodName = methodInstance.replace("()", "");
		List<CtClass> allClasses = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtClass.class));
		
		for(CtClass cls: allClasses){			
			List<CtMethod> methods = cls.getMethodsByName(methodName);
			for(CtMethod method: methods){
				if(method.getSimpleName().equals(methodName))
					method.setVisibility(ModifierKind.PUBLIC);
					if(!SaveMap.containsMethod(methodName))
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
}
