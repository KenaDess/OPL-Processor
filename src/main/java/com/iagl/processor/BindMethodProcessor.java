package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtLocalVariable;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class BindMethodProcessor extends AbstractManualProcessor{

	@Override
	public void process() {		
		PrintWriter writer;
	    try {
	      writer = new PrintWriter("C:/Users/AnaGissel/Desktop/Instances.txt");
	      
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
				
		// Phase 1: Replace all getInstance invocations
		for (CtMethod method : methods){
			replaceGetInstanceInvocations(method,writer);
		}
		
		// Phase 2: Delete the createInjector object
		for (CtMethod method : methods){
			commentCreateInjectorStatement(method);
		}
		writer.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    }
	}	
	
	/**
	 * Replace all getInstance invocations
	 * @param method
	 */
	private void replaceGetInstanceInvocations(CtMethod<?> method,PrintWriter writer){				
		CtBlock body = method.getBody();
		String methodName = method.getSimpleName();
		if(methodName.equals("main")){	
			String className = method.getParent().getSignature();
			List<CtStatement> statements = body.getStatements();										
			
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
														
							String classToBind = invocation.getExecutable().getType().toString();
														
							//generate the snippetExpressionValue
							String value = generateSnippetExpressionValue(className,classToBind,writer);
							
							if(!SaveMap.containsInstance(className, classToBind)){
								SaveMap.saveInstance(className, classToBind, variable.getSimpleName());
								writer.println("SAVE: ");
							}
							
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
	private String generateSnippetExpressionValue(String className,String classToBind,PrintWriter writer){		
		//bind().toInstance()
		if(SaveMap.containsClassToInstance(classToBind))
			return generateBindToInstance(className,classToBind,writer);		
		else
			return generateBindTo(className,classToBind,writer);			
	}
	
	/**
	 * Generate the code for a Bind() OR BindTo()
	 * @param className
	 * @param classToBind
	 * @return
	 */
	private String generateBindTo(String className,String classToBind,PrintWriter writer){
		String bindClass = SaveMap.getClassValue(classToBind);		
		String value =" new "+bindClass+"(";	
		List<String> params = SaveMap.getConstructorParameters(bindClass);
		writer.println("-------BIND--------- ");
		
		for(int i=0;i<params.size();i++){			
			String param =params.get(i);
			writer.println("param: "+param);
			if(SaveMap.containsClass(param) || SaveMap.containsClassToInstance(param)){
				writer.println("contains class");
				//if an instance exists
				if(SaveMap.containsInstance(className, param)){
					param = SaveMap.getInstanceVariable(className, param);	
					writer.println("contains instance");
				}
				else
					param = "new "+SaveMap.getClassValue(param)+"()";				
			}		
			
			if(i==0)
				value =value+param;
			else
				value =value+","+param;
		}
		value = value+")";
		return value;
	}
	
	/**
	 * Generate the code for a  BindToInstance()
	 * @param className
	 * @param classToBind
	 * @return
	 */
	private String generateBindToInstance(String className,String classToBind,PrintWriter writer){
		String instance = SaveMap.getClassToInstance(classToBind);
		writer.println("-------INSTANCE------ ");
		//verify if the instance is a method
		if(instance.contains("()"))	{	
			writer.println("ins "+instance);
			if(SaveMap.containsMethod(instance)){				
				String methodClass = SaveMap.getMethodValue(instance);
				writer.println("methodClass "+methodClass);
				//verfy if an instance of the methodClass exists already 
				if(SaveMap.containsInstance(className, methodClass))
					return SaveMap.getInstanceVariable(className, methodClass)+"."+instance;
				else{
					//create new object
					return "(new "+methodClass+"())."+instance;
				}						
			}					
			else
				return instance;
		}
		else{
			return instance;
		}
	}
}
