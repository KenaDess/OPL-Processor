package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaveMap {
	
	private static HashMap<String, String> bindsMap = new HashMap<String, String>();
	private static HashMap<String, String> bindsToInstanceMap = new HashMap<String, String>();
	private static HashMap<String, List<String>> constructorParamentersMap = new HashMap<String, List<String>>();
	private static HashMap<String, HashMap<String, String>> instancesMap = new HashMap<String, HashMap<String, String>>();
	private static HashMap<String, String> bindsMethodsMap = new HashMap<String, String>();
	private static HashMap<String, List<String>> providerMethodsMap = new HashMap<String, List<String>>();
	
	/**
	 * Save the className with it mapping value
	 * @param className
	 * @param value
	 */
	public static void saveBinds(String className, String value){
		bindsMap.put(className, value);
	}
	
	/**
	 * Returns true if the map contains the className, false if not.
	 * @param className
	 * @return
	 */
	public static boolean containsClass(String className){
		return bindsMap.containsKey(className);
	}
	
	/**
	 * Return the mapping value for the className
	 * @param className
	 * @return
	 */
	public static String getClassValue(String className){
		return bindsMap.get(className);
	}	
	
	/**
	 * Save the methodName with it mapping value
	 * @param className
	 * @param value
	 */
	public static void saveMethod(String methodName, String className){
		bindsMethodsMap.put(methodName, className);
	}
	
	/**
	 * Returns true if the map contains the methodName, false if not.
	 * @param className
	 * @return
	 */
	public static boolean containsMethod(String methodName){
		return bindsMethodsMap.containsKey(methodName);
	}
	
	/**
	 * Return the mapping value for the methodName
	 * @param className
	 * @return
	 */
	public static String getMethodValue(String methodName){
		return bindsMethodsMap.get(methodName);
	}
	
	/**
	 * Save the className with its instance mapping value
	 * @param className
	 * @param value
	 */
	public static void saveBindsToInstance(String className, String value){
		bindsToInstanceMap.put(className, value);
	}
	
	/**
	 * Returns true if the map contains the className, false if not.
	 * @param className
	 * @return
	 */
	public static boolean containsClassToInstance(String className){
		return bindsToInstanceMap.containsKey(className);
	}
	
	/**
	 * Return the mapping value for the className
	 * @param className
	 * @return
	 */
	public static String getClassToInstance(String className){
		return bindsToInstanceMap.get(className);
	}
	
	/**
	 * Save the constructorName with all his parameters
	 * @param constructorName
	 * @param parameters
	 */
	public static void saveConstructorParameters(String constructorName, List<String> parameters){
		constructorParamentersMap.put(constructorName, parameters);
	}
	
	/**
	 * Returns true if the map contains the constructorName, false if not.
	 * @param constructorName
	 * @return
	 */
	public static boolean containsConstructor(String constructorName){
		return constructorParamentersMap.containsKey(constructorName);
	}
	
	/**
	 * Returns the list of parameters from the giving constructorName
	 * @param constructorName
	 * @return
	 */
	public static List<String> getConstructorParameters(String constructorName){
		List<String> params = constructorParamentersMap.get(constructorName);
		return (params != null) ? params : new ArrayList<String>();
	}	

	/**
	 * Save the methodName with its className and returnType
	 * @param returnType
	 * @param className
	 * @param methodName
	 */
	public static void saveProvidesMethod(String returnType, String className, String methodName){
		List<String> list = new ArrayList<String>();
		list.add(className);
		list.add(methodName);
		providerMethodsMap.put(returnType, list);
	}
	
	/**
	 * Returns true if a method of returnType is already mapped , false if is not.
	 * @param returnType
	 * @return
	 */
	public static boolean containsProviderMethod(String returnType){
		if(providerMethodsMap.containsKey(returnType))
			return true;
		else
			return false;
	}
	
	/**
	 * Returns the methodName and className for the given returnType
	 * @param returnType
	 * @return
	 */
	public static List<String> getProviderMethod(String returnType){
		return providerMethodsMap.get(returnType);
	}
	
	/**
	 * Save variableName from the given ClassInstance inside the ClassName 
	 * @param className
	 * @param classInstance
	 * @param variableName
	 */
	public static void saveInstance(String className, String classInstance, String variableName){
		if(instancesMap.containsKey(className))
			instancesMap.get(className).put(classInstance, variableName);
		else{
			instancesMap.put(className, new HashMap<String, String>());
			instancesMap.get(className).put(classInstance, variableName);
		}			
	}
	
	/**
	 * Returns true if a variable of ClassInstance is already instantiated in the ClassName, false if is not.
	 * @param className
	 * @param classInstance
	 * @return
	 */
	public static boolean containsInstance(String className, String classInstance){
		if(instancesMap.containsKey(className))
			return instancesMap.get(className).containsKey(classInstance);
		else
			return false;
	}
	
	/**
	 * Returns the name of the variable (type ClassInstance) instantiated in the ClassName
	 * @param className
	 * @param classInstance
	 * @return
	 */
	public static String getInstanceVariable(String className, String classInstance){
		return instancesMap.get(className).get(classInstance);
	}
	
  /**
   * Returns the list of all the keys in the bindsMap
   * @return 
   */
  public static List<String> getAllKeys() {
    List<String> keys = new ArrayList<String>();
    for (String key : bindsMap.keySet()) {
      keys.add(key);
    }
    return keys;
  }
  
  public static List<String> getAllClases(){
	  List<String> keys = new ArrayList<String>();
	  for (String key : instancesMap.keySet()) {
	      keys.add(key);
	    }
	  return keys;
  }
  
  public static List<String> getAllInstances(String className){
	  List<String> keys = new ArrayList<String>();
	  
	  for(Map.Entry<String,String> map :instancesMap.get(className).entrySet()){
		  keys.add("cls: "+map.getKey()+" |var: "+map.getValue());
	  }
	  
	  return keys;
  }

}
