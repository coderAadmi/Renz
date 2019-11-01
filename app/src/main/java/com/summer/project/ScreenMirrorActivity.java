package com.summer.project;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ScreenMirrorActivity extends AppCompatActivity {

    Thread screenSharing;
    ImageView mScreen;

    ImageView mCursor;

    float dX,dY, x, y;
    int X,Y;

    boolean isRunning = true;
    String serverIp = "192.168.43.251";
    int port = 12345;

    String DEVICE_NAME = "POLOMAN";

    Bitmap screenImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_miror);
        mScreen = findViewById(R.id.image);

        Intent intent = getIntent();
        //String clientName = intent.getStringExtra("USER_NAME");
        serverIp = intent.getStringExtra("USER_IP");
        port = intent.getIntExtra("USER_PORT",12345);

        //initScreen();

        initTCPScreen();

        mCursor = findViewById(R.id.cursor);
        mCursor.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
//                Log.d("DRAG_CUR",event.toString());
//                return false;

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        dX = view.getX() - event.getRawX();
                        dY = view.getY() - event.getRawY();
                        Log.d("DRAG_CUR","DOWN : "+view.getX()+"  :  "+view.getY());
                        break;

                    case MotionEvent.ACTION_MOVE:
                        Log.d("DRAG_CUR","MOVED : "+(event.getRawX() + dX)+"  : "+ (event.getRawY() + dY));
                        x = event.getRawX() + dX;
                        y = event.getRawY() + dY;
                        view.animate()
                                .x(x)
                                .y(y)
                                .setDuration(0)
                                .start();
                        break;
                    default:
                        Log.d("DRAG_CUR","DEFAULT");
                        return false;
                }
                return true;
            }
        });

        Thread mouse = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    DatagramSocket client = new DatagramSocket();
                    byte[] s = new byte[1024];
                    s = "Poloman".getBytes();
                    InetAddress ip = InetAddress.getByName(serverIp);
                    DatagramPacket packet = new DatagramPacket(s,s.length,ip,13345);
                    client.send(packet);
                    String so = "1920 1080 $";
                    byte[] o = new byte[so.getBytes().length];
                    while(true)
                    {
                        DatagramPacket pocket = new DatagramPacket(o,o.length);
                        client.receive(pocket);
                        String msg = new String(pocket.getData());
                        String m = "";
                        for(int i=0;i<msg.length();i++)
                        {
                            if(msg.charAt(i) == '$')
                                break;
                            m += msg.charAt(i);
                        }
                        String[] list = m.split(" ");
                        X = Integer.parseInt(list[0]);
                        Y= Integer.parseInt(list[1]);
                        mCursor.post(new Runnable() {
                            @Override
                            public void run() {
                                mCursor.setX(X);
                                mCursor.setY(Y);
                            }
                        });
                        Log.d("MOUSE_RE",m);
                    }
                }
                catch (Exception e)
                {
                    Log.d("MOUSE_RE",e.getMessage());
                }
            }
        });
//        mouse.start();

    }

    private void initScreen()
    {
        screenSharing = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    isRunning = true;
                    DatagramSocket client = new DatagramSocket();
                    byte[] name  = DEVICE_NAME.getBytes();
                    DatagramPacket packet = new DatagramPacket(name,name.length, InetAddress.getByName(serverIp),port);
                    client.send(packet);
                    byte[] len = new byte[64];
                    byte[] img = null;
                    DatagramPacket lenPacket = null;
                    DatagramPacket imgPacket = null;

                    while (isRunning)
                    {
                        lenPacket = new DatagramPacket(len, len.length);
                        client.receive(lenPacket);
                        String l = new String(lenPacket.getData());
                        int length = Integer.parseInt(l.split(" ")[0]);
                        Log.d("SCREEN_LEN",length+"");
                        img = new byte[length];
                        imgPacket = new DatagramPacket(img, img.length);
                        client.receive(imgPacket);
                        img = imgPacket.getData();
                        screenImg = BitmapFactory.decodeByteArray(img,0,length);
                        mScreen.post(new Runnable() {
                            @Override
                            public void run() {
                                mScreen.setImageBitmap(screenImg);
                            }
                        });
                    }
                    client.close();
                }
                catch (Exception e)
                {
                    Log.d("SCREEN_OUTER_THREAD",e.getMessage());
                }
            }
        });
        screenSharing.start();
    }

    private void initTCPScreen()
    {
        screenSharing = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    ObjectInputStream oin;
                    Socket socket;
                    byte[] imageInBytes;
                    while(isRunning)
                    {
                         socket = new Socket(serverIp,port);
                        oin  = new ObjectInputStream(socket.getInputStream());
                        imageInBytes = (byte[]) oin.readObject();
                        screenImg = BitmapFactory.decodeByteArray(imageInBytes,0,imageInBytes.length);
                        mScreen.post(new Runnable() {
                            @Override
                            public void run() {
                                mScreen.setImageBitmap(screenImg);
                            }
                        });
                    }

                }
                catch (Exception e)
                {
                    Log.d("SCREEN_E",e.getMessage());
                }
            }
        });
        screenSharing.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

}
