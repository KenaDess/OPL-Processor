package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

public class ConstructorProcessor extends AbstractManualProcessor {

  public static final String ANNOTATION_INJECT = "@javax.inject.Inject";

  @Override
  public void process() {

    PrintWriter writer;
    try {
      writer = new PrintWriter("C:/Users/Pauline/Desktop/ctor-process.txt");
      //writer = new PrintWriter("C:/Users/AnaGissel/Desktop/ctor.txt");

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
    PrintWriter writer;
    try {
    	//writer = new PrintWriter("C:/Users/AnaGissel/Desktop/bindmap-list.txt");
      writer = new PrintWriter("C:/Users/Pauline/Desktop/bindmap-list.txt");
      
      // Pour visualiser
      writer.println("Constructor.getType.getQualifiedName : "+constructor.getType().getQualifiedName());
      writer.println("Constructor.getType.getSimpleName    : "+constructor.getType().getSimpleName());
      
      // Contenu de la bindsMap
      writer.println();
      writer.println("***** Contenu de la bindsMap *****");
      for ( String key : SaveMap.getAllKeys()) {
        writer.println("key : "+key);
      }
      writer.println("\n\n***** Fin contenu *****");
      
      
      // On recupere les parametres du constructeur
      List<String> params = SaveMap.getConstructorParameters(constructor.getType().getQualifiedName());
      
      writer.println("\n\n***** Les parametres du constructeur *****");
      for(String param : params) {
        writer.println("-> Param: "+param);
        
        if(SaveMap.containsClass(param)) {
          writer.println("-> Param remplacant : " + SaveMap.getClassValue(param));
        } else {
          writer.println("-> Pas de param remplacant");
        }
      }
      writer.println("\n\n***** Fin des parametres *****");
      
      
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}
