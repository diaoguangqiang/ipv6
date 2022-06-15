package com.example.gebilaohu.ludengctr;


import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

//import com.example.socket_demo.R;
import android.widget.Toast;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;

public class MainActivity extends Activity {

    MediaPlayer mMediaPlayer;

    private TextView textmq2,texttemp,texthumi,textfyj,texthgj,textnwkstate,textalarmstate;
    private Button btnNetwork,btncancel,btnsettemp,btnsethumi,btnsetmq2;
    private EditText editiptext,editporttext,edittemptext,edithumitext,editmq2text;

    //消息定义
    static final int RX_DATA_UPDATE_UI = 1;
    final int TX_DATA_UPDATE_UI = 2;
    static final int TIPS_UPDATE_UI = 3;

    public static Handler mainHandler;
    private ClientThread clientThread = null;
    private Timer mainTimer;


    byte SendBuf[] = { 0x3A, 0x00, 0x01, 0x0A, 0x00, 0x00, 0x23, 0x00 };
    private Message MainMsg;

    final int WRITE_TEST = 1;
    final int SEND_REGIST = 2;
    final int SEND_TEMP_EDIT_CONTENT = 3;
    final int SEND_HUMI_EDIT_CONTENT = 4;
    final int SEND_MQ2_EDIT_CONTENT = 5;

    int timeoutcnt=0;

    int btnstate = 0;//0是空闲状态，1是预约状态，2是暂离状态

    static String recv;

    int alarmflag = 0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("MainActivity", "onCreate execute");

        editiptext = (EditText) findViewById(R.id.edit_ip_text);
        editporttext = (EditText) findViewById(R.id.edit_port_text);

        edittemptext = (EditText) findViewById(R.id.edit_temp_alarm_text);
        edithumitext = (EditText) findViewById(R.id.edit_humi_alarm_text);
        editmq2text = (EditText) findViewById(R.id.edit_mq2_alarm_text);


        Log.d("MainActivity", "onCreate execute");

        textmq2 = (TextView) findViwBeyId(R.id.text_mq2_view);
        texttemp = (TextView) findViewById(R.id.text_temp_view);
        texthumi = (TextView) findViewById(R.id.text_humi_view);
        textalarmstate = (TextView) findViewById(R.id.text_alarm_state_view);



        textnwkstate = (TextView) findViewById(R.id.text_nwk_state_view);

        btnNetwork = (Button) findViewById(R.id.button_nkw_cnt);
        btncancel = (Button) findViewById(R.id.button_nkw_cancel);

        btnsettemp = (Button)
                findViewById(R.id.button_set_temp_cnt);
        btnsethumi = (Button) findViewById(R.id.button_set_humi_cnt);
        btnsetmq2 = (Button) findViewById(R.id.button_set_mq2_cnt);




        editiptext.setText("192.168.4.1");
        editporttext.setText("9000");




        btnNetwork.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String strIpAddr = editiptext.getText().toString();
                int port = Integer.parseInt(editporttext.getText().toString());
                //int port = 33333;
                //clientThread = new ClientThread("192.168.1.104",33333);//建立客户端线程
                clientThread = new ClientThread(strIpAddr,port);//建立客户端线程
                clientThread.start();


