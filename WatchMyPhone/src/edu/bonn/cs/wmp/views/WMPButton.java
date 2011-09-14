package edu.bonn.cs.wmp.views;

import android.widget.Button;
import android.content.Context;
import android.util.AttributeSet;

@Deprecated
public class WMPButton extends Button {
	private boolean wmpInput = false;

	public void setWmpInput(boolean value) {
		this.wmpInput = value;
	}

	public boolean isWmpInput() {
		return this.wmpInput;
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
