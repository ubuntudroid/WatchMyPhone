package edu.bonn.cs.wmpte;

import edu.bonn.cs.wmp.MainActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.SlidingDrawer;
import android.widget.SlidingDrawer.OnDrawerScrollListener;

public class WMPTextEditorActivity extends MainActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
     // prepare Radar View slider
	final ImageView handle = (ImageView) findViewById(R.id.handle);
	handle.setAlpha(100);
	SlidingDrawer slider = (SlidingDrawer) findViewById(R.id.drawer);
	slider.setOnDrawerScrollListener(new OnDrawerScrollListener() {
		
		@Override
		public void onScrollStarted() {
			handle.setAlpha(255);
		}
		
		@Override
		public void onScrollEnded() {
			handle.setAlpha(100);
		}
	});
    }
}