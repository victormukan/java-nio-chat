package chat;

import java.net.*;
import java.io.*;

public class ChatServerThread extends Thread {
    private ChatServer server;
    private Socket socket;
    private DataInputStream streamIn;
    private DataOutputStream streamOut;
    private int ID;

    public ChatServerThread(ChatServer server, Socket socket) {
        super();
        this.server = server;
        this.socket = socket;
        this.ID = socket.getPort();
    }

    public void send(String msg) {
        try {
            streamOut.writeUTF(msg);
            streamOut.flush();
        } catch(IOException ioe) {
            System.out.println(ID + " ERROR sending: " + ioe.getMessage());
            server.remove(ID);
            stop();
        }
    }

    public int getID() { return ID; }

    public void run() {
        System.out.println("Server Thread " + ID + " running.");

        while (!Thread.currentThread().isInterrupted()) {
            try {
                server.handle(ID, streamIn.readUTF());
            } catch(IOException ioe) {
                System.out.println(ID + " ERROR reading: " + ioe.getMessage());
                server.remove(ID);
                stop();
            }
        }
    }

    public void open() throws IOException {
        streamIn = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        streamOut = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void close() throws IOException {
        if (socket != null) {
            socket.close();
        }
        if (streamIn != null) {
            streamIn.close();
        }
        if (streamOut != null) {
            streamOut.close();
        }
    }
}
