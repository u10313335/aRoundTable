package tw.jouou.aRoundTable.lib;

import java.util.HashMap;
import java.util.Map;

public class ScopedHashMap extends HashMap<String, String> {
	private String scope;
	
	public ScopedHashMap(String scope){
		this.scope = scope;
	}
	
	public String put(String key, String value){
		return super.put(scope + "[" + key + "]", value);
	}
	
}
