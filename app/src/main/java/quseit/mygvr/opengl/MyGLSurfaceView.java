package quseit.mygvr.opengl;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import com.google.vr.sdk.controller.Controller;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2017/9/5.
 */

public class MyGLSurfaceView extends GLSurfaceView {
    // Tracks the orientation of the phone. This would be a head pose if this was a VR application.
    // See the {@link Sensor.TYPE_ROTATION_VECTOR} section of {@link SensorEvent.values} for details
    // about the specific sensor that is used. It's important to note that this sensor defines the
    // Z-axis as point up and the Y-axis as pointing toward what the phone believes to be magnetic
    // north. Google VR's coordinate system defines the Y-axis as pointing up and the Z-axis as
    // pointing toward the user. This requires a 90-degree rotation on the X-axis to convert between
    // the two coordinate systems.
    private final SensorManager sensorManager;
    private final Sensor orientationSensor;
    private final PhoneOrientationListener phoneOrientationListener;
    private float[] phoneInWorldSpaceMatrix = new float[16];

    // Tracks the orientation of the physical controller in its properly centered Start Space.
    private Controller controller;
    /**
     * See {@link #resetYaw}
     */
    private float[] startFromSensorTransformation;
    private float[] controllerInStartSpaceMatrix = new float[16];

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 创建一个OpenGL ES 2.0 context
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);

        //设置Renderer到GLSurfaceView
        setRenderer(new MyGL20Renderer());
        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        phoneOrientationListener = new PhoneOrientationListener();
    }

    /**
     * Bind the controller used for rendering.
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }

    /**
     * Start the orientation sensor only when the app is visible.
     */
    public void startTrackingOrientation() {
        sensorManager.registerListener(
                phoneOrientationListener, orientationSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * This is similar to {@link com.google.vr.sdk.base.GvrView#recenterHeadTracker}.
     */
    public void resetYaw() {
        startFromSensorTransformation = null;
    }

    /**
     * Stop the orientation sensor when the app is dismissed.
     */
    public void stopTrackingOrientation() {
        sensorManager.unregisterListener(phoneOrientationListener);
    }

    //传感器监听
    private class PhoneOrientationListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            SensorManager.getRotationMatrixFromVector(phoneInWorldSpaceMatrix, event.values);
            if (startFromSensorTransformation == null) {
                // Android's hardware硬件 uses radians弧度, but OpenGL uses degrees角度. Android uses
                // [yaw偏度, pitch倾斜, roll滚动起伏] for the order of elements in the orientation array.
                float[] orientationRadians =
                        SensorManager.getOrientation(phoneInWorldSpaceMatrix, new float[3]);
                startFromSensorTransformation = new float[3];
                for (int i = 0; i < 3; ++i) {
                    startFromSensorTransformation[i] = (float) Math.toDegrees(orientationRadians[i]);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

    //渲染
    private class Renderer implements GLSurfaceView.Renderer{
        private FloatBuffer vertexBuffer;
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
        // 数组中每个顶点的坐标数
        static final int COORDS_PER_VERTEX = 3;
        //三角形的三个顶点坐标
        //OpenGLES 假定[0,0,0](X,Y,Z) 是GLSurfaceView 帧的中心
        float triangleCoords[] = { // 按逆时针方向顺序:
                0.0f,  0.622008459f, 0.0f,   // top
                -0.5f, -0.311004243f, 0.0f,   // bottom left
                0.5f, -0.311004243f, 0.0f    // bottom right
        };

        // 设置颜色，分别为red, green, blue 和alpha (opacity)
        float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

        private int mProgram;
        @Override
        public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {

        }

        @Override
        public void onSurfaceChanged(GL10 gl10, int i, int i1) {

        }

        @Override
        public void onDrawFrame(GL10 gl10) {

        }
    }
}