package p2p.android.telephony.connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import android.os.Handler;
import android.os.Message;

public class chatter extends Thread {
    static final int MESSAGE_READ=-1;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    public String result;
    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff,0,msg.arg1);
                    result=tempMsg;
                    break;
            }
            return true;
        }
    });

    public chatter(Socket skt)
    {
        socket=skt;
        try
        {
            inputStream=socket.getInputStream();
            outputStream=socket.getOutputStream();
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
            outputStream.write(bytes);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
