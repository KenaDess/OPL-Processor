package util;

import java.util.HashMap;

public class SaveMap {
	private HashMap<String, String> bindsMap;
	
	
	public SaveMap() {
		this.bindsMap = new HashMap<String, String>();
	}
	
	public HashMap<String, String> getMap(){
		return this.bindsMap;
	}
	
	public void saveBinds(String className, String value){
		bindsMap.put(className, value);
	}
	
	public boolean containsClass(String className){
		return bindsMap.containsKey(className);
	}
	
	public String getClassValue(String className){
		return bindsMap.get(className);
	}	
}
