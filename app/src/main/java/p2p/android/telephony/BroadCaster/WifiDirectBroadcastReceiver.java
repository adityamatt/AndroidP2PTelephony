package p2p.android.telephony.BroadCaster;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.widget.Toast;


import p2p.android.telephony.HomePage;

public class WifiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager mManger;
    private WifiP2pManager.Channel mChannel;
    private HomePage mActivity;

    public WifiDirectBroadcastReceiver(WifiP2pManager mManger, WifiP2pManager.Channel mChannel, HomePage mHomepage) {
        this.mManger = mManger;
        this.mChannel = mChannel;
        this.mActivity = mHomepage;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action=intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action))
        {
            // Check to see if wifi is enabled  and notify appropriate Activity;
            int state=intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE,-1);
            if (state==WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            {
                Toast.makeText(context,"Wifi is ON",Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(context,"Wifi is OFF",Toast.LENGTH_SHORT).show();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action))
        {
            //Call WifiP2PManager.requestPeers() to get a list of current peers
            if (mManger!=null)
            {
                mManger.requestPeers(mChannel,mActivity.peerListListener);
            }
        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
        {
            //Respond to new connection or disconnections
            if (mManger==null)
            {
                return;
            }
            NetworkInfo networkInfo=intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected())
            {
                mManger.requestConnectionInfo(mChannel,mActivity.connectionInfoListener);
            }
            else
            {
                mActivity.ConnectionStatus.setText("Device is Disconnected");
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action))
        {
            //Respond to the device's Wifi State changing
        }
    }

}
