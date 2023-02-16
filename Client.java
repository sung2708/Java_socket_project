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
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Client {
    public static void main(String[] args) {
        // choose a directory sent to the server
        JFrame frame = new JFrame("File Explorer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        int rVal = fileChooser.showOpenDialog(null);
        if (rVal == JFileChooser.APPROVE_OPTION) {
            frame.setVisible(false);
            frame.dispose();
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            System.out.println(path);
            // connect to the server
            try {
                Socket socket = new Socket("localhost", 3000);
                DataOutputStream toServer = new DataOutputStream(socket.getOutputStream());
                toServer.writeUTF(path);
                toServer.flush();
                DataInputStream fromServer = new DataInputStream(socket.getInputStream());
                String message = fromServer.readUTF();
                System.out.println(message);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
