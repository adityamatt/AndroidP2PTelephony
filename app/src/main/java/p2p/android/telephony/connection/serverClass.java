package p2p.android.telephony.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class serverClass extends Thread {
    Socket socket;
    ServerSocket serverSocket;

    @Override
    public void run() {
        try
        {
            serverSocket=new ServerSocket(8888);
            socket=serverSocket.accept();
            //Chatting part
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
