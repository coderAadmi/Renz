import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class ScreenServer{
    private ServerSocket server;
    private InetAddress ip;
    private int port;
    private int clientsConnected;
    private HashMap<InetAddress, Socket> clientTable;
    private boolean isRunning;

    public static String CLOSE_SERVER = "CLOSE_SERVER";
    public ScreenServer(int port) throws IOException {
        server = new ServerSocket(port);
        this.ip = server.getInetAddress();
        clientsConnected = 0;
        isRunning = true;
        clientTable = new HashMap<>();
        startServer();
    }

    private void startServer() throws IOException {
        while(isRunning)
        {
            Socket client = server.accept();
            client.setSendBufferSize(1024*1024);
            System.out.println("CLient connected");
            clientsConnected++;
//            clientTable.put(client.getInetAddress(),client);
            //call new ClientHandler() extending Thread
            Thread t = new Thread(new ClientHandler(client,client.getInetAddress()));
            t.start();
        }
    }

    public void close() throws Exception
    {
        server.close();
    }

    public static void main(String[] args)
    {
        ScreenServer server = null;
        try {
            server  = new ScreenServer(12345);
        } catch (IOException e) {
            e.printStackTrace();
            try {
                server.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}