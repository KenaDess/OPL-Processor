package util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SaveMap {
	
	private static HashMap<String, String> bindsMap = new HashMap<String, String>();;
	private static HashMap<String, List<String>> constructorParamenters = new HashMap<String, List<String>>();
	
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
	 * Save the constructorName with all his parameters
	 * @param constructorName
	 * @param parameters
	 */
	public static void saveConstructorParameters(String constructorName, List<String> parameters){
		constructorParamenters.put(constructorName, parameters);
	}
	
	/**
	 * Returns true if the map contains the constructorName, false if not.
	 * @param constructorName
	 * @return
	 */
	public static boolean containsConstructor(String constructorName){
		return constructorParamenters.containsKey(constructorName);
	}
	
	/**
	 * Returns the list of parameters from the giving constructorName
	 * @param constructorName
	 * @return
	 */
	public static List<String> getConstructorParameters(String constructorName){
		return constructorParamenters.get(constructorName);
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

}
