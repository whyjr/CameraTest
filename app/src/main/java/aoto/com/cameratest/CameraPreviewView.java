package aoto.com.cameratest;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

/**
 * @author why
 *
 */
public class CameraPreviewView extends FrameLayout {

    public static final int DEFAULT_PREVIEW_WIDTH = 640;
    public static final int DEFAULT_PREVIEW_HEIGHT = 480;

    public boolean ismPreviewed() {
        return mPreviewed;
    }

    public void setmPreviewed(boolean mPreviewed) {
        this.mPreviewed = mPreviewed;
    }

    private boolean mPreviewed = false;
    boolean mSurfaceCreated = false;

    private Camera mCamera = null;
    private SurfaceTexture mSurfaceTexture = null;
    private FaceDrawerView mFaceDrawerView = null;

    public CameraPreviewView(Context context) {
        this(context, null);
    }

    public CameraPreviewView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CameraPreviewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    Paint getFaceRectPaint() {
        return mFaceDrawerView.getFaceRectPaint();
    }

    void setCamera(Camera camera) {
        mPreviewed = false;
        mCamera = camera;
        if (mCamera != null) {
            requestLayout();

            // get Camera parameters
            Camera.Parameters params = mCamera.getParameters();
            params.setPreviewSize(CameraPreviewView.DEFAULT_PREVIEW_WIDTH, CameraPreviewView.DEFAULT_PREVIEW_HEIGHT);

            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // set the focus mode
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                // set Camera parameters
                mCamera.setParameters(params);
            }

            if (!mPreviewed && mSurfaceCreated) {
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                    mPreviewed = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    void drawFaces(List<Rect> faceRects, int color) {
        try {
            if (mCamera == null || mCamera.getParameters() == null || mCamera.getParameters().getPreviewSize()
                    == null) {
                return;
            }

            mFaceDrawerView.drawFaces(faceRects, mCamera.getParameters().getPreviewSize().height, mCamera
                    .getParameters().getPreviewSize().width, color);
        } catch (RuntimeException e) {
            e.printStackTrace();
            // error when camera interrupted.
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);

        int finalHeight = originalWidth * DEFAULT_PREVIEW_WIDTH / DEFAULT_PREVIEW_HEIGHT;

        super.onMeasure(MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }

    private void initViews() {
        final TextureView textureView = new TextureView(getContext());
        addView(textureView);

        mFaceDrawerView = new FaceDrawerView(getContext());
        addView(mFaceDrawerView);

        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                mSurfaceCreated = true;
                mSurfaceTexture = surface;
                try {
                    mCamera.setPreviewTexture(mSurfaceTexture);
                    mCamera.startPreview();
                    mPreviewed = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "打开相机失败", Toast.LENGTH_SHORT).show();
                }
                Matrix mirrorMatrix = new Matrix();
                mirrorMatrix.setScale(-1, 1, getWidth() / 2, 0);
                textureView.setTransform(mirrorMatrix);

                float scaleX;
                float scaleY;
                Matrix matrix = new Matrix();
                if (getWidth() > getHeight()) {
                    scaleX = getWidth() / (float) DEFAULT_PREVIEW_WIDTH;
                    scaleY = getHeight() / (float) DEFAULT_PREVIEW_HEIGHT;
                } else {
                    scaleX = getWidth() / (float) DEFAULT_PREVIEW_HEIGHT;
                    scaleY = getHeight() / (float) DEFAULT_PREVIEW_WIDTH;
                }
                matrix.setScale(scaleX, scaleY);
                matrix.postScale(-1, 1, getWidth() / 2, 0);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // Do nothing.
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mPreviewed = false;
                }
                mSurfaceCreated = false;
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // Do nothing.
            }
        });
    }
}