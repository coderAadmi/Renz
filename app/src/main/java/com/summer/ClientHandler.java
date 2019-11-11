import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ClientHandler implements Runnable {
    private Socket client;
    private InetAddress ip;
    private boolean isRunning  = true;
    ByteArrayOutputStream baos;
    private ObjectOutputStream dout;

    public ClientHandler(Socket client, InetAddress ip) throws IOException {
        this.client = client;
        this.ip = ip;
       baos  = new ByteArrayOutputStream();
       dout = new ObjectOutputStream(client.getOutputStream());

    }

    @Override
    public void run() {
        while (isRunning)
        {
//            baos.reset();
            try {
                ImageIO.write( getScreen(), "jpg", baos );
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                dout.writeObject(imageInByte);
            } catch (Exception e) {
                isRunning = false;
                try {
                    client.shutdownOutput();
                    client.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }

    public void closeConnection()
    {
        isRunning = false;
    }

    private BufferedImage getScreen() throws AWTException {
        Rectangle rect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        return new Robot().createScreenCapture(rect);
    }
}
