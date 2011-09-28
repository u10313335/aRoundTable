package tw.jouou.aRoundTable.util;

import android.view.View;

public class Utils {
	public static View[] copyArray(View[] original, int newLength) { 
		View[] copy = new View[newLength]; 
	 	System.arraycopy(original, 0, copy, 0, Math.min(original.length, newLength)); 
	 	return copy; 
	} 
}
