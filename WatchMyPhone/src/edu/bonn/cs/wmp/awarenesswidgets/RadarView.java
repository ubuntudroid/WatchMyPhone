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
import android.view.MotionEvent;
import android.view.View;
import edu.bonn.cs.wmp.R;
import edu.bonn.cs.wmp.views.WMPEditText;
import edu.bonn.cs.wmp.views.WMPView;

/**
 * This Awareness Widget shows the complete currently edited document. It
 * reacts on {@link LineChange}s from the corresponding {@link WMPEditText}.
 * Addititonally this view displays the current viewports of all session
 * takers. Changes to these are indicated by {@link ViewportChange}s - and
 * here is the point where these changes get processed.
 * 
 * Keep in mind, that these view is really flexible in it's layout. You may
 * hide it in a slider or display it directly visible on the screen. Any size
 * is allowed. 
 * 
 * @author Sven Bendel
 *
 */
public class RadarView extends View implements WMPAwarenessWidget {
	private Map<String, Viewport> viewportLines = new HashMap<String, RadarView.Viewport>();
	private int subjectID;
	private View subject;
	
	private float[] lineLengths;
	
	/*
	 * TODO: this should be handled more flexible as well as the drawing of the border lines.
	 * Maybe we should just subclass this one and modify the draw method instead directly drawing
	 * the border line in this class.
	 */
	/**
	 * This constant describes the size of the border line if the radar view sits in a slider.
	 */
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
	
	/**
	 * This method does all necessary initialization stuff. Atm this mainly
	 * includes setting the subject view from XML.
	 * @param attrs
	 */
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
	
	/**
	 * This defines a Viewport as it is managed and drawn by a radar view.
	 * A Viewport simply consists of two floats describing the ratios of the
	 * first/last line in the viewport to the number of lines of the document
	 * as it is displayed in the corresponding {@link WMPEditText}. 
	 * 
	 * @author Sven Bendel
	 *
	 */
	private class Viewport {
		@SuppressWarnings("unused")
		public float startPosRatio = -1;
		@SuppressWarnings("unused")
		public float endPosRatio = -1;

		/**
		 * Standard constructor for a new Viewport object.
		 * @param startPosRatio
		 * 			The last line of the viewport to total lines of the viewport 
		 * 			ratio with reference to the way the document is displayed in the
		 * 			corresponding {@link WMPEditText}. E.g. 2/4=0.5 if the document has
		 * 			a total of 4 lines when being displayed in the WMPEditText and the
		 * 			topmost line of the viewport is the second one.
		 * @param endPosRatio
		 * 			The last line of the viewport to total lines of the viewport ratio with
		 * 			reference to the way the document is displayed in the
		 * 			corresponding {@link WMPEditText}. E.g. 2/4=0.5 if the document has
		 * 			a total of 4 lines when being displayed in the WMPEditText and the
		 * 			bottom line of the viewport is the second one.
		 */
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
			
			drawViewports(canvas, lineSpacing);
		}
		
		
		super.onDraw(canvas);
	}
	
	/**
	 * Draws all viewports on this radar view with
	 * respect to the given line spacing.
	 * @param canvas
	 * 			The Canvas to draw onto.
	 * @param lineSpacing
	 * 			The spacing in pixels between each drawn line.
	 */
	private void drawViewports(Canvas canvas, float lineSpacing) {
		drawMyViewport(canvas, lineSpacing);
		drawOtherViewports(canvas, lineSpacing);
	}
	
	/**
	 * Draws the viewport of the local user.
	 * @param canvas
	 * 			The Canvas to draw onto.
	 * @param lineSpacing
	 * 			The spacing in pixels between each drawn line.
	 */
	private void drawMyViewport(Canvas canvas, float lineSpacing) {
		Viewport myViewport = viewportLines.get(ViewportChange.USER_ME);
		if (myViewport == null) {
			myViewport = new Viewport(0, 1);
		}
		Paint myViewportPaint = new Paint();
		myViewportPaint.setColor(Color.argb(55, 0, 0, 255));
		myViewportPaint.setStyle(Style.FILL);
		float myStartPosRatio = myViewport.startPosRatio;
		float myEndPosRatio = myViewport.endPosRatio;
		Rect myDrawingRect = new Rect(3, (int) (myStartPosRatio*this.getHeight()), this.getWidth()-3, (int) (myEndPosRatio*this.getHeight()+lineSpacing));
		canvas.drawRect(myDrawingRect, myViewportPaint);
	}

	/**
	 * Draws the viewports of the for foreign session takers.
	 * @param canvas
	 * 			The Canvas to draw onto.
	 * @param lineSpacing
	 * 			The spacing in pixels between each drawn line.
	 */
	private void drawOtherViewports(Canvas canvas, float lineSpacing) {
		for (String user : viewportLines.keySet()) {
			if (!user.equals(ViewportChange.USER_ME)) {
				Paint viewportPaint = new Paint();
				// TODO: we need a specific color for every collaborator
				viewportPaint.setColor(Color.argb(100, 0, 255, 0));
				viewportPaint.setStyle(Style.STROKE);
				Viewport viewport = viewportLines.get(user);
				float startPosRatio = viewport.startPosRatio;
				float endPosRatio = viewport.endPosRatio;
				Rect r = new Rect(3, (int) (startPosRatio*this.getHeight()), this.getWidth()-3, (int) (endPosRatio*this.getHeight()+lineSpacing));
				viewportPaint.setStrokeWidth(3);
				canvas.drawRect(r, viewportPaint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		/*
		 * swiping over the radar view shall not change
		 * the state of views below 
		 */
		return true;
	}

	@Override
	public void setSubjectView(WMPView v) {
		this.subject = (View) v;
		invalidate();
	}
}
