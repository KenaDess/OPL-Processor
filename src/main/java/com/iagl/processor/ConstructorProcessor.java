package com.iagl.processor;

import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.visitor.filter.TypeFilter;

public class ConstructorProcessor extends AbstractManualProcessor {

  public static final String ANNOTATION_INJECT = "@javax.inject.Inject";

  @Override
  public void process() {
    // Phase 1 : on transforme les constructeurs annotes @Inject
    transformAnnotatedConstructors();

    // Phase 2 : on transforme les attributs de classe annotes @Inject
    transformAnnotatedFields();
  }

  /**
   * Transforms the annotated constructors
   */
  public void transformAnnotatedConstructors() {
    // Recherche des constructeurs annotes
    List<CtConstructor> allConstructors = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtConstructor.class));

    for (CtConstructor constructor : allConstructors) {
      Integer indexInjectAnnotation = getIndexAnnotatedInjectConstructor(constructor);

      // Si le constructeur est annote par @Inject, on supprime l'annotation
      if (indexInjectAnnotation != null) {
        CtAnnotation<?> annotationInject = constructor.getAnnotations().get(indexInjectAnnotation);
        constructor.removeAnnotation(annotationInject);
      }
    }
  }

  /**
   * Transforms the annotated class fields 
   */
  public void transformAnnotatedFields() {

  }

  /* ************************************* UTILS **********************************/

  /**
   * Returns true if the constructor have an @Inject annotation
   * @param constructor the constructor to verify
   * @return 
   */
  private Integer getIndexAnnotatedInjectConstructor(CtConstructor<? extends Object> constructor) {
    List<CtAnnotation<?>> annotations = constructor.getAnnotations();

    if (!annotations.isEmpty()) {
      for (int index = 0; index < annotations.size(); index++) {
        if (ANNOTATION_INJECT.equals(annotations.get(index).getSignature()))
          return index;
      }
    }
    return null;
  }

}
