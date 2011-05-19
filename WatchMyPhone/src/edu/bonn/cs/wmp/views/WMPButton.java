package edu.bonn.cs.wmp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class WMPButton extends Button {
	
	private boolean wmpInput = false;
	
	public boolean isWmpInput() {
		return wmpInput;
	}

	public void setWmpInput(boolean wmpInput) {
		this.wmpInput = wmpInput;
	}

	public WMPButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public WMPButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public WMPButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

}
