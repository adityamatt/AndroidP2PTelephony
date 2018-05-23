package p2p.android.telephony.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class clientClass extends Thread {
    Socket socket;
    String hostAdd;
    chatter chatterClass;

    public clientClass(InetAddress hostaddress,chatter c) {
        chatterClass=c;
        hostAdd=hostaddress.getHostAddress();
        socket=new Socket();
    }

    @Override
    public void run() {
        try
        {
            socket.connect(new InetSocketAddress(hostAdd,8888),500);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
