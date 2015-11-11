package com.iagl.processor;

import java.util.ArrayList;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
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
			replaceGetInstanceInvocations(method);
		}
		
		// Phase 3: Delete the createInjector object
		for (CtMethod method : methods){
			commentCreateInjectorStatement(method);
		}
	}	
	
	/**
	 * Replace all getInstance invocations
	 * @param method
	 */
	private void replaceGetInstanceInvocations(CtMethod<?> method){				
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
	 * Comments the createInjector object Statement
	 * @param method
	 */
	private void commentCreateInjectorStatement(CtMethod<?> method){
		String methodName = method.getSimpleName();
		
		if(methodName.equals("main")){	
			int statementIndex =0;
			boolean isInjector =false;
			CtBlock body = method.getBody();
			CtCodeSnippetStatement statementToDelete = getFactory().Core().createCodeSnippetStatement();			
			
			for(CtStatement statement:body.getStatements()){	
				statementIndex++;				
				Class<CtLocalVariable> filterClass = CtLocalVariable.class;
				TypeFilter<CtLocalVariable> statementFilter = new TypeFilter<CtLocalVariable>(filterClass);
				List<CtLocalVariable> variables = statement.getElements(statementFilter);
				
				for(CtLocalVariable variable: variables){		
					if( variable.getType().getQualifiedName().equals("com.google.inject.Injector"))
					{		
						isInjector=true;							
						statementToDelete.setValue("//"+statement.toString());
						break;
					}
				}
				if(isInjector)
					break;
			}			
			CtStatement statement = body.getStatement(statementIndex-1);
			statement.replace(statementToDelete);	
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
	 * Generate the SnippetExpressionValue for the className
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
