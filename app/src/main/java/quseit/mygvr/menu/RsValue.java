package quseit.mygvr.menu;

import java.nio.FloatBuffer;

/**
 * Created by Administrator on 2017/9/6.
 */

public class RsValue {

    public static float[] modelCube;
    public static float[] modelPosition;

    public static float[] modelCube2;
    public static float[] modelPosition2;


    public static final float Z_NEAR = 0.1f;
    public static final float Z_FAR = 100.0f;

    public static final float CAMERA_Z = 0.01f;
    public static final float TIME_DELTA = 1.8f;

    public static final float YAW_LIMIT = 0.12f;
    public static final float PITCH_LIMIT = 0.12f;

    public static final int COORDS_PER_VERTEX = 3;

    // We keep the light always position just above the user.
    public static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};

    // Convenience vector for extracting the position from a matrix via multiplication.
    public static final float[] POS_MATRIX_MULTIPLY_VEC = {0, 0, 0, 1.0f};

    public static final float MIN_MODEL_DISTANCE = 3.0f;
    public static final float MAX_MODEL_DISTANCE = 7.0f;

    public static final String OBJECT_SOUND_FILE = "cube_sound.wav";
    public static final String SUCCESS_SOUND_FILE = "success.wav";

    public static final float[] lightPosInEyeSpace = new float[4];
    //floor
    public static FloatBuffer floorVertices;
    public static FloatBuffer floorColors;
    public static FloatBuffer floorNormals;
    //box1
    public static FloatBuffer cubeVertices;
    public static FloatBuffer cubeColors;
    public static FloatBuffer cubeFoundColors;
    public static FloatBuffer cubeNormals;

    //box2
    public static FloatBuffer cubeVertices2;
    public static FloatBuffer cubeColors2;
    public static FloatBuffer cubeFoundColors2;
    public static FloatBuffer cubeNormals2;

    public static int cubeProgram;
    public static int floorProgram;

    public static int cubePositionParam;
    public static int cubeNormalParam;
    public static int cubeColorParam;
    public static int cubeModelParam;
    public static int cubeModelViewParam;
    public static int cubeModelViewProjectionParam;
    public static int cubeLightPosParam;

    public static int cubeProgram2;
    public static int floorProgram2;

    public static int cubePositionParam2;
    public static int cubeNormalParam2;
    public static int cubeColorParam2;
    public static int cubeModelParam2;
    public static int cubeModelViewParam2;
    public static int cubeModelViewProjectionParam2;
    public static int cubeLightPosParam2;

    public static int floorPositionParam;
    public static int floorNormalParam;
    public static int floorColorParam;
    public static int floorModelParam;
    public static int floorModelViewParam;
    public static int floorModelViewProjectionParam;
    public static int floorLightPosParam;

    public static float[] camera;
    public static float[] view;
    public static float[] headView;
    public static float[] modelViewProjection;
    public static float[] modelView;
    public static float[] modelFloor;

    public static float[] tempPosition;
    public static float[] headRotation;

    public static float objectDistance = MAX_MODEL_DISTANCE / 2.0f;
    public static float floorDepth = 20f;









}
