package com.gonsin.androidweb.samples;

import android.net.wifi.WifiManager;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.monday.androidweb.lib.BaseController;
import com.monday.androidweb.lib.GonsinWebServer;
import com.gonsin.androidweb.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private GonsinWebServer webServer;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String rootPath = Environment.getExternalStorageDirectory().getPath();
        String path = rootPath+"/GonsinWebServer/test/views";
        File file = new File(path);
        Log.d("path",path);
        if (!file.exists()) {
            file.mkdirs();
            Log.d("path","创建成功");
        }

        textView = (TextView)findViewById(R.id.textView);
        //获取当前wifi地址并显示
        setIpAccess();

        //开启
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(webServer == null){

                    String rootPath = Environment.getExternalStorageDirectory().getPath()
                            + File.separator + "ws";
                    Log.d("rootpath",rootPath);
                    //新建服务器，设定访问端口，web文件路径
                    webServer = new GonsinWebServer(8083, rootPath);


                    try {
                        // 注册Controller
                        List<BaseController> baseControllerList = new ArrayList<BaseController>();
                        baseControllerList.add(new TestController());
                        webServer.registerController(baseControllerList);

                        Log.d("rootpath","添加controller成功");
                        // 开启服务
                        webServer.start();
                        Log.d("rootpath","WebServer开启成功");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        //关闭
        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // 关闭服务
                if(webServer != null){
                    webServer.stop();
                    webServer = null;
                }
            }
        });
    }

    private void setIpAccess() {
        textView.setText(getIpAccess());
    }

    private String getIpAccess() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
        final String formatedIpAddress = String.format("%d.%d.%d.%d", (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        return "http://" + formatedIpAddress + ":";
    }
}
