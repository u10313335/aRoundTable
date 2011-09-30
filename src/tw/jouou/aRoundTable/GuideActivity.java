package tw.jouou.aRoundTable;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ViewFlipper;

public class GuideActivity extends Activity{
	
	  private ViewFlipper wizardViewFlipper;
	    private RelativeLayout Buttons;
	    private Button prev;
	    private Button next;
	    private int currentPage = 0;
	    private int pageBound;
	    
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.guide);
	        
	        
	       wizardViewFlipper = (ViewFlipper)findViewById(R.id.wizard_flipper);
	       Buttons = (RelativeLayout)findViewById(R.id.Buttons);
	       prev = (Button)findViewById(R.id.action_back);
	       next = (Button)findViewById(R.id.action_next);
	        
	       Buttons.setBackgroundColor(Color.DKGRAY);
	       
	       pageBound = wizardViewFlipper.getChildCount() - 1;
	       
//	       if(isFirstDisplayed()) {
//	           // user walked past beginning of wizard, so return that they cancelled
//			 
//			 Back.setText("Quit");
//			 Back.setOnClickListener(new OnClickListener(){
	//
//				@Override
//				public void onClick(View v) {
//					
//					GuideActivity.this.finish();
//				}
//				 
//				
//				 
//			 });
//			 	
//	       }
//	       
//	       else
//			 {
//				 Back.setText("Back");
//			 }
	        
	       next.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					if(currentPage != pageBound){
						if(currentPage == 0){ 
							
							prev.setText("Back");
							prev.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btn_back, 0, 0, 0);// Todo : set drawable
							
						}
						currentPage++;
						wizardViewFlipper.showNext();
					}else{
						
						next.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
						next.setText("Finish");  /* finish */
						
					}

					

					
				}
	        	
	        	
	        	
	        });
	        
	        prev.setOnClickListener(new OnClickListener(){
	        	
	        	

				@Override
				public void onClick(View v) {
					if(currentPage != 0){
						if(currentPage == 1){
							
							prev.setText("Quit");
							prev.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
							/* TODO: set button to quit */
						}
						next.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_btn_forward, 0, 0, 0);
						next.setText("Next");  /* finish */
						wizardViewFlipper.showPrevious();
						currentPage--;
					}else{
						prev.setText("Quit");
						prev.setOnClickListener(new OnClickListener(){

							@Override
							public void onClick(View v) {
							
								GuideActivity.this.finish();
							}
							
						});/* TODO: quit */
					}
				}
	        	
	        	
	        	
	        });
	        
	      
	        
	        
	    }
	    
	  

}
