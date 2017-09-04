package quseit.mygvr;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    TextView textView2;
    TextView textView3;
    private long total_data = TrafficStats.getTotalRxBytes();
    long nowdata;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        textView = (TextView) findViewById(R.id.tv_show);
//        textView2 = (TextView) findViewById(R.id.tv_show2);
//        textView3 = (TextView) findViewById(R.id.tv_show3);
//        textView.setText(total_data+"");



    }
    public void ShowOne(View view){
        startActivity(new Intent(this,Main2Activity.class));
    }
    public void ShowVideo(View view){
        startActivity(new Intent(this,Main3Activity.class));
    }
    public void ShowVR(View view){
        startActivity(new Intent(this,Main4Activity.class));
    }
    public void ShowVRC(View view){
        startActivity(new Intent(this,Main5Activity.class));
    }













    public void showFlow(View view){
        getTotalTxPackets();
//        getAppTrafficList();
    }

    public void getTotalTxPackets(){
        textView.setText("原来："+total_data);
        textView2.setText("现在："+nowdata);
        nowdata = TrafficStats.getTotalRxBytes();
        textView3.setText("消耗："+xiaohao()+" M");

    }

    private long xiaohao(){
        long result=nowdata-total_data;
        Log.e("流量：",result+"");
        if (result>0){
            return result/1024/1024;
        }
        return 0;
    }

    public void getAppTrafficList(){
        //获取所有的安装在手机上的应用软件的信息，并且获取这些软件里面的权限信息
        PackageManager pm=getPackageManager();//获取系统应用包管理
        //获取每个包内的androidmanifest.xml信息，它的权限等等
        List<PackageInfo> pinfos=pm.getInstalledPackages
                (PackageManager.GET_UNINSTALLED_PACKAGES | PackageManager.GET_PERMISSIONS);
        //遍历每个应用包信息
        for(PackageInfo info:pinfos){
            Log.e("bb",info.toString());
            //请求每个程序包对应的androidManifest.xml里面的权限
            String[] premissions=info.requestedPermissions;
            if(premissions!=null && premissions.length>0){
                //找出需要网络服务的应用程序
                for(String premission : premissions){
                    if("android.permission.INTERNET".equals(premission)){
                        //获取每个应用程序在操作系统内的进程id
                        int uId=info.applicationInfo.uid;
                        //如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
                        long rx= TrafficStats.getUidRxBytes(uId);
                        //如果返回-1，代表不支持使用该方法，注意必须是2.2以上的
                        long tx=TrafficStats.getUidTxBytes(uId);
                        if(rx<0 || tx<0){
                            Log.e("aaa","11111");
                            continue;
                        }else{
                            Log.e("aaa","??:"+info.applicationInfo.loadLabel(pm));

                            Log.e("aaa","2222:"+rx+"---"+tx);
                            Toast.makeText(this, info.applicationInfo.loadLabel(pm)+"消耗的流量--"+
                                    Formatter.formatFileSize(this, rx+tx), Toast.LENGTH_SHORT);
                            textView.setText(rx+"---"+tx+":"+rx+tx);
                        }
                            Log.e("aaa","333");
                    }
//                            Log.e("aaa","4444");
                }
            }
        }
    }
}
