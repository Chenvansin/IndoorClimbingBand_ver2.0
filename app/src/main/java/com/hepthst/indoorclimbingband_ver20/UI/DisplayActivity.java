package com.hepthst.indoorclimbingband_ver20.UI;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hepthst.indoorclimbingband_ver20.Model.CalorieParser;
import com.hepthst.indoorclimbingband_ver20.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by hepthSt on 2017/10/13.
 * Update on 2017/10/27.
 */

public class DisplayActivity extends Activity {
    private ViewPager mViewPager;
    private ArrayList<View> pageView;
    private Button mTransfer;
    private Button mSend;
    private TextView mHeightTV;
    private TextView mIntroduceTV;
    private TextView mCalorie;
    private static final String Address = "172.20.10.7"; //Changed by Net Environment
    private static final String FILENAME = "Height.txt";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        mHeightTV = (TextView) findViewById(R.id.tvHeight_Detail);
        mIntroduceTV = (NsTextView)findViewById(R.id.Introduce);
        mIntroduceTV.setMovementMethod(new ScrollingMovementMethod());
        mCalorie = (TextView) findViewById(R.id.tvCalorie_Detail);

        //this part for PageView
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        LayoutInflater inflater = getLayoutInflater();
        View view1 = inflater.inflate(R.layout.page_1,null);
        View view2 = inflater.inflate(R.layout.page_2,null);
        View view3 = inflater.inflate(R.layout.page_3,null);

        pageView = new ArrayList<View>();
        pageView.add(view1);
        pageView.add(view2);
        pageView.add(view3);

        PagerAdapter mPageAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return pageView.size();
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                ((ViewPager) container).removeView(pageView.get(position));
            }

            @Override
            public Object instantiateItem(ViewGroup container, int position) {
                ((ViewPager)container).addView(pageView.get(position));
                return pageView.get(position);
            }
        };

        mViewPager.setAdapter(mPageAdapter);


        findViewById(R.id.BtnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(Environment.getExternalStorageDirectory(),FILENAME);
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    try {
                        FileInputStream inputStream = null;
                        inputStream = new FileInputStream(file);
                        byte[] b = new byte[inputStream.available()]; // this what I need
                        inputStream.read(b);
                        mHeightTV.setText(new String(b) + "m   ");
                        CalorieParser parser =  new CalorieParser((String) mHeightTV.getText());
                        mCalorie.setText(String.format("%.2f",parser.getCalorie()).toString() + "kCal");

                        Toast.makeText(DisplayActivity.this,"Data Updated ",Toast.LENGTH_LONG).show();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(DisplayActivity.this,"SDcard 不存在/此时无法读写",Toast.LENGTH_LONG).show();
                }
            }
        });


        findViewById(R.id.BtnTransfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SocketChannel socketChannel = null;
                        try{
                            socketChannel = SocketChannel.open();
                            SocketAddress socketAddress = new InetSocketAddress(Address,1991);
                            socketChannel.connect(socketAddress);
                            sendData(socketChannel,"filename");
                            String string = "";
                            string = receiveData(socketChannel);
                            if(!string.isEmpty()){
                                socketChannel = SocketChannel.open();
                                socketChannel.connect(new InetSocketAddress(Address,1991));
                                sendData(socketChannel,string);
                                receiveFile(socketChannel,new File("sdcard/"+string));
                            }else
                                Log.d(TAG, "file object is empty.");
                        }catch (IOException e) {
                            e.printStackTrace();
                        }finally {
                            try{
                                socketChannel.close();
                            }catch (Exception e){}
                        }
                    }
                }).start();
            }
        });
    }

    private void sendData(SocketChannel socketChannel, String stirng) throws IOException {
            byte[] bytes = stirng.getBytes();
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            socketChannel.write(buffer);
            socketChannel.socket().shutdownOutput();
        }

        private String receiveData(SocketChannel socketChannel) throws IOException {
            String string = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                byte[] bytes;
                int count = 0;
                while((count = socketChannel.read(buffer)) >= 0){
                    buffer.flip();
                    bytes = new byte[count];
                    buffer.get(bytes);
                    baos.write(bytes);
                    buffer.clear();
                }
                bytes = baos.toByteArray();
                string = new String(bytes);
            }finally{
                try {
                    baos.close();
                }catch (Exception e){}
            }
            return string;
        }


        private static void receiveFile(SocketChannel socketChannel, File file) throws IOException {
            FileOutputStream fos = null;
            FileChannel channel = null;

            try{
                fos = new FileOutputStream(file);
                channel = fos.getChannel();
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

                int size = 0;
                while((size = socketChannel.read(buffer)) != -1){
                    buffer.flip();
                    if (size > 0 ){
                        buffer.limit(size);
                        channel.write(buffer);
                        buffer.clear();
                    }
                }
            }finally {
                try{
                    channel.close();
                }catch (Exception e){}
                try{
                    fos.close();
                }catch (Exception e){}
            }
        }

}
