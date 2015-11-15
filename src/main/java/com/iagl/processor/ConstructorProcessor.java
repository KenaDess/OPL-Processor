package com.iagl.processor;

import java.util.List;

import spoon.processing.AbstractManualProcessor;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtField;
import spoon.reflect.visitor.filter.TypeFilter;
import util.SaveMap;

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
      boolean annotedField = removeInjectAnnotation(field);

      if (annotedField) {
        String typeField = field.getType().getQualifiedName();
        String newTypeField = "";

        // On cherche une instance dans la map bind().toInstance()
        if (SaveMap.containsClassToInstance(typeField)) {

          String className = field.getParent().getSignature(); // nom de la classe
          newTypeField = SaveMap.getClassToInstance(typeField); // la nouvelle assignation

          if (newTypeField.contains("()")) { // c'est une methode
            if (SaveMap.containsMethod(newTypeField)) {
              String methodClass = SaveMap.getMethodValue(newTypeField);

              // une instance de ce type existe deja
              if (SaveMap.containsInstance(className, methodClass))
                setFieldAssignement(field, SaveMap.getInstanceVariable(className, methodClass) + "." + newTypeField);
              else {
                // sinon on cree l'objet
                setFieldAssignement(field, "(new " + methodClass + "())." + newTypeField);
              }
            } else {
              setFieldAssignement(field, "new " + newTypeField + "()");
            }
          } else { // ce n'est pas une methode
            setFieldAssignement(field, newTypeField);
          }
        }
        // Si aucune instance n'est trouvee, alors on cherche dans la map bind().to()
        else if (SaveMap.containsClass(typeField)) {
          newTypeField = SaveMap.getClassValue(typeField);
          setFieldAssignement(field, "new " + newTypeField + "()");
        }
        // Sinon, on cree une nouvelle instance de typeField
        else {
          setFieldAssignement(field, "new " + typeField + "()");
        }
      }
    }
  }

  /* ************************************* UTILS **********************************/

  /**
   * Remove the inject annotation on the element if exists, do nothing if not
   * @param element the CtElement to verify
   * @return True if an Inject annotation is deleted, false if not
   */
  private boolean removeInjectAnnotation(CtElement element) {
    for (CtAnnotation annotation : element.getAnnotations()) {
      if (ANNOTATION_INJECT.equals(annotation.getSignature())) {
        element.removeAnnotation(annotation);
        return true;
      }
    }
    return false;
  }

  /**
   * Set an assignment to the field
   * @param field
   * @param assignment
   */
  private void setFieldAssignement(CtField field, String assignment) {
    CtCodeSnippetExpression snippet = getFactory().Core().createCodeSnippetExpression();
    snippet.setValue(assignment);
    snippet.setParent(field);
    field.setDefaultExpression(snippet);
  }
}
