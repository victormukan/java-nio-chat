package chat;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer implements Runnable {
    private List<ChatServerThread> clients;//[] = new ChatServerThread[50];
    private ServerSocket server;
    private Thread thread;

    public ChatServer(int port) {
        try {
            System.out.println("Binding to port " + port + ", please wait  ...");
            server = new ServerSocket(port);
            System.out.println("Server started: " + server);
            start();
            clients = new ArrayList<>();
        } catch(IOException ioe) {
            System.out.println("Can not bind to port " + port + ": " + ioe.getMessage());
        }
    }
    public static void main(String args[]) {
        ChatServer server = new ChatServer(4000);
    }

    public void run() {
        while (thread != null) {
            try {
                System.out.println("Waiting for a client ...");
                addThread(server.accept());
            } catch(IOException ioe) {
                System.out.println("Server accept error: " + ioe);
                stop();
            }
        }
    }

    private ChatServerThread findClient(int ID) {
        for (ChatServerThread client: clients) {
            if (client.getID() == ID) {
                return client;
            }
        }
        return null;
    }

    public synchronized void handle(int ID, String input) {
        if (input.equals(".bye")) {
            findClient(ID).send(".bye");
            remove(ID);
        } else {
            for (ChatServerThread client: clients) {
                client.send(ID + ": " + input);
            }
        }
    }
    public synchronized void remove(int ID) {
        ChatServerThread removed = findClient(ID);

        if (removed != null) {
            System.out.println("Removing client thread " + ID);

            try {
                removed.close();
            } catch(IOException ioe) {
                System.out.println("Error closing thread: " + ioe);
            }

            clients.remove(removed);
            removed.interrupt();
        }
    }
    private void addThread(Socket socket) {
        System.out.println("Client accepted: " + socket);
        ChatServerThread newClient = new ChatServerThread(this, socket);

        try {
            newClient.open();
            newClient.start();
        } catch(IOException ioe) {
            System.out.println("Error opening thread: " + ioe);
        }

        clients.add(newClient);
    }

    public void start() {
        if (thread == null) {
            thread = new Thread(this);
            thread.start();
        }
    }

    public void stop() {
        if (thread != null) {
            //thread.stop();
            thread.interrupt();
            thread = null;
        }
    }
}
