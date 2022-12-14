package pvpsocket;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pedro
 */
public class Conexao {

    public Jogador receivePlayer(Socket socket) throws Exception {
        InputStream in;
        byte[] buffer = new byte[1024];
        Jogador jogador;

        try {
            in = socket.getInputStream();
            in.read(buffer);

            final ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            final ObjectInputStream ois = new ObjectInputStream(bais);
            Object returned = ois.readObject();
            jogador = (Jogador) returned;
            return jogador;

        } catch (IOException e) {
            System.out.println("Exceção no InputStream: " + e);
        }
        return null;
    }

    public void sendPlayer(Socket socket, Jogador jogador) throws Exception {

        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(jogador);
        final byte[] data = baos.toByteArray();

        OutputStream out;
        try {
            //socket.setSoTimeout(2000);
            out = socket.getOutputStream();
            out.write(data);
        } catch (IOException e) {
            System.out.println("Exceção no OutputStream: " + e);
        }
    }

    public String receiveCommand(Socket socket) throws Exception {
        InputStream in;
        int bt;
        byte bufferTxt[];
        String txt = "";
        bufferTxt = new byte[144];

        try {
            //socket.setSoTimeout(999999999);
            in = socket.getInputStream();
            bt = in.read(bufferTxt);
            if (bt > 0) {
                txt = new String(bufferTxt);
            }
        } catch (IOException e) {
            System.out.println("Exceção no InputStream: " + e);
        }
        return txt;
    }

    public void sendCommand(Socket socket, String txt) throws Exception {
        OutputStream out;

        try {
            out = socket.getOutputStream();
            out.write(txt.getBytes());
        } catch (IOException e) {
            System.out.println("Exceção no OutputStream: " + e);
        }
    }
}
