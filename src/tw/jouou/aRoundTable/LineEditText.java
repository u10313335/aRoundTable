package tw.jouou.aRoundTable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Gravity;
import android.widget.EditText;

public class LineEditText extends EditText {
	
	 private Paint mPaint;  
    
	    public LineEditText(Context context) {  
	        super(context);  
	        // TODO Auto-generated constructor stub  
	       
	    }  
	      
	    @Override  
	    public void onDraw(Canvas canvas)  
	    {  
	          
	        int lineHeight=this.getLineHeight();
	        Paint mPaint=getPaint();
	        mPaint.setColor(Color.LTGRAY);
	        int topPadding=this.getPaddingTop();
	        int leftPadding=this.getPaddingLeft();
	        float textSize=getTextSize();
	        setGravity(Gravity.LEFT|Gravity.TOP);
	        int y =(int)(topPadding+textSize);
	        for(int i=0;i<getLineCount()+50;i++){
	         canvas.drawLine(leftPadding, y+2, getRight()-leftPadding, y+2, mPaint);
	         y+=lineHeight;
	        }
	        
	        canvas.translate(0, 0);
	        super.onDraw(canvas);
	    }  

}

