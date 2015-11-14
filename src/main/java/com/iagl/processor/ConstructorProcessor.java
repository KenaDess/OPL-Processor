package com.iagl.processor;

import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
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
      removeInjectAnnotation(constructor);
    }
  }

  /**
   * Transforms the annotated class fields 
   */
  public void transformAnnotatedFields() {
    // Recherche des champs annotes
    List<CtField> allFields = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtField.class));

    for (CtField field : allFields) {
      removeInjectAnnotation(field);
    }
  }

  /* ************************************* UTILS **********************************/

  /**
   * Remove the inject annotation on the element if exists, do nothing if not
   * @param element the CtElement to verify
   */
  private void removeInjectAnnotation(CtElement element) {
    for (CtAnnotation annotation : element.getAnnotations()) {
      if (ANNOTATION_INJECT.equals(annotation.getSignature()))
        element.removeAnnotation(annotation);
    }
  }

}
