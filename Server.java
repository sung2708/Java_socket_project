import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.filechooser.FileSystemView;
import java.nio.file.WatchEvent.Kind;
import java.util.List;
import java.util.ArrayList;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class Server extends JFrame {
    private JPanel panel;
    private JList<String> clientList;
    private DefaultListModel<String> model;
    private JButton btnWatch;
    private JButton btnDisconnect;
    private JTextField tfPath;
    // Server socket and list of client sockets
    private ServerSocket serverSocket;
    private ArrayList<Socket> clients;

public Server() {
    // add client list
    model = new DefaultListModel<>();
    clientList = new JList<>(model);
    clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    clientList.setFixedCellWidth(200);
    clientList.setFixedCellHeight(20);
    clientList.setVisibleRowCount(10);
    clientList.setBorder(BorderFactory.createTitledBorder("Client List"));
    // add buttons
    btnWatch = new JButton("Watch");
    btnDisconnect = new JButton("Disconnect");
    btnWatch.setEnabled(false);
    btnDisconnect.setEnabled(false);
    // add text field
    tfPath = new JTextField();
    tfPath.setColumns(30);

    // add panel
    panel = new JPanel();
    panel.add(clientList);
    panel.add(btnWatch);
    panel.add(btnDisconnect);
    panel.add(tfPath);
    add(panel);
    //set up server socket
    try {
        serverSocket = new ServerSocket(3000);
        clients = new ArrayList<>();
        System.out.println("Server started at " + new Date());
    } catch (IOException e) {
        e.printStackTrace();
    }
    //create a thread to listen for connection request
    new Thread(() -> {
        while (true) {
            try {
                // listen for connection request
                Socket socket = serverSocket.accept();
                // add the client socket to the list
                clients.add(socket);
                // add the client to the list
                model.addElement(socket.getInetAddress().getHostAddress());
                // add listeners if a client is selected
                clientList.addListSelectionListener(new ListSelectionListener() {
                    @Override
                    public void valueChanged(ListSelectionEvent e) {
                        // TODO Auto-generated method stub
                        // enable the watch button and disconnect button
                        btnWatch.setEnabled(true);
                        btnDisconnect.setEnabled(true);
                        // enable the text field
                        tfPath.setEditable(true);
                    }
                });
                // add listeners if watch button is clicked, display the text field, and display changes in the directory in the text field
                // if disconnect button is clicked, disconnect the client, delete the client from the list
                btnWatch.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {

                            // get the selected client socket
                            Socket clientSocket = clients.get(clientList.getSelectedIndex());

                            new Thread(() -> {
                                try {
                                    // get the input stream of the client socket
                                    // receive the path of the directory to watch from the client
                            DataInputStream inputFromClient = new DataInputStream(clientSocket.getInputStream());
                            String path = inputFromClient.readUTF();
                                    // get the path of the directory to watch
                                    Path directory = Paths.get(path);
                                    // create a watch service
                                    WatchService watchService = directory.getFileSystem().newWatchService();
                                    // register the directory to watch
                                    directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                                    // listen for changes in the directory
                                    while (true) {
                                        // get the key of the watch service
                                        WatchKey key = watchService.take();
                                        // get the events of the key
                                        for (WatchEvent<?> event : key.pollEvents()) {
                                            // get the kind of the event
                                            Kind<?> kind = event.kind();
                                            // get the context of the event
                                            @SuppressWarnings("unchecked")
                                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                                            // get the path of the event
                                            Path filename = ev.context();
                                            // display the changes in the directory in the text field
                                            if(kind == StandardWatchEventKinds.ENTRY_CREATE) {
                                                tfPath.setText(filename + " created");
                                            } else if(kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                                tfPath.setText(filename + " deleted");
                                            } else if(kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                                tfPath.setText(filename + " modified");
                                            }
                                        }
                                        // reset the key
                                        boolean valid = key.reset();
                                        // if the key is not valid, break the loop
                                        if (!valid) {
                                            break;
                                        }
                                    }
                                } catch (IOException | InterruptedException e1) {
                                    // TODO Auto-generated catch block
                                    e1.printStackTrace();
                                }
                            }).start();
                    }
                });
                // disconect and delete the client from the list, reset the list selection
                btnDisconnect.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO Auto-generated method stub
                        // get the selected client socket
                        Socket clientSocket = clients.get(clientList.getSelectedIndex());
                        try {
                            // close the client socket
                            clientSocket.close();
                            // delete the client from the list
                            model.removeElementAt(clientList.getSelectedIndex());
                            // reset the list selection
                            clientList.clearSelection();
                            // disable the watch button and disconnect button
                            btnWatch.setEnabled(false);
                            btnDisconnect.setEnabled(false);
                            // disable the text field
                            tfPath.setEditable(false);
                        } catch (IOException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                });
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }).start();
}
    public static void main(String[] args) {
         Server frame = new Server();
        frame.setTitle("Client Socket List");
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}