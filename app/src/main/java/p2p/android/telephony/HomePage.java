package p2p.android.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import p2p.android.telephony.BroadCaster.WifiDirectBroadcastReceiver;


public class HomePage extends AppCompatActivity {
    private Button btnOffOn,btnDiscover,btnSend;
    private ListView deviceList;
    public static TextView readMsgBox,ConnectionStatus;
    private EditText showMsg,Nick;

    private  WifiManager wifiManager;
    private static WifiP2pManager mManager;
    private static WifiP2pManager.Channel mChannel;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private String[] deviceArrayName;
    private WifiP2pDevice[] deviceArray;

    private static clientClass cClass;
    private static serverClass sClass;
    private static chatter chatterClass;

    static final int MESSAGE_READ=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            //your codes here
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_home_page);
            IntiateWork();
            exqListener();

        }

    }
    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    readMsgBox.setText(tempMsg);
                    break;
            }
            return true;
        }
    });



    private void IntiateWork()
    {
        btnOffOn=(Button)findViewById(R.id.onOff);
        btnDiscover=(Button)findViewById(R.id.discover);
        btnSend=(Button)findViewById(R.id.sendButton);
        deviceList=(ListView)findViewById(R.id.peerListView);
        readMsgBox=(TextView) findViewById(R.id.readMsg);
        Nick=(EditText)findViewById(R.id.Nick);
        ConnectionStatus=(TextView)findViewById(R.id.connectionStatus);
        showMsg=(EditText)findViewById(R.id.writeMsg);
        wifiManager=(WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mManager=(WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager,mChannel,this);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        if (wifiManager.isWifiEnabled())
        {
            btnOffOn.setText("WIFI OFF");
        }
        else
        {
            btnOffOn.setText("WIFI ON");
        }
    }
    public WifiP2pManager.PeerListListener peerListListener=new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if (!peerList.getDeviceList().equals(peers))
            {
                peers.clear();
                peers.addAll(peerList.getDeviceList());
                deviceArrayName=new String[peerList.getDeviceList().size()];
                deviceArray=new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;

                for (WifiP2pDevice device: peerList.getDeviceList())
                {
                    deviceArrayName[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }
                ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceArrayName);
                deviceList.setAdapter(adapter);
            }
            if (peers.size()==0)
            {
                Toast.makeText(getApplicationContext(),"No Device found",Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };
    private void exqListener()
    {
        btnOffOn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                disconnect();

                if (wifiManager.isWifiEnabled())
                {
                    wifiManager.setWifiEnabled(false);
                    btnOffOn.setText("WIFI ON");
                }
                else
                {
                    wifiManager.setWifiEnabled(true);
                    btnOffOn.setText("WIFI OFF");
                }
            }
        });
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeDevName(Nick.getText().toString());
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess(){
                        ConnectionStatus.setText("Discovery Started");

                    }

                    @Override
                    public void onFailure(int i)
                    {
                        ConnectionStatus.setText("Discovery Starting Failed");
                    }
                });
            }
        });
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device=deviceArray[position];
                WifiP2pConfig config=new WifiP2pConfig();
                config.deviceAddress=device.deviceAddress;

                mManager.connect(mChannel,config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Connected to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reason) {
                        Toast.makeText(getApplicationContext(),"Failed Connection to "+device.deviceName,Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=showMsg.getText().toString();
                System.out.println("SENT MESSAGE IS");
                System.out.println(msg);
                chatterClass.write(msg.getBytes());
            }
        });
    }
    public static void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                if (cClass!=null) {
                                    cClass.endConnection();
                                    cClass=null;
                                }
                                if (sClass!=null)
                                {
                                    sClass.endConnection();
                                    sClass=null;
                                }
                                if (chatterClass!=null)
                                {
                                    chatterClass.close();
                                    //chatterClass=null;
                                }
                                ConnectionStatus.setText("Device is Disconnected");
                                //Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                ConnectionStatus.setText("Device is connected");
                                //Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }
    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupownerAddress = info.groupOwnerAddress;
            if (info.groupFormed &&  info.isGroupOwner)
            {
                ConnectionStatus.setText("Host");
                sClass=new serverClass();
                sClass.start();
            }
            else if (info.groupFormed)
            {
                ConnectionStatus.setText("Client");
                cClass=new clientClass(groupownerAddress);
                cClass.start();
            }
        }
    };
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    private class serverClass extends Thread {
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try
            {
                serverSocket=new ServerSocket(8888);
                socket=serverSocket.accept();
                //Chatting part
                chatterClass=new chatter(socket);
                chatterClass.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        public void endConnection()
        {
            try
            {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            socket=null;
            serverSocket=null;
        }
    }

    public class clientClass extends Thread {
        Socket socket;
        String hostAdd;

        public clientClass(InetAddress hostaddress) {
            hostAdd=hostaddress.getHostAddress();
            socket=new Socket();
        }

        @Override
        public void run() {
            try
            {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                //Chatting Part
                chatterClass=new chatter(socket);
                chatterClass.start();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        public void endConnection()
        {
            try
            {
                socket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            socket=null;
            hostAdd=null;
        }
    }

    public class chatter extends Thread {
        static final int MESSAGE_READ=-1;
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;


        public chatter(Socket skt)
        {
            socket=skt;
            try
            {
                inputStream=(InputStream)socket.getInputStream();
                outputStream=(OutputStream)socket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer=new byte[1024];
            int bytes;
            while (socket!=null)
            {
                try
                {
                    bytes=inputStream.read(buffer);
                    if (bytes>0)
                    {
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes)
        {
            try
            {
                if (outputStream==null)
                {
                    System.out.println("OUTPUTSTREAM IS NULL");
                }
                outputStream.write(bytes);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }


        public void close()
        {
            try
            {
                socket=null;
                outputStream.close();
                inputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
    public void changeDevName(final String input)
    {

        try {
            Class[] paramTypes = new Class[3];
            paramTypes[0] = WifiP2pManager.Channel.class;
            paramTypes[1] = String.class;
            paramTypes[2] = WifiP2pManager.ActionListener.class;
            Method setDeviceName = mManager.getClass().getMethod(
                    "setDeviceName", paramTypes);
            setDeviceName.setAccessible(true);

            Object arglist[] = new Object[3];
            arglist[0] = mChannel;
            arglist[1] = input;
            arglist[2] = new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(),"Device Name:"+input, Toast.LENGTH_SHORT);
                    //Log.d("setDeviceName succeeded", "true");
                }

                @Override
                public void onFailure(int reason) {
                    Toast.makeText(getApplicationContext(),"Failed to Change the Name", Toast.LENGTH_SHORT);
                    //Log.d("setDeviceName failed", "true");
                }
            };
            setDeviceName.invoke(mManager, arglist);

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
