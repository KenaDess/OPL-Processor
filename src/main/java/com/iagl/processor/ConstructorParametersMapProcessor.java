package com.iagl.processor;

import java.util.ArrayList;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ConstructorParametersMapProcessor extends AbstractManualProcessor{

	public static final String ANNOTATION_INJECT = "@javax.inject.Inject";
	
	@Override
	public void process() {			
	
      // On recupere la liste des contructeurs
      List<CtConstructor> constructors = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtConstructor.class));

      for (CtConstructor ctor : constructors) {
          generateMapConstructorParameters(ctor);		
      }
	}
	
	/**
	 * Generate the maping of all constructor parameters
	 * @param constructor
	 */
	private void generateMapConstructorParameters(CtConstructor<? extends Object> constructor){
		
		List<String> parameters = new ArrayList<String>();
		
		if(getAnnotatedInjectConstructor(constructor)){
			for(CtParameter<?> parameter : constructor.getParameters()){
				parameters.add(parameter.getType().toString());
			}	
			SaveMap.saveConstructorParameters(constructor.getType().toString(), parameters);	
		}			
	}
	
	/**
	   * Returns true if the constructor have an @Inject annotation
	   * @param constructor the constructor to verify
	   * @return 
	   */
	  private Boolean getAnnotatedInjectConstructor(CtConstructor<? extends Object> constructor) {
	    List<CtAnnotation<?>> annotations = constructor.getAnnotations();
	    Boolean injectFound = false;
	    
	    if (!annotations.isEmpty()) {
	      for (int index = 0; index < annotations.size(); index++) {
	        if (ANNOTATION_INJECT.equals(annotations.get(index).getSignature()))
	        	injectFound = true;
	      }
	    }
	    
	    return injectFound;
	  }
}
