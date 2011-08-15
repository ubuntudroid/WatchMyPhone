package edu.bonn.cs.wmp.awarenesswidgets;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import edu.bonn.cs.wmp.R;
import edu.bonn.cs.wmp.views.WMPView;

public class RadarView extends View implements WMPAwarenessWidget {
	private Map<String, Viewport> viewportLines = new HashMap<String, RadarView.Viewport>();
	private int subjectID;
	private View subject;
	
	private float[] lineLengths;
	
	public RadarView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public RadarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}
	
	public RadarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}
	
	private void init(AttributeSet attrs) {
		TypedArray a = getContext().obtainStyledAttributes(attrs,
				R.styleable.WMPAwarenessWidget);
		subjectID = a.getResourceId(R.styleable.WMPAwarenessWidget_subject,
				0);
		if (subjectID == 0) {
			Log.e("RadarView",
			"Subject for RadarView wasn't correctly set! Set the ressource ID of the subject View in your XML layout via the subject attribute.");
		}
		a.recycle();
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		subject = getRootView().findViewById(subjectID);
		((WMPView) subject).addWidget(this);
	}
	
	private class Viewport {
		@SuppressWarnings("unused")
		public int startPos = -1;
		@SuppressWarnings("unused")
		public int endPos = -1;

		public Viewport(int startPos, int endPos) {
			this.startPos = startPos;
			this.endPos = endPos;
		}

	}

	@Override
	public void onViewContentChange(ContentChange c) {
		if (c instanceof ViewportChange) {
			// draw new rectangle over content
			int startPos = ((ViewportChange) c).startPos;
			int endPos = ((ViewportChange) c).endPos;
			String user = c.user;
			viewportLines.put(user, new Viewport(startPos, endPos));
		} else if (c instanceof LineChange) {
			LineChange lineChange = (LineChange) c;
			lineLengths = lineChange.lineLengths;
			// TODO: maybe just invalidate every three seconds to save cpu time?
			this.invalidate();
		}
	}

	@Override
	public boolean isInterestedIn(Class<? extends ContentChange> changeClass) {
		return (changeClass.equals(LineChange.class) || changeClass
				.equals(ViewportChange.class));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		
		setBackgroundColor(Color.WHITE);
		
		if (lineLengths != null) {
			// draw line indicators
			// TODO: optimize by just redrawing the changed lines
			int height = this.getHeight();
			int width = this.getWidth();
			
			/*
			 *  TODO: handle short documents (indicated by lineSpacing ratio being very big)
			 *  -> these shouldn't be stretched to the maximum heigth
			 */
			
			int arraySize = lineLengths.length;
			float lineSpacing = height/arraySize;
			float lineStart = (float) (width*0.05);
			float fullLineEnd = (float) (width*0.95);
			Paint p = new Paint();
			p.setColor(Color.BLACK);
			p.setStrokeWidth(lineSpacing/5);
			
			float subjectLineLength = subject.getWidth() - subject.getPaddingLeft() - subject.getPaddingRight();
			
			for (int i = 0; i < arraySize; i++) {
				float lineEnd = lineStart + (fullLineEnd-lineStart)/subjectLineLength*lineLengths[i];
				if (lineEnd < lineStart) {
					lineEnd = lineStart;
				}
				canvas.drawLine(lineStart, (i+.5f)*lineSpacing, lineEnd, (i+.5f)*lineSpacing, p);
			}
		}
		
		// draw viewports
		
		super.onDraw(canvas);
	}

	public void setSubjectView(View v) {
		this.subject = v;
		invalidate();
	}

}
