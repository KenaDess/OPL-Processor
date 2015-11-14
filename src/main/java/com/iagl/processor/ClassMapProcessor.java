package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtAbstractInvocation;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.code.CtStatement;
import spoon.reflect.code.CtVariableAccess;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.reference.CtParameterReference;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ClassMapProcessor extends AbstractManualProcessor{
	
	public static final String ANNOTATION_OVERRIDE = "@java.lang.Override";
	
	@Override
	public void process() {	
		/*PrintWriter writer;
	    try {
	      writer = new PrintWriter("C:/Users/AnaGissel/Desktop/bindInvocations.txt");
	      */
		//Get all methods
		List<CtMethod> methods = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtMethod.class));
		
		//create BindMap
		for (CtMethod method : methods){
			doGenerateBindMap(method);	
		}	
		/*writer.close();
	    } catch (FileNotFoundException e) {
	      e.printStackTrace();
	    }*/
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
				//writer.println("------------- statement ----------- \n");
				
				String classTobind="";
				String bindTo="";
				String instance = "";
				
				//Get all invocations inside the method
				Class<CtInvocation> filterClass = CtInvocation.class;
				TypeFilter<CtInvocation> statementFilter = new TypeFilter<CtInvocation>(filterClass);
				List<CtInvocation> invocations = statement.getElements(statementFilter);
				for(CtInvocation invocation:invocations){
					//writer.println("invocation : "+invocation.toString());
					//writer.println("inv type : "+invocation.getType().getSimpleName());					
					
					Class<CtVariableAccess> filterClass2 = CtVariableAccess.class;
					TypeFilter<CtVariableAccess> statementFilter2 = new TypeFilter<CtVariableAccess>(filterClass2);
					List<CtVariableAccess> variables = statement.getElements(statementFilter2);
					
					if(variables.size()>0){
						if(invocation.getType().getSimpleName().equals("AnnotatedBindingBuilder")){
							classTobind = variables.get(0).getType().toString();
							//writer.println("classTobind: "+classTobind );
						}
							
						if(invocation.getType().getSimpleName().equals("ScopedBindingBuilder")){
							bindTo = variables.get(1).getType().toString();
							//writer.println("bindTo: "+bindTo );
						}
							
						if(invocation.getType().getSimpleName().equals("void")){							
							if(invocation.getArguments().size()>0)
								instance = invocation.getArguments().get(0).toString();						
						}
					}										
				}		
				
				if(!classTobind.equals("")){
					//case: bind().toInstance()
					if(!instance.equals("")){
						if(!SaveMap.containsClassToInstance(classTobind)){
							SaveMap.saveBindsToInstance(classTobind, instance);
							//writer.println("BIND: "+classTobind +" TOINSTANCE: "+instance);
						}
					}
					else{
						//case: bind();
						if(bindTo.equals("") && instance.equals(""))
							bindTo = classTobind;
						//case: bind() or bind().to()
						if(!SaveMap.containsClass(classTobind)){
							SaveMap.saveBinds(classTobind, bindTo);	
							//writer.println("BIND: "+classTobind +" TO: "+bindTo);
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
