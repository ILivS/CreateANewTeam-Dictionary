package main;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public  class IsConnected {
    public  static  boolean IsConnecting(int timeout) throws InterruptedException, IOException {
            Socket sock = new Socket();
            InetSocketAddress addr = new InetSocketAddress("www.google.com",80);

            try
            {
                sock.connect(addr,timeout);

                return true;
            }
            catch (Exception e)
            {

                return false;
            }
            finally
            {
                try
                {
                    sock.close();
                }
                catch (Exception e)
                {

                }
            }

    }



}
