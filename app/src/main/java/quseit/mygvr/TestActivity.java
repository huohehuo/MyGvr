package quseit.mygvr;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.google.vr.sdk.audio.GvrAudioEngine;
import com.google.vr.sdk.base.AndroidCompat;
import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;

public class TestActivity extends GvrActivity implements GvrView.StereoRenderer {
    protected float[] modelCube;
    protected float[] modelPosition;

    private static final String TAG = "BoxGame3DActivity";

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 0.01f;
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

    // Convenience vector for extracting the position from a matrix via multiplication.
    private static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

    private static final float MIN_MODEL_DISTANCE = 3.0f;
    private static final float MAX_MODEL_DISTANCE = 7.0f;

    private static final String OBJECT_SOUND_FILE = "cube_sound.wav";
    private static final String SUCCESS_SOUND_FILE = "success.wav";

    private final float[] lightPosInEyeSpace = new float[4];

    private FloatBuffer floorVertices;
    private FloatBuffer floorColors;
    private FloatBuffer floorNormals;

    private FloatBuffer cubeVertices;
    private FloatBuffer cubeColors;
    private FloatBuffer cubeFoundColors;
    private FloatBuffer cubeNormals;

//    private FloatBuffer cubeVertices2;
//    private FloatBuffer cubeColors2;
//    private FloatBuffer cubeFoundColors2;
//    private FloatBuffer cubeNormals2;

    private int cubeProgram;
    private int floorProgram;

    private int cubePositionParam;
    private int cubeNormalParam;
    private int cubeColorParam;
    private int cubeModelParam;
    private int cubeModelViewParam;
    private int cubeModelViewProjectionParam;
    private int cubeLightPosParam;

//    private int cubeProgram2;
//    private int floorProgram2;
//
//    private int cubePositionParam2;
//    private int cubeNormalParam2;
//    private int cubeColorParam2;
//    private int cubeModelParam2;
//    private int cubeModelViewParam2;
//    private int cubeModelViewProjectionParam2;
//    private int cubeLightPosParam2;

    private int floorPositionParam;
    private int floorNormalParam;
    private int floorColorParam;
    private int floorModelParam;
    private int floorModelViewParam;
    private int floorModelViewProjectionParam;
    private int floorLightPosParam;

    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private float[] tempPosition;
    private float[] headRotation;

    private float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    private float floorDepth = 20f;

    private Vibrator vibrator;
    private volatile int sourceId = GvrAudioEngine.INVALID_ID;
    private volatile int successSourceId = GvrAudioEngine.INVALID_ID;

