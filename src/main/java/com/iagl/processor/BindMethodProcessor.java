package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.code.CtFieldReadImpl;
import util.SaveMap;

public class BindMethodProcessor extends AbstractProcessor<CtMethod>{

	@Override
	public void process(CtMethod method) {	
		
		HashMap<String, String> saveBind = new HashMap<String, String>();
		
		createInjector(method);
		HashMap<String, String> saveBind2 = doGenerateBindMap(method, saveBind);		
		getInstance(method, saveBind2);
	}	
	
	private void getInstance(CtMethod<?> method, HashMap<String, String> saveBind){
				
		CtBlock body = method.getBody();
		String methodName = method.getSimpleName();
		if(methodName.equals("main")){	
			List<CtStatement> statements = body.getStatements();
			
			PrintWriter writer;					
			try {
				writer = new PrintWriter("C:/Users/AnaGissel/Desktop/the-file-name3.txt");
				
				//Verify Map values
				if(saveBind ==null){
					writer.println("MAP NULL ");
				}
				else{
					writer.println("MAP: "+saveBind.size());
				}
				
				for (String key: saveBind.keySet()) {
					writer.println("key : " + key);
					writer.println("valueMap : " + saveBind.get(key));
				}
				
				//Get all statements from the body
				for(CtStatement statement:statements){	
					Class<CtInvocation> filterClass = CtInvocation.class;
					TypeFilter<CtInvocation> statementFilter = new TypeFilter<CtInvocation>(filterClass);
					List<CtInvocation> expressions = statement.getElements(statementFilter);
					
					for(CtInvocation invocation: expressions){
						if(invocation.toString().contains(".getInstance(")){
							writer.println("inv: "+invocation.toString());
							
							Class<CtFieldReadImpl> filterClass2 = CtFieldReadImpl.class;
							TypeFilter<CtFieldReadImpl> statementFilter2 = new TypeFilter<CtFieldReadImpl>(filterClass2);
							List<CtFieldReadImpl> elements = statement.getElements(statementFilter2);
							for(CtFieldReadImpl element : elements){
								writer.println("type: "+element.getType());							    
								writer.println("map: "+saveBind.get(element.getType().toString()));
							}							
							
							//Replace invocation
							CtTypeReference typeReference = elements.get(0).getType();
							Factory factory = getFactory();

							factory.getEnvironment().setAutoImports(true);
							CtField<?> field = factory.Core().createField();
							field.setSimpleName("exemple");
							field.setType(typeReference);
							field.setParent(statement);							
							
						}
					}
				}
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}				
	}
	
	private void createInjector(CtMethod<?> method){
		String methodName = method.getSimpleName();
		
		if(methodName.equals("main")){	
			CtBlock body = method.getBody();
			
			Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
			TypeFilter<CtVariableAccess> statementFilter = new TypeFilter<CtVariableAccess>(filterClass2);
			List<CtVariableAccess> variables = body.getElements(statementFilter);			
			List<CtStatement> statements = body.getStatements();
			
			Class<CtInvocation> filterClass = CtInvocation.class;
			TypeFilter<CtInvocation> statementFilter2 = new TypeFilter<CtInvocation>(filterClass);
			List<CtInvocation> expressions =body.getElements(statementFilter2);
			
			/*PrintWriter writer;					
			try {
				writer = new PrintWriter("C:/Users/AnaGissel/Desktop/the-file-name2.txt");*/
				for(CtStatement statement:statements){	
					//writer.println("statement: "+statement.toString());
				}
				
				for(CtVariableAccess variable: variables){					
					if( variable.getType().toString().equals("com.google.inject.Injector"))
					{	
						//writer.println("variable: "+variable.toString());
						//writer.println("type: "+variable.getType());
					}
				}
				
				for(CtInvocation invocation: expressions){						
					if(invocation.toString().contains("Guice.createInjector(")){
						//writer.println("field: "+invocation.toString());
					}
				}
				
			/*	writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	*/		
		}
	}
	
	private HashMap<String, String> doGenerateBindMap(CtMethod<?> method,HashMap<String, String> saveBind){
		
		String methodName = method.getSimpleName();
		
		if(methodName.equals("configure")){		
			CtBlock body = method.getBody();
			List<CtStatement> statements = body.getStatements();
			
			PrintWriter writer;					
			try {
			writer = new PrintWriter("C:/Users/AnaGissel/Desktop/the-file-name.txt");
			for(CtStatement statement:statements){				
				
				if(statement.toString().contains("bind(") && statement.toString().contains(".to("))
				{			
					Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
					TypeFilter<CtVariableAccess> statementFilter = new TypeFilter<CtVariableAccess>(filterClass2);
					List<CtVariableAccess> variables = statement.getElements(statementFilter);
					if(variables.size()== 2)	
						if(!saveBind.containsKey(variables.get(0).getType().toString())){
							saveBind.put(variables.get(0).getType().toString(), variables.get(1).getType().toString());
							writer.println("bind: "+variables.get(0).getType().toString()+" to "+variables.get(1).getType().toString());
							//writer.println("save: "+saveBind.get(variables.get(0).getType().toString()));
						}
				}						
			}				
				writer.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return saveBind;
	}
	
}
