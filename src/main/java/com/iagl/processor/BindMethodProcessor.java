package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import spoon.processing.AbstractManualProcessor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;
import util.SaveMap;

public class BindMethodProcessor extends AbstractManualProcessor{

	public static final String ANNOTATION_OVERRIDE = "@java.lang.Override";
	
	@Override
	public void process() {		
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
		
		// Phase 1: create BindMap
		for (CtMethod method : methods){
			doGenerateBindMap(method);	
		}		
		
		// Phase 2: Replace all getInstance invocations
		for (CtMethod method : methods){
			getInstance(method);
		}
		
		// Phase 3: Delete the createInjector object
		for (CtMethod method : methods){
			createInjector(method);
		}
	}	
	
	/**
	 * Replace all getInstance invocations
	 * @param method
	 */
	private void getInstance(CtMethod<?> method){
				
		CtBlock body = method.getBody();
		String methodName = method.getSimpleName();
		if(methodName.equals("main")){	
			List<CtStatement> statements = body.getStatements();
										
			//statement instanceof CtInvocqtio inv.getExecutabe.getSignature().equals
			
			//Get all statements from the body
			for(CtStatement statement:statements){	
				Class<CtLocalVariable> filterClass = CtLocalVariable.class;
				TypeFilter<CtLocalVariable> statementFilter = new TypeFilter<CtLocalVariable>(filterClass);
				List<CtLocalVariable> variables = statement.getElements(statementFilter);
				
				for(CtLocalVariable variable: variables){	
					
					Class<CtInvocation> filterClass2 = CtInvocation.class;
					TypeFilter<CtInvocation> statementFilter2 = new TypeFilter<CtInvocation>(filterClass2);
					List<CtInvocation> expressions = variable.getDefaultExpression().getElements(statementFilter2);
											
					for(CtInvocation invocation: expressions){
						if(invocation.getExecutable().toString().equals("com.google.inject.Injector#getInstance(java.lang.Class)")){				
							String bindClass = SaveMap.getClassValue(invocation.getExecutable().getType().toString());
							List<String> params = SaveMap.getConstructorParameters(bindClass);
							
							//generate the snippetExpressionValue
							String value = generateSnippetExpressionValue(bindClass);
							
							CtCodeSnippetExpression snippet = getFactory().Core().createCodeSnippetExpression();
							snippet.setValue(value);
							snippet.setParent(variable);			
							
							variable.setDefaultExpression(snippet);	
						}
					}		
				}
			}
		}				
	}
	
	/**
	 * Delete the createInjector object
	 * @param method
	 */
	private void createInjector(CtMethod<?> method){
		String methodName = method.getSimpleName();
		
		if(methodName.equals("main")){	
			List<CtStatement> statements = method.getBody().getStatements();
			
			/*PrintWriter writer;					
			try {
				writer = new PrintWriter("C:/Users/AnaGissel/Desktop/the-file-name2.txt");*/
				for(CtStatement statement:statements){	
					
					Class<CtLocalVariable> filterClass = CtLocalVariable.class;
					TypeFilter<CtLocalVariable> statementFilter = new TypeFilter<CtLocalVariable>(filterClass);
					List<CtLocalVariable> variables = statement.getElements(statementFilter);
					
					for(CtLocalVariable variable: variables){		
						if( variable.getType().getQualifiedName().equals("com.google.inject.Injector"))
						{		
							//variable.setSimpleName("gissel");
							
							CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
							snippet.setValue("//"+statement.toString());
							snippet.setParent(variable);
							//snippet.insertAfter(variable);
							//REPLACE
							//statement.replace(snippet);
							//writer.println("snippet: "+snippet.toString());
						}
					}
				}
				
			/*	writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		*/
		}
	}
	
	/***
	 * Creates the bindMap
	 * @param method
	 */
	private void doGenerateBindMap(CtMethod<?> method){
		
		if(method.getSimpleName().equals("configure") && isAnnotatedOverride(method) 
				&& method.getType().getQualifiedName().equals("void")){	
			
			List<CtStatement> statements = method.getBody().getStatements();
			
			for(CtStatement statement:statements){				
				//Get all invocations inside the method
				Class<CtInvocation> filterClass = CtInvocation.class;
				TypeFilter<CtInvocation> statementFilter = new TypeFilter<CtInvocation>(filterClass);
				List<CtInvocation> invocations = statement.getElements(statementFilter);
				for(CtInvocation invocation:invocations){
					
					if(invocation.getType().getQualifiedName().equals("com.google.inject.binder.ScopedBindingBuilder"))
					{						
						Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
						TypeFilter<CtVariableAccess> statementFilter2 = new TypeFilter<CtVariableAccess>(filterClass2);
						List<CtVariableAccess> variables = statement.getElements(statementFilter2);
						if(variables.size()== 2)	
							if(!SaveMap.containsClass(variables.get(0).getType().toString())){
								SaveMap.saveBinds(variables.get(0).getType().toString(), variables.get(1).getType().toString());
							}
					}
				}					
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
	 * Generate the SnippetExpressionValue
	 * @param className
	 * @return
	 */
	private String generateSnippetExpressionValue(String className){
		String value =" new "+className+"(";			
		
		List<String> params = SaveMap.getConstructorParameters(className);
		for(int i=0;i<params.size();i++){
			String param =params.get(i);
			if(SaveMap.containsClass(param))
				param = SaveMap.getClassValue(param);
			
			if(i==0)
				value =value+"new "+param+"()";
			else
				value =value+",new "+param+"()";
		}
		value = value+")";
		return value;
	}
}