    /**
     * Converts a raw text file, saved as a resource, into an OpenGL ES shader.
     *
     * @param type The type of shader we will be creating.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The shader object handler.
     * 根据raw文件中的代码，设置着色器
     */
    private int loadGLShader(int type, int resId) {
        String code = readRawTextFile(resId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, code);
        GLES20.glCompileShader(shader);

        // Get the compilation status.
        final int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

        // If the compilation failed, delete the shader.
        if (compileStatus[0] == 0) {
            Log.e(TAG, "Error compiling shader: " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }

        if (shader == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shader;
    }

    /**
     * 打印错误信息
     */
    private static void checkGLError(String label) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, label + ": glError " + error);
            throw new RuntimeException(label + ": glError " + error);
        }
    }

    /**
     * Sets the view to our GvrView and initializes the transformation matrices we will use
     * to render our scene.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initializeGvrView();

        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];
        tempPosition = new float[4];
        // Model first appears directly in front of user.
        modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};
        headRotation = new float[4];
        headView = new float[16];


        //震动
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

    }

    public void initializeGvrView() {
        setContentView(R.layout.activity_test);
        GvrView gvrView = (GvrView) findViewById(R.id.gvr_test);
        //设置颜色缓存为RGBA，位数都为8
        gvrView.setEGLConfigChooser(8, 8, 8, 8, 16, 8);

        /**设置渲染器**/
        gvrView.setRenderer(this);
        gvrView.setTransitionViewEnabled(false);//设置将手机放入盒子中的提示取消

        // Enable Cardboard-trigger feedback with Daydream headsets. This is a simple way of supporting
        // Daydream controller input for basic interactions using the existing Cardboard trigger API.
        /**使用Daydream耳机启用Cardboard触发反馈。
         * 这是一种使用现有Cardboard触发器API支持Daydream控制器输入进行基本交互的简单方法。**/
        gvrView.enableCardboardTriggerEmulation();

        if (gvrView.setAsyncReprojectionEnabled(true)) {
            // Async reprojection decouples the app framerate from the display framerate,
            // allowing immersive interaction even at the throttled clockrates set by
            // sustained performance mode.
            /**异步投影，沉浸式，性能模式**/
            AndroidCompat.setSustainedPerformanceMode(this, true);
        }

        setGvrView(gvrView);
    }

    @Override
    public void onRendererShutdown() {
        Log.e(TAG, "onRendererShutdown");
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.e(TAG, "onSurfaceChanged变化");
    }

    /**
     * Creates the buffers we use to store information about the 3D world.
     *
     * <p>OpenGL doesn't use Java arrays, but rather needs data in a format it can understand.
     * Hence we use ByteBuffers.
     *
     * @param config The EGL configuration used when creating the surface.
     */
    @Override
    public void onSurfaceCreated(EGLConfig config) {
        Log.e(TAG, "onSurfaceCreated创建");
        GLES20.glClearColor(0.1f, 0.1f, 0.1f, 0.5f); // Dark background so text shows up well.

        initOneBox();

//        initTwoBox();


        // make a floor底部地面布局
        ByteBuffer bbFloorVertices = ByteBuffer.allocateDirect(TestLayoutData.FLOOR_COORDS.length * 4);
        bbFloorVertices.order(ByteOrder.nativeOrder());
        floorVertices = bbFloorVertices.asFloatBuffer();
        floorVertices.put(TestLayoutData.FLOOR_COORDS);
        floorVertices.position(0);

        //正常状态的底部地面
        ByteBuffer bbFloorNormals = ByteBuffer.allocateDirect(TestLayoutData.FLOOR_NORMALS.length * 4);
        bbFloorNormals.order(ByteOrder.nativeOrder());
        floorNormals = bbFloorNormals.asFloatBuffer();
        floorNormals.put(TestLayoutData.FLOOR_NORMALS);
        floorNormals.position(0);

        //底部地面颜色设置
        ByteBuffer bbFloorColors = ByteBuffer.allocateDirect(TestLayoutData.FLOOR_COLORS.length * 4);
        bbFloorColors.order(ByteOrder.nativeOrder());
        floorColors = bbFloorColors.asFloatBuffer();
        floorColors.put(TestLayoutData.FLOOR_COLORS);
        floorColors.position(0);

        //提取shader文件中的代码
        int vertexShader = loadGLShader(GLES20.GL_VERTEX_SHADER, R.raw.test_light_vertex);
        int gridShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.test_grid_fragment);
        int passthroughShader = loadGLShader(GLES20.GL_FRAGMENT_SHADER, R.raw.test_passthrough_fragment);

        cubeProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(cubeProgram, vertexShader);
        GLES20.glAttachShader(cubeProgram, passthroughShader);
        GLES20.glLinkProgram(cubeProgram);
        GLES20.glUseProgram(cubeProgram);

        checkGLError("Cube program");

        //获取着色器程序中，指定为attribute类型变量的id。
        //获取指向着色器中aPosition的index
        cubePositionParam = GLES20.glGetAttribLocation(cubeProgram, "a_Position");
        cubeNormalParam = GLES20.glGetAttribLocation(cubeProgram, "a_Normal");
        cubeColorParam = GLES20.glGetAttribLocation(cubeProgram, "a_Color");


        //获取着色器程序中，指定为uniform类型变量的id。
        cubeModelParam = GLES20.glGetUniformLocation(cubeProgram, "u_Model");
        cubeModelViewParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVMatrix");
        cubeModelViewProjectionParam = GLES20.glGetUniformLocation(cubeProgram, "u_MVP");
        cubeLightPosParam = GLES20.glGetUniformLocation(cubeProgram, "u_LightPos");

