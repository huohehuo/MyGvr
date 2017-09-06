package quseit.mygvr.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/9/5.
 */

public class MyGL20Renderer implements GLSurfaceView.Renderer {
    private Triangle triangle;
    private Square square;
    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";
    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        // 初始化一个三角形
        triangle = new Triangle();
        // 初始化一个正方形
//        square = new Square();
        //设置背景的颜色
        GLES20.glClearColor(0.4f, 0.4f, 0.4f, 1.0f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }
    private final float[] mMVPMatrix = new float[16];
    private final float[] mRotationMatrix = new float[16];
    public volatile float mAngle;
    private final float[] tmpMatrix2 = new float[16];
    @Override
    public void onDrawFrame(GL10 gl10) {
// 重绘背景色
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // 为三角形创建一个旋转变换
//        long time = SystemClock.uptimeMillis() % 4000L;
//        float angle = 0.090f * ((int) time);
//        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);
        // Create a rotation for the triangle
        // long time = SystemClock.uptimeMillis() % 4000L;
        // float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // 合并旋转矩阵到投影和相机视口矩阵
        Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);
// Phone's Z faces up. We need it to face toward the user.
        Matrix.rotateM(tmpMatrix2, 0, 90, 1, 0, 0);
        // 画一个角度
        triangle.draw(mMVPMatrix);

    }
}
