package p2p.android.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
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
    public TextView readMsgBox,ConnectionStatus;
    private EditText showMsg;

    private WifiManager wifiManager;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;

    private BroadcastReceiver mReceiver;
    private IntentFilter mIntentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private String[] deviceArrayName;
    private WifiP2pDevice[] deviceArray;
    static final int MESSAGE_READ=-1;

    ServerClass serverClass;
    ClientCLass clientCLass;
    SendReceive sendReceive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        IntiateWork();
        exqListener();
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
                sendReceive.write(msg.getBytes());
            }
        });
    }

    public WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupownerAddress = info.groupOwnerAddress;
            if (info.groupFormed &&  info.isGroupOwner)
            {
                ConnectionStatus.setText("Host");
                serverClass=new ServerClass();
                serverClass.start();
            }
            else if (info.groupFormed)
            {
                ConnectionStatus.setText("Client");
                clientCLass=new ClientCLass(groupownerAddress);
                clientCLass.start();
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

    public class ServerClass extends Thread{
        Socket socket;
        ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket=new ServerSocket(8887);
                socket=serverSocket.accept();
                sendReceive=new SendReceive(socket);
                sendReceive.start();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally
            {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public class SendReceive extends Thread {
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
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
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        public void write(byte[] bytes)
        {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public class ClientCLass extends Thread {
        Socket socket;
        String hostAdd;
        public ClientCLass(InetAddress hostAddress)
        {
            hostAdd=hostAddress.getHostAddress();
            socket= new Socket();
            sendReceive=new SendReceive(socket);
            sendReceive.start();
        }

        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8887),500);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            finally
            {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