//        cubeProgram2 = GLES20.glCreateProgram();
//        GLES20.glAttachShader(cubeProgram2, vertexShader);
//        GLES20.glAttachShader(cubeProgram2, passthroughShader);
//        GLES20.glLinkProgram(cubeProgram2);
//        GLES20.glUseProgram(cubeProgram2);
//
//
//        cubePositionParam2 = GLES20.glGetAttribLocation(cubeProgram2, "a_Position");
//        cubeNormalParam2 = GLES20.glGetAttribLocation(cubeProgram2, "a_Normal");
//        cubeColorParam2 = GLES20.glGetAttribLocation(cubeProgram2, "a_Color");
//
//        cubeModelParam2 = GLES20.glGetUniformLocation(cubeProgram2, "u_Model");
//        cubeModelViewParam2 = GLES20.glGetUniformLocation(cubeProgram2, "u_MVMatrix");
//        cubeModelViewProjectionParam2 = GLES20.glGetUniformLocation(cubeProgram2, "u_MVP");
//        cubeLightPosParam2 = GLES20.glGetUniformLocation(cubeProgram2, "u_LightPos");

        checkGLError("Cube program params");

        floorProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(floorProgram, vertexShader);
        GLES20.glAttachShader(floorProgram, gridShader);
        GLES20.glLinkProgram(floorProgram);
        GLES20.glUseProgram(floorProgram);

        checkGLError("Floor program");

        floorModelParam = GLES20.glGetUniformLocation(floorProgram, "u_Model");
        floorModelViewParam = GLES20.glGetUniformLocation(floorProgram, "u_MVMatrix");
        floorModelViewProjectionParam = GLES20.glGetUniformLocation(floorProgram, "u_MVP");
        floorLightPosParam = GLES20.glGetUniformLocation(floorProgram, "u_LightPos");

        floorPositionParam = GLES20.glGetAttribLocation(floorProgram, "a_Position");
        floorNormalParam = GLES20.glGetAttribLocation(floorProgram, "a_Normal");
        floorColorParam = GLES20.glGetAttribLocation(floorProgram, "a_Color");

        checkGLError("Floor program params");

        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.

        // Avoid any delays during start-up due to decoding of sound files.
//        new Thread(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // Start spatial audio playback of OBJECT_SOUND_FILE at the model position. The
//                        // returned sourceId handle is stored and allows for repositioning the sound object
//                        // whenever the cube position changes.
//                        gvrAudioEngine.preloadSoundFile(OBJECT_SOUND_FILE);
//                        sourceId = gvrAudioEngine.createSoundObject(OBJECT_SOUND_FILE);
//                        gvrAudioEngine.setSoundObjectPosition(
//                                sourceId, modelPosition[0], modelPosition[1], modelPosition[2]);
//                        gvrAudioEngine.playSound(sourceId, true /* looped playback */);
//                        // Preload an unspatialized sound to be played on a successful trigger on the cube.
//                        gvrAudioEngine.preloadSoundFile(SUCCESS_SOUND_FILE);
//                    }
//                })
//                .start();

        updateModelPosition();

        checkGLError("onSurfaceCreated");
    }
    private void initOneBox(){
        ByteBuffer bbVertices = ByteBuffer.allocateDirect(TestLayoutData.CUBE_COORDS.length * 4);
        bbVertices.order(ByteOrder.nativeOrder());
        cubeVertices = bbVertices.asFloatBuffer();
        cubeVertices.put(TestLayoutData.CUBE_COORDS);
        cubeVertices.position(0);

        //未处于视线中心的五颜六色方块
        ByteBuffer bbColors = ByteBuffer.allocateDirect(TestLayoutData.CUBE_COLORS.length * 4);
        bbColors.order(ByteOrder.nativeOrder());
        cubeColors = bbColors.asFloatBuffer();
        cubeColors.put(TestLayoutData.CUBE_COLORS);
        cubeColors.position(0);

        //处于视线中心时的方块颜色（全为yellow）
        ByteBuffer bbFoundColors =
                ByteBuffer.allocateDirect(TestLayoutData.CUBE_FOUND_COLORS.length * 4);
        bbFoundColors.order(ByteOrder.nativeOrder());
        cubeFoundColors = bbFoundColors.asFloatBuffer();
        cubeFoundColors.put(TestLayoutData.CUBE_FOUND_COLORS);
        cubeFoundColors.position(0);

        //正常状态下的正多面体
        ByteBuffer bbNormals = ByteBuffer.allocateDirect(TestLayoutData.CUBE_NORMALS.length * 4);
        bbNormals.order(ByteOrder.nativeOrder());
        cubeNormals = bbNormals.asFloatBuffer();
        cubeNormals.put(TestLayoutData.CUBE_NORMALS);
        cubeNormals.position(0);
    }

