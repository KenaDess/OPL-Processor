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

	@Override
	public void process() {		
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
				
		// Phase 1: Replace all getInstance invocations
		for (CtMethod method : methods){
			replaceGetInstanceInvocations(method);
		}
		
		// Phase 2: Delete the createInjector object
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
