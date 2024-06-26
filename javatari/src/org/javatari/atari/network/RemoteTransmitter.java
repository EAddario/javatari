// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.parameters.Parameters;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;


public final class RemoteTransmitter {

    private static final int MAX_UPDATES_PENDING = Parameters.SERVER_MAX_UPDATES_PENDING;
    private final ConcurrentLinkedQueue<ServerUpdate> updates;
    private final List<ConnectionStatusListener> connectionListeners = new ArrayList<>();
    private boolean started = false;
    private ServerSocket serverSocket;
    private Socket socket;
    private int port = Parameters.SERVER_SERVICE_PORT;
    private ServerConsole console;
    private UpdatesSender updatesSender;
    private OutputStream socketOutputStream;
    private InputStream socketInputStream;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public RemoteTransmitter() {
        updates = new ConcurrentLinkedQueue<>();
    }

    public void start() throws IOException {
        start(port);    // Last port used or default port
    }

    public void start(int port) throws IOException {
        // Open the serverSocket the first time to get errors early here
        this.port = port;
        serverSocket = new ServerSocket(port);
        updatesSender = new UpdatesSender();
        started = true;
        updatesSender.start();
    }

    public void stop() throws IOException {
        started = false;
        // Stop listening serverSocket if needed
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        if (socket != null && !socket.isClosed()) socket.close();
        UpdatesSender sender = updatesSender;
        if (sender == null) return;
        sender.interrupt();    // Will stop the sender loop
        try {
            sender.join();        // Wait for stop to complete
        } catch (InterruptedException e) {
            // No problem
        }
    }

    public boolean isStarted() {
        return started;
    }

    public int port() {
        return port;
    }

    void serverConsole(ServerConsole console) {
        this.console = console;
    }

    void sendUpdate(ServerUpdate update) {
        if (outputStream == null) return;
        synchronized (updates) {
            while (updates.size() > MAX_UPDATES_PENDING) {
                try {
                    updates.wait();
                } catch (InterruptedException ignored) {
                }
            }
            updates.add(update);
            updates.notifyAll();
        }
    }

    public boolean isClientConnected() {
        return outputStream != null;
    }

    public void addConnectionStatusListener(ConnectionStatusListener lis) {
        if (!connectionListeners.contains(lis)) connectionListeners.add(lis);
    }

    private void listen() throws IOException {
        // Reopen the serverSocked if needed (2nd client connection and so on)
        if (serverSocket == null || serverSocket.isClosed())
            serverSocket = new ServerSocket(port);
        Socket conn = serverSocket.accept();
        serverSocket.close();
        connect(conn);
    }

    private void connect(Socket toSocket) throws IOException {
        socket = toSocket;
        socket.setTcpNoDelay(true);
        socketOutputStream = socket.getOutputStream();
        outputStream = new ObjectOutputStream(socketOutputStream);
        socketInputStream = socket.getInputStream();
        inputStream = new ObjectInputStream(socketInputStream);
        resetUpdatesPending();
        console.clientConnected();
        notifyConnectionStatusListeners();
    }

    private void disconnect() {
        boolean wasConnected = outputStream != null;
        cleanStreamsSilently();
        resetUpdatesPending();
        if (wasConnected) {
            console.clientDisconnected();
            notifyConnectionStatusListeners();
        }
    }

    private void cleanStreamsSilently() {
        try {
            socket.close();
        } catch (Exception ignored) {
        }
        try {
            socketOutputStream.close();
        } catch (Exception ignored) {
        }
        try {
            socketInputStream.close();
        } catch (Exception ignored) {
        }
        socket = null;
        socketOutputStream = null;
        outputStream = null;
        socketInputStream = null;
        inputStream = null;
    }

    private void resetUpdatesPending() {
        synchronized (updates) {
            updates.clear();
            updates.notifyAll();
        }
    }

    private void notifyConnectionStatusListeners() {
        for (ConnectionStatusListener lis : connectionListeners)
            lis.connectionStatusChanged();
    }

    private final class UpdatesSender extends Thread {
        UpdatesSender() {
            super("RemoteTransmitter Sender");
        }

        @Override
        public void run() {
            try {
                while (started) {
                    listen();
                    try {
                        ServerUpdate update;
                        while (started) {
                            synchronized (updates) {
                                while ((update = updates.poll()) == null)
                                    updates.wait();
                                updates.notifyAll();
                            }
                            if (started && update != null) {
                                synchronized (outputStream) {
                                    outputStream.writeObject(update);
                                    outputStream.flush();
                                    socketOutputStream.flush();
                                    @SuppressWarnings("unchecked")
                                    List<ControlChange> clientControlChanges =
                                            (List<ControlChange>) inputStream.readObject();
                                    if (clientControlChanges != null)
                                        console.receiveClientControlChanges(clientControlChanges);
                                }
                            }
                        }
                    } catch (Exception ignored) {
                    }
                    // Exception while connected or interrupted. Try to disconnect
                    disconnect();
                }
            } catch (Exception ex) {
                // Exception while listening or connecting or interrupted. Try to disconnect
                disconnect();
            }
            started = false;
            updatesSender = null;
        }
    }

}
