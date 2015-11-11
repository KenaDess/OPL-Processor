package com.iagl.processor;

import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ClassMapProcessor extends AbstractManualProcessor{
	
	public static final String ANNOTATION_OVERRIDE = "@java.lang.Override";
	
	@Override
	public void process() {		
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
		
		//create BindMap
		for (CtMethod method : methods){
			doGenerateBindMap(method);	
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
}
