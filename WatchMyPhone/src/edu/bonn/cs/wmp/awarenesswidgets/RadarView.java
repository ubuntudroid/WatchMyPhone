package edu.bonn.cs.wmp.awarenesswidgets;

import android.view.View;
import android.view.ViewConfiguration;

import java.util.HashMap;
import java.util.Map;

import edu.bonn.cs.wmp.R;
import edu.bonn.cs.wmp.views.WMPEditText;
import edu.bonn.cs.wmp.views.WMPView;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;

public class RadarView extends View implements WMPAwarenessWidget {
	private Map<String, Viewport> viewportLines = new HashMap<String, RadarView.Viewport>();
	private int subjectID;
	private View subject;
	private Canvas subjectCanvas;

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
		} else if (c instanceof TextChange) {
			// reload content
			// TODO: obsolete with CanvasChange?
		} else if (c instanceof CanvasChange) {
			subjectCanvas = ((CanvasChange) c).canvas;
			this.invalidate();
		}
	}

	@Override
	public boolean isInterestedIn(Class<? extends ContentChange> changeClass) {
		return (changeClass.equals(CanvasChange.class) || changeClass
				.equals(ViewportChange.class));
	}

	@Override
	protected void onDraw(Canvas canvas) {
//		super.onDraw(canvas);
		
		if (subject != null) {
//			subject.setDrawingCacheEnabled(true);
			subject.buildDrawingCache();
//			subject.invalidate();
			Bitmap drawingCache = subject.getDrawingCache().copy(Config.ARGB_4444, true);
			subject.destroyDrawingCache();
//			subject.setDrawingCacheEnabled(false);
			// TODO: use the Matrix to scale down the bitmap
			Matrix matrix = new Matrix();
			float sx = (float) this.getWidth()/drawingCache.getWidth();
			float sy = (float) this.getHeight()/drawingCache.getHeight();
			matrix.postScale(sx, sy);
			canvas.drawBitmap(drawingCache, matrix, null);
//			canvas = new Canvas(drawingCache);
		}
		super.onDraw(canvas);
	}

	public void setSubjectView(View v) {
		this.subject = v;
		invalidate();
	}

}
