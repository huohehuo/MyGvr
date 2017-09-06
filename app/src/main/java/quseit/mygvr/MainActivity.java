package quseit.mygvr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import quseit.mygvr.opengl.OpenGLActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        handler.sendEmptyMessage(1);

    }
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            findViewById(R.id.button6).performClick();
        }
    };
    public void ShowOne(View view){
        startActivity(new Intent(this,PicARActivity.class));
    }
    public void ShowVideo(View view){
        startActivity(new Intent(this,VideoVRActivity.class));
    }
    public void ShowVR(View view){
        startActivity(new Intent(this,BoxGame3DActivity.class));
    }
    public void ShowVRC(View view){
        startActivity(new Intent(this,ShowVRActivity.class));
    }
    public void ShowOpenGL(View view){
        startActivity(new Intent(this,OpenGLActivity.class));
    }
    public void ShowBoxTest(View view){
        startActivity(new Intent(this,TestActivity.class));
    }
}