                //mainTimer = new Timer();//定时查询所有终端信息
                //setTimerTask();


//				String strport = editporttext.getText().toString();
//				Toast.makeText(MainActivity.this, "ip:" + strIpAddr + " " + "port:" + strport,
//						Toast.LENGTH_SHORT).show();
            }
        });
        btncancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clientThread != null) {
                    MainMsg = ClientThread.childHandler
                            .obtainMessage(ClientThread.RX_EXIT);
                    ClientThread.childHandler.sendMessage(MainMsg);
                    textnwkstate.setText("断开网络");

                }
            }
        });

        btnsettemp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMsg = mainHandler.obtainMessage(TX_DATA_UPDATE_UI,
                        SEND_TEMP_EDIT_CONTENT, 1);
                mainHandler.sendMessage(MainMsg);
            }
        });
        btnsethumi.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMsg = mainHandler.obtainMessage(TX_DATA_UPDATE_UI,
                        SEND_HUMI_EDIT_CONTENT, 1);
                mainHandler.sendMessage(MainMsg);
            }
        });
        btnsetmq2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainMsg = mainHandler.obtainMessage(TX_DATA_UPDATE_UI,
                        SEND_MQ2_EDIT_CONTENT, 1);
                mainHandler.sendMessage(MainMsg);
            }
        });
        initMainHandler();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //开始播放
    public void playRing(final Activity activity){
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);//用于获取手机默认铃声的Uri
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(activity, alert);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);//告诉mediaPlayer播放的是铃声流
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //停止播放
    public void stopRing(){
        if (mMediaPlayer!=null){
            if (mMediaPlayer.isPlaying()){
                mMediaPlayer.stop();
                //mMediaPlayer.release();
            }
        }
    }

    void SendData(String str) {
        MainMsg = ClientThread.childHandler.obtainMessage(ClientThread.TX_DATA,
                str.length(), 0, (Object) str);
        ClientThread.childHandler.sendMessage(MainMsg);
    }
    void initMainHandler() {
        mainHandler = new Handler() {

            //主线程消息处理中心
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RX_DATA_UPDATE_UI:

                        String lrecv = recv;
                        int index = lrecv.indexOf("mq2");
                        if( index != -1)
                        {
                            index = lrecv.indexOf("=");
                            int index1 = lrecv.indexOf(",");
                            if(index1 != -1)
                            {
                                textmq2.setText("烟雾浓度:"+lrecv.substring(index+1, index1)+"%");
                            }

                            String lrecv1 = lrecv.substring(index1+1);
                            index = lrecv1.indexOf("temp");
                            if(index != -1)
                            {
                                index = lrecv1.indexOf("=");
                                index1 = lrecv1.indexOf(",");
                                if(index1 != -1 && index != -1)
                                {
                                    texttemp.setText("温度:"+lrecv1.substring(index+1, index1)+"度");
                                }
                            }

                            String lrecv2 = lrecv1.substring(index1+1);
                            index = lrecv2.indexOf("humi");
                            if(index != -1)
                            {
                                index = lrecv2.indexOf("=");
                                index1 = lrecv2.indexOf(",");
                                if(index1 != -1)
                                {

                                    texthumi.setText("湿度:"+lrecv2.substring(index+1, index1)+"%");
                                }
                            }

                            String lrecv3 = lrecv2.substring(index1+1);
                            index = lrecv3.indexOf("alarmflag");
                            if(index != -1)
                            {
                                index = lrecv3.indexOf("=");
                                index1 = lrecv3.indexOf(",");
                                if(index1 != -1)
                                {
                                    int flag = lrecv3.substring(index+1, index1).indexOf("1");
                                    if(flag != -1)
                                    {

                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("温度上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("2") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("湿度上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("3") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("烟雾上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("4") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("温湿度上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("5") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("温度烟雾上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("6") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("湿度烟雾上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else if(lrecv3.substring(index+1, index1).indexOf("7") != -1)
                                    {
                                        if(alarmflag == 0)
                                            playRing(MainActivity.this);
                                        textalarmstate.setText("温度湿度烟雾上限报警");
                                        if(alarmflag == 0)
                                            alarmflag = 1;
                                    }
                                    else
                                    {
                                        stopRing();
                                        textalarmstate.setText("正常状态");
                                        alarmflag = 0;
                                    }

                                }
                            }




                        }
                        else
                        {
                            Toast.makeText(MainActivity.this, "收到内容:" + recv,
                                    Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case TX_DATA_UPDATE_UI: //msg.arg1保存功能码 arg2保存终端地址
                        switch (msg.arg1) {
                            case WRITE_TEST:
                                SendData("Hello android socket demo!\r\n");
                                break;
                            case SEND_REGIST:
                                SendData("ep=B9VAMS666GX6KXSK&pw=123456");
                                break;
                            case SEND_TEMP_EDIT_CONTENT:
                                SendData("th:"+edittemptext.getText().toString());
                                break;
                            case SEND_HUMI_EDIT_CONTENT:
                                SendData("hh:"+edithumitext.getText().toString());
                                break;
                            case SEND_MQ2_EDIT_CONTENT:
                                SendData("mh:"+editmq2text.getText().toString());
                                break;



                            default:
                                break;
                        }
                        break;
                    case TIPS_UPDATE_UI:
                        String str = (String) msg.obj;
                        textnwkstate.setText(str);
                        break;
                }
                super.handleMessage(msg);
            }
        };
    }


}
