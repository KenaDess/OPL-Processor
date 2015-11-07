package util;

import java.util.HashMap;
import java.util.Map;

public class SaveMap {
	private static Map<String, String> bindsMap = new HashMap<String, String>();
	
	public static void saveBinds(String className, String value){
		bindsMap.put(className, value);
	}
	
	public static boolean containsClass(String className){
		return bindsMap.containsKey(className);
	}
	
	public static String getClassValue(String className){
		return bindsMap.get(className);
	}	
}
