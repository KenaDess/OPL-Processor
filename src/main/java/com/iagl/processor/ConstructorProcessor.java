package com.iagl.processor;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.visitor.filter.TypeFilter;
import spoon.support.reflect.declaration.CtFieldImpl;
import spoon.support.reflect.reference.CtTypeReferenceImpl;
import util.SaveMap;

public class ConstructorProcessor extends AbstractManualProcessor {

  public static final String ANNOTATION_INJECT = "@javax.inject.Inject";

  @Override
  public void process() {
    PrintWriter writer;
    try {
      writer = new PrintWriter("C:/Users/Pauline/Desktop/bindmap-list.txt");
      
    // On recupere la liste des contructeurs
    List<CtConstructor> constructors = getFactory().Package().getRootPackage().getElements(new TypeFilter(CtConstructor.class));

    for (CtConstructor ctor : constructors) {
      Integer indexAnnotation = getIndexAnnotatedInjectConstructor(ctor);

      // On ne garde que les constructeurs avec l'annotation @Inject
      if (indexAnnotation != null) {
        remplaceConstructor(ctor, indexAnnotation);
      }
    } 
    writer.close();
  } catch (FileNotFoundException e) {
    e.printStackTrace();
  }
  }// fin process

  
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
  
  
  private void remplaceConstructor(CtConstructor<? extends Object> constructor, Integer indexAnnotation) {
    
    PrintWriter writer;
    try {
      writer = new PrintWriter("C:/Users/Pauline/Desktop/bindmap-list.txt");
      
      CtParameter<?> ctParamToReplace = null;
      String nameParamToReplace = null;
      String typeParamToReplace = null;
      String typeSubstituteParam = null;
      
      // Pour tous les parametres du constructeur @Inject
      for (CtParameter<?> ctParameter : constructor.getParameters()) {

        // On recupere le parametre remplacant
        typeSubstituteParam = getSubstituteParameter(ctParameter.getType().getQualifiedName());
        
        // S'il existe un parametre remplacant alors on peut remplacer
        if (!typeSubstituteParam.isEmpty()) {
          ctParamToReplace = ctParameter;
          typeParamToReplace = ctParameter.getType().getQualifiedName();
          nameParamToReplace = ctParameter.getSimpleName();
        }
        
      }
      
      // Si on a bien un parametre de subtitution
      if (ctParamToReplace != null) {
        
        // alors on remove le parametre a remplacer
        constructor.removeParameter(ctParamToReplace);
        
        // on remove l'annotation
        constructor.removeAnnotation(constructor.getAnnotations().get(indexAnnotation));
   
        // remplacement de l'affectation de l'attribut a remplacer
        for (CtStatement statement : constructor.getBody().getStatements()){
          writer.println("statement : "+statement.toString());
        }
        
        // on recupere l'attribut de la classe a remplacer (son index)
        CtClass<?> classe = constructor.getParent(CtClass.class);
        Integer indexField = getIndexFieldToReplace(classe.getFields(), typeParamToReplace);

        // on le remplace avec le nouvel attribut
        classe.getFields().get(indexField).replace(createNewField(getSubstituteParameter(typeParamToReplace), nameParamToReplace));
      }

      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Creates a new field
   * @param type the type of the field to create
   * @param name the name of the field to create
   * @return
   */
  private CtFieldImpl createNewField(String type, String name) {
    CtFieldImpl newField = new CtFieldImpl();
    
    CtTypeReferenceImpl newref = new CtTypeReferenceImpl();
    newref.setSimpleName(type);
    
    newField.setType(newref);
    newField.setSimpleName(name);
    newField.setVisibility(ModifierKind.PRIVATE);
    
    return newField;
  }
  
  /**
   * Returns the index of the field to replace in the class 
   * @param fields the list of the fields in the class
   * @param typeField the type's field to replace
   * @return
   */
  private Integer getIndexFieldToReplace(List<CtField<?>> fields, String typeField){
    for (int index = 0; index < fields.size(); index++) {
      if (typeField.equals(fields.get(index).getType().getQualifiedName())) {
        return index;
      }
    }
    return null;
  }
  
  /**
   * Returns the substitute of the oldParameter if exists, an empty string if not.
   * @param oldParameter the parameter to replace
   * @return
   */
  private String getSubstituteParameter(String oldParameter) {
    if(SaveMap.containsClass(oldParameter)) {
      return SaveMap.getClassValue(oldParameter);
    }
    return "";
  }
  
}