//    private void initTwoBox(){
//        ByteBuffer bbVertices2 = ByteBuffer.allocateDirect(TestLayoutData.CUBE_COORDS_TWO.length * 4);
//        bbVertices2.order(ByteOrder.nativeOrder());
//        cubeVertices2 = bbVertices2.asFloatBuffer();
//        cubeVertices2.put(TestLayoutData.CUBE_COORDS_TWO);
//        cubeVertices2.position(0);
//
//        //未处于视线中心的五颜六色方块
//        ByteBuffer bbColors2 = ByteBuffer.allocateDirect(TestLayoutData.CUBE_COLORS.length * 4);
//        bbColors2.order(ByteOrder.nativeOrder());
//        cubeColors2 = bbColors2.asFloatBuffer();
//        cubeColors2.put(TestLayoutData.CUBE_COLORS);
//        cubeColors2.position(0);
//
//        //处于视线中心时的方块颜色（全为yellow）
//        ByteBuffer bbFoundColors2 =
//                ByteBuffer.allocateDirect(TestLayoutData.CUBE_FOUND_COLORS.length * 4);
//        bbFoundColors2.order(ByteOrder.nativeOrder());
//        cubeFoundColors2 = bbFoundColors2.asFloatBuffer();
//        cubeFoundColors2.put(TestLayoutData.CUBE_FOUND_COLORS);
//        cubeFoundColors2.position(0);
//
//        //正常状态下的正多面体
//        ByteBuffer bbNormals2 = ByteBuffer.allocateDirect(TestLayoutData.CUBE_NORMALS.length * 4);
//        bbNormals2.order(ByteOrder.nativeOrder());
//        cubeNormals2 = bbNormals2.asFloatBuffer();
//        cubeNormals2.put(TestLayoutData.CUBE_NORMALS);
//        cubeNormals2.position(0);
//    }

    /**
     * Updates the cube model position.
     */
    protected void updateModelPosition() {
        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);

        // Update the sound location to match it with the new cube position.
//        if (sourceId != GvrAudioEngine.INVALID_ID) {
//            gvrAudioEngine.setSoundObjectPosition(
//                    sourceId, modelPosition[0], modelPosition[1], modelPosition[2]);
//        }
        checkGLError("updateCubePosition");
    }

    /**
     * Converts a raw text file into a string.
     * @param resId The resource ID of the raw text file about to be turned into a shader.
     * @return The context of the text file, or null in case of error.
     *读取raw文件中的代码，并返回
     */
    private String readRawTextFile(int resId) {
        InputStream inputStream = getResources().openRawResource(resId);
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Prepares OpenGL ES before we draw a frame.
     * 在绘制视图之前准备OpenGL ES。
     *
     * @param headTransform The head transformation in the new frame.
     */
    @Override
    public void onNewFrame(HeadTransform headTransform) {
//        setCubeRotation();

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);

        // Update the 3d audio engine with the most recent head rotation.
        headTransform.getQuaternion(headRotation, 0);
//        gvrAudioEngine.setHeadRotation(
//                headRotation[0], headRotation[1], headRotation[2], headRotation[3]);
//         Regular update call to GVR audio engine.
//        gvrAudioEngine.update();

        checkGLError("onReadyToDraw");
    }

    //设置立方体旋转矩阵
    protected void setCubeRotation() {
        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.5f, 0.5f, 1.0f);
    }

    /**
     * Draws a frame for an eye.
     *为我们的视野画每一帧图。
     * @param eye The eye to render. Includes all required transformations.
     */
    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        checkGLError("colorParam");

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawCube();
//        drawCubeTwo();

        // Set modelView for the floor, so we draw floor in the correct location
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        drawFloor();
    }

    @Override
    public void onFinishFrame(Viewport viewport) {}

    /**
     * Draw the cube.
     *绘制立方体
     * <p>We've set all of our transformation matrices. Now we simply pass them into the shader.
     */
    public void drawCube() {
        //使用shader程序
        GLES20.glUseProgram(cubeProgram);

        GLES20.glUniform3fv(cubeLightPosParam, 1, lightPosInEyeSpace, 0);

        // Set the Model in the shader, used to calculate lighting
        //// 将最终变换矩阵传入shader程序
        GLES20.glUniformMatrix4fv(cubeModelParam, 1, false, modelCube, 0);

        // Set the ModelView in the shader, used to calculate lighting
        GLES20.glUniformMatrix4fv(cubeModelViewParam, 1, false, modelView, 0);

        // Set the position of the cube
        GLES20.glVertexAttribPointer(
                cubePositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);

        // Set the ModelViewProjection matrix in the shader.
        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam, 1, false, modelViewProjection, 0);

        // Set the normal positions of the cube, again for shading
        GLES20.glVertexAttribPointer(cubeNormalParam, 3, GLES20.GL_FLOAT, false, 0, cubeNormals);
        GLES20.glVertexAttribPointer(cubeColorParam, 4, GLES20.GL_FLOAT, false, 0,
                isLookingAtObject() ? cubeFoundColors : cubeColors);

        // Enable vertex arrays
        GLES20.glEnableVertexAttribArray(cubePositionParam);
        GLES20.glEnableVertexAttribArray(cubeNormalParam);
        GLES20.glEnableVertexAttribArray(cubeColorParam);
        // 图形绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);

        // Disable vertex arrays
        GLES20.glDisableVertexAttribArray(cubePositionParam);
        GLES20.glDisableVertexAttribArray(cubeNormalParam);
        GLES20.glDisableVertexAttribArray(cubeColorParam);

        checkGLError("Drawing cube");
    }

