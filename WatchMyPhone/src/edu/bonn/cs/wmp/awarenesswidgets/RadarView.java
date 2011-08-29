package edu.bonn.cs.wmp.awarenesswidgets;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Style;
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
	
	final int BORDER_PAINT_HEIGHT = 3;
	
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
		public float startPosRatio = -1;
		@SuppressWarnings("unused")
		public float endPosRatio = -1;

		public Viewport(float startPosRatio, float endPosRatio) {
			this.startPosRatio = startPosRatio;
			this.endPosRatio = endPosRatio;
		}

	}

	@Override
	public void onViewContentChange(ContentChange c) {
		if (c instanceof ViewportChange) {
			// draw new rectangle over content
			float startPos = ((ViewportChange) c).top;
			float endPos = ((ViewportChange) c).bottom;
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
		
		// prepare view
		setBackgroundColor(Color.WHITE);
		Paint borderPaint = new Paint();
		borderPaint.setColor(Color.argb(255,199,199,199));
		borderPaint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, getWidth(), BORDER_PAINT_HEIGHT, borderPaint);
		
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
			float lineSpacing = (height - BORDER_PAINT_HEIGHT)/arraySize;
			float lineStart = (float) (width*0.02);
			float fullLineEnd = (float) (width*0.98);
			Paint linePaint = new Paint();
			linePaint.setColor(Color.BLACK);
			linePaint.setStrokeWidth(lineSpacing/5);
			
			float subjectLineLength = subject.getWidth() - subject.getPaddingLeft() - subject.getPaddingRight();
			
			for (int i = 0; i < arraySize; i++) {
				float lineEnd = lineStart + (fullLineEnd-lineStart)/subjectLineLength*lineLengths[i];
				if (lineEnd < lineStart) {
					lineEnd = lineStart;
				}
				canvas.drawLine(lineStart, (i+.5f)*lineSpacing, lineEnd, (i+.5f)*lineSpacing, linePaint);
			}
			
			// draw viewports
			for (String user : viewportLines.keySet()) {
				int color = Color.RED;
				if (user.equals(ViewportChange.USER_ME)) {
					color = Color.argb(100, 0, 0, 255);
				} else {
					// TODO: we need a specific color for every collaborator
					color = Color.argb(100, 0, 255, 0);
				}
				Viewport viewport = viewportLines.get(user);
				float startPosRatio = viewport.startPosRatio;
				float endPosRatio = viewport.endPosRatio;
				Rect r = new Rect(3, (int) (startPosRatio*this.getHeight()), this.getWidth()-3, (int) (endPosRatio*this.getHeight()+lineSpacing));
				Paint viewportPaint = new Paint();
				viewportPaint.setColor(color);
				viewportPaint.setStyle(Style.FILL);
//				viewportPaint.setStrokeWidth(2);
				canvas.drawRect(r, viewportPaint);
			}
		}
		
		
		super.onDraw(canvas);
	}

	public void setSubjectView(View v) {
		this.subject = v;
		invalidate();
	}

}
