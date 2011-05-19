/*******************************************************************************
 * Copyright (C) 2011 Technische Universität Dresden
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Dresden, University of Technology, Faculty of Computer Science
 * Computer Networks Group: http://www.rn.inf.tu-dresden.de
 * mobilis project: http://mobilisplatform.sourceforge.net
 ******************************************************************************/
package de.tudresden.inf.rn.mobilis.mapdraw.overlaymgr.lazy;

import android.widget.ImageView;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import java.io.InputStream;

public class LazyLoadAnimation {

	private static final java.lang.String LOG_TAG = "Maps_LazyLoadAnimation";

	ImageView imageView;
	public static final int FRAME_DURATION = 20;

	AnimationDrawable anim;
	private boolean first = true;

	public LazyLoadAnimation(ImageView imageView) {
		this.imageView = imageView;
		this.imageView.setVisibility(View.INVISIBLE);
	}

	private void initDefault() {
		String baseurl = "/de/tudresden/inf/rn/mobilis/mapdraw/overlaymgr/lazy/anim/";
		anim = new AnimationDrawable();
		anim.setOneShot(false);

		for (int i = 0; i < 8; i++) {
			String uri = baseurl + String.format("loader0%s.png", i);
			InputStream is = getClass().getResourceAsStream(uri);
			Drawable drawable = Drawable.createFromStream(is, null);
			drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
			anim.addFrame(drawable, FRAME_DURATION);
		}
		this.imageView.setBackgroundDrawable(anim);
	}

	public void stop() {
		imageView.setVisibility(View.INVISIBLE);
		anim.setVisible(false, false);
		anim.stop();
		imageView.postInvalidate();
	}

	public void start() {
		if (first) {
			if (anim == null) {
				initDefault();
				anim.setBounds(0, 0, anim.getMinimumWidth(), anim.getMinimumHeight());
			}
			first = false;
		}
		imageView.setVisibility(View.VISIBLE);
		anim.setVisible(true, true);
		anim.start();
		imageView.postInvalidate();
	}

	public void setAnimationDrawable(AnimationDrawable anim) {
		this.anim = anim;
		this.imageView.setBackgroundDrawable(anim);
	}
}