//    public void drawCubeTwo() {
//        //使用shader程序
//        GLES20.glUseProgram(cubeProgram2);
//
//        GLES20.glUniform3fv(cubeLightPosParam2, 1, lightPosInEyeSpace, 0);
//
//        // Set the Model in the shader, used to calculate lighting
//        //// 将最终变换矩阵传入shader程序
//        GLES20.glUniformMatrix4fv(cubeModelParam2, 1, false, modelCube, 0);
//
//        // Set the ModelView in the shader, used to calculate lighting
//        GLES20.glUniformMatrix4fv(cubeModelViewParam2, 1, false, modelView, 0);
//
//        // Set the position of the cube
//        GLES20.glVertexAttribPointer(
//                cubePositionParam2, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, cubeVertices);
//
//        // Set the ModelViewProjection matrix in the shader.
//        GLES20.glUniformMatrix4fv(cubeModelViewProjectionParam2, 1, false, modelViewProjection, 0);
//
//        // Set the normal positions of the cube, again for shading
//        GLES20.glVertexAttribPointer(cubeNormalParam2, 3, GLES20.GL_FLOAT, false, 0, cubeNormals2);
//        GLES20.glVertexAttribPointer(cubeColorParam2, 4, GLES20.GL_FLOAT, false, 0,
//                isLookingAtObject() ? cubeFoundColors2 : cubeColors2);
//
//        // Enable vertex arrays
//        GLES20.glEnableVertexAttribArray(cubePositionParam2);
//        GLES20.glEnableVertexAttribArray(cubeNormalParam2);
//        GLES20.glEnableVertexAttribArray(cubeColorParam2);
//        // 图形绘制
//        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 36);
//
//        // Disable vertex arrays
//        GLES20.glDisableVertexAttribArray(cubePositionParam2);
//        GLES20.glDisableVertexAttribArray(cubeNormalParam2);
//        GLES20.glDisableVertexAttribArray(cubeColorParam2);
//
//        checkGLError("Drawing cube");
//    }

    /**
     * Draw the floor.
     * 画地板
     * <p>This feeds in data for the floor into the shader. Note that this doesn't feed in data about
     * position of the light, so if we rewrite our code to draw the floor first, the lighting might
     * look strange.
     */
    public void drawFloor() {
        GLES20.glUseProgram(floorProgram);

        // Set ModelView, MVP, position, normals, and color.
        GLES20.glUniform3fv(floorLightPosParam, 1, lightPosInEyeSpace, 0);
        GLES20.glUniformMatrix4fv(floorModelParam, 1, false, modelFloor, 0);
        GLES20.glUniformMatrix4fv(floorModelViewParam, 1, false, modelView, 0);
        GLES20.glUniformMatrix4fv(floorModelViewProjectionParam, 1, false, modelViewProjection, 0);
        GLES20.glVertexAttribPointer(
                floorPositionParam, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, floorVertices);
        GLES20.glVertexAttribPointer(floorNormalParam, 3, GLES20.GL_FLOAT, false, 0, floorNormals);
        GLES20.glVertexAttribPointer(floorColorParam, 4, GLES20.GL_FLOAT, false, 0, floorColors);

        GLES20.glEnableVertexAttribArray(floorPositionParam);
        GLES20.glEnableVertexAttribArray(floorNormalParam);
        GLES20.glEnableVertexAttribArray(floorColorParam);

        //绘制面 GLES20.TRIANGLES
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 24);

        GLES20.glDisableVertexAttribArray(floorPositionParam);
        GLES20.glDisableVertexAttribArray(floorNormalParam);
        GLES20.glDisableVertexAttribArray(floorColorParam);

        checkGLError("drawing floor");
    }

    /**当点击或拉动Cardboard触发器时调用。
     * Called when the Cardboard trigger is pulled.
     */
    @Override
    public void onCardboardTrigger() {
        Log.e(TAG, "onCardboardTrigger：点击屏幕");

        if (isLookingAtObject()) {
            Log.e(TAG, "onCardboardTrigger:...看到我了！");
            VideoVRActivity.startVideo(TestActivity.this);
//            successSourceId = gvrAudioEngine.createStereoSound(SUCCESS_SOUND_FILE);
//            gvrAudioEngine.playSound(successSourceId, false /* looping disabled */);
//            hideObject();
        }else{
            Log.e(TAG, "onCardboardTrigger:没看到我");
        }

        // Always give user feedback.
        vibrator.vibrate(50);//震动回馈
    }

    /**
     * Find a new random position for the object.
     *
     * <p>We'll rotate it around the Y-axis so it's out of sight, and then up or down by a little bit.
     *
     * 方法作用：隐藏物体即为对象找到一个新的随机位置。
     * <p>
     * 方法说明：我们将围绕Y轴旋转它，使它看不见，然后向上或向下一点点
     */
    protected void hideObject() {
        float[] rotationMatrix = new float[16];
        float[] posVec = new float[4];

        // First rotate in XZ plane, between 90 and 270 deg away, and scale so that we vary
        // the object's distance from the user.
        float angleXZ = (float) Math.random() * 180 + 90;
        Matrix.setRotateM(rotationMatrix, 0, angleXZ, 0f, 1f, 0f);
        float oldObjectDistance = objectDistance;
        objectDistance =
                (float) Math.random() * (MAX_MODEL_DISTANCE - MIN_MODEL_DISTANCE) + MIN_MODEL_DISTANCE;
        float objectScalingFactor = objectDistance / oldObjectDistance;
        //物体按坐标缩放比例缩放
        Matrix.scaleM(rotationMatrix, 0, objectScalingFactor, objectScalingFactor, objectScalingFactor);
        Matrix.multiplyMV(posVec, 0, rotationMatrix, 0, modelCube, 12);

        float angleY = (float) Math.random() * 80 - 40; // Angle in Y plane, between -40 and 40.
        angleY = (float) Math.toRadians(angleY);
        float newY = (float) Math.tan(angleY) * objectDistance;

        modelPosition[0] = posVec[0];
        modelPosition[1] = newY;
        modelPosition[2] = posVec[2];

        updateModelPosition();
    }

    /**通过计算对象在眼睛空间中的位置来检查用户是否正在查看对象。
     * Check if user is looking at object by calculating where the object is in eye-space.
     *如果用户正在查看对象，则为true
     * @return true if the user is looking at the object.
     */
    private boolean isLookingAtObject() {
        // Convert object space to camera space. Use the headView from onNewFrame.
        Matrix.multiplyMM(modelView, 0, headView, 0, modelCube, 0);
        Matrix.multiplyMV(tempPosition, 0, modelView, 0, POS_MATRIX_MULTIPLY_VEC, 0);

        float pitch = (float) Math.atan2(tempPosition[1], -tempPosition[2]);
        float yaw = (float) Math.atan2(tempPosition[0], -tempPosition[2]);

        return Math.abs(pitch) < PITCH_LIMIT && Math.abs(yaw) < YAW_LIMIT;
    }
}
