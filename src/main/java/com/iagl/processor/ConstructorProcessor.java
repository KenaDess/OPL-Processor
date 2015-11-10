package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.visitor.filter.TypeFilter;

public class ConstructorProcessor extends AbstractManualProcessor {

  public static final String ANNOTATION_INJECT = "@javax.inject.Inject";

  @Override
  public void process() {

    PrintWriter writer;
    try {
      writer = new PrintWriter("C:/Users/Pauline/Desktop/ctor-process.txt");

      // On recupere la liste des contructeurs
      List<CtConstructor> constructors = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtConstructor.class));

      for (CtConstructor ctor : constructors) {

        // On ne garde que les constructeurs avec l'annotation @Inject
        if (isAnnotatedInjectConstructor(ctor)) {
          writer.println("Remplacement du constructeur : " + ctor.getType());
          // Remplacement
          remplaceConstructor(ctor);
        }
      }
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }// fin process

  /**
   * Retourne true si le constructeur est annote de @Inject, false sinon
   * @param constructor le constructeur a verifier
   * @return True s'il est annote @Inject, false sinon
   */
  private boolean isAnnotatedInjectConstructor(CtConstructor<? extends Object> constructor) {
    return !constructor.getAnnotations().isEmpty()
      && ANNOTATION_INJECT.equals(constructor.getAnnotations().get(0).getSignature());
  }
  
  private void remplaceConstructor(CtConstructor<? extends Object> constructor) {
    
  }
}
