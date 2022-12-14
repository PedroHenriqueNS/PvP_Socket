package pvpsocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 *
 * @author pedro
 */
public class Conexao {
    
    public Jogador receivePlayer(Socket socket) throws Exception {
        InputStream in;
        byte[] buffer = new byte[2048];
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
        OutputStream out;
        
        try {
            out = socket.getOutputStream();
            ObjectOutputStream objOut = new ObjectOutputStream(out);
            objOut.writeObject(jogador);
            objOut.flush();
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
