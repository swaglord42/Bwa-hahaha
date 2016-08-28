package com.wikitude.samples.rendering.internal;

import android.opengl.GLSurfaceView;

import com.wikitude.common.rendering.RenderExtension;
import com.wikitude.common.tracking.RecognizedTarget;
import com.wikitude.samples.rendering.external.StrokedRectangle;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CustomRenderExtension implements GLSurfaceView.Renderer, RenderExtension {

    private RecognizedTarget _currentlyRecognizedTarget = null;
    private StrokedRectangle _strokedRectangle;

    @Override
    public void onDrawFrame(final GL10 unused) {
        if (_currentlyRecognizedTarget != null) {
            _strokedRectangle.onDrawFrame(_currentlyRecognizedTarget);
        }
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        _strokedRectangle = new StrokedRectangle();
    }

    @Override
    public void onSurfaceChanged(final GL10 unused, final int width, final int height) {
    }

    public void onResume() {
    }

    public void onPause() {
    }

    public void setCurrentlyRecognizedTarget(final RecognizedTarget currentlyRecognizedTarget_) {
        _currentlyRecognizedTarget = currentlyRecognizedTarget_;
    }

}
