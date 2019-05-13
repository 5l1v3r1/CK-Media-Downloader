/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Share;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author christopher
 */
public class Upload {
    private ExecutorService app;
    private OperationStream s;
    private final String address;
    private Socket soc;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final Vector<video> data;
    private final File media;
    
    public Upload(String address, OperationStream s, Vector<video> data) {
        this.s = s;
        this.address = address;
        this.data = data; media = null;
    }
    
    public Upload(String address, OperationStream s, File media) {
        this.s = s;
        this.address = address;
        this.media = media; data = null;
    }
    
    public void send() {
        app = Executors.newSingleThreadExecutor();   
        app.execute(new client());
        app.shutdown();
    }
    
    private class client implements Runnable {

        @Override public void run() {
            if(connectToServer() == 0) {
                s.addProgress("Connection Successful");
                processConnection();
            } else s.addProgress("Finished");
        }
        
        private void sendFile(File file) throws FileNotFoundException, IOException {
            FileInputStream fis = new FileInputStream(file);
            
            byte[] buffer = new byte[(int)file.length()];
            fis.read(buffer);
            out.writeObject(buffer); out.flush(); out.reset();
        }
        
        private void sendSave() throws IOException, ClassNotFoundException {
            s.addProgress("Sending dependencies");
            for(int i = 0; i < data.size(); i++) {
                out.writeObject(data.get(i).getThumbnail().getName()); out.flush();
                if(((String)in.readObject()).equals("No")) sendFile(data.get(i).getThumbnail());
                for(int j = 0; j < data.get(i).getPreviewCount(); j++) {
                    out.writeObject(data.get(i).getPreview(j).getName()); out.flush();
                    if(((String)in.readObject()).equals("Yes")) continue;
                    sendFile(data.get(i).getPreview(j));
                }
            }
            out.writeObject("EOF"); out.flush();
            
            s.addProgress("Sending videos");
            out.writeObject(data); out.flush();            
        }
        
        private void sendWhole() throws IOException {
            FileInputStream fis = new FileInputStream(media);
            out.writeObject(media.getName());
            
            s.addProgress("1%");
            byte[] file = new byte[(int)media.length()];
            fis.read(file);
            out.writeObject(file); s.addProgress("98%");
            out.flush(); out.reset(); s.addProgress("100%");
        }
        
        private void sendPackets(long buffSize, long fileSize) throws IOException {
            out.writeObject(buffSize);
            out.writeObject(fileSize);
            out.writeObject(media.getName()); 
            out.flush();
            
            int loops = (int)(fileSize / buffSize);
            int remain = (int)(fileSize % buffSize);
            FileInputStream fis = new FileInputStream(media);
            byte[] buffer = new byte[(int)buffSize];
            for(int i = 0; i < loops; i++) {
                fis.read(buffer);
                out.writeObject(buffer); out.flush(); out.reset();
                s.addProgress(String.format("%.0f",(float)((i+1)/(loops+1))*100)+"%");
            } buffer = null;
            if(remain != 0) {
                buffer = new byte[remain];
                fis.read(buffer);
                out.writeObject(buffer); out.flush(); out.reset();
                s.addProgress("100%");
            }
        }        
        
        private void sendMedia() throws IOException, ClassNotFoundException {
            long serverMemory = (long)in.readObject();
            long freeMemory = Runtime.getRuntime().freeMemory(), buffSize;
            
            buffSize = serverMemory > freeMemory ? freeMemory : serverMemory;
            
            if (buffSize < media.length()) {
                out.writeObject(2); out.flush(); sendPackets(buffSize,media.length());
            } else {
                out.writeObject(1); out.flush(); sendWhole();
            } 
        }
        
        private void processConnection() {
            try {
                int mode;
                if (data != null) mode = 1;
                else mode = 2;
                out.writeObject(mode); out.flush();
                if (mode == 1)
                    sendSave();
                else sendMedia();
            } catch (IOException e) {
                e.printStackTrace();
                s.addProgress("I/O Exception occurred");
                s.addProgress(e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                s.addProgress("Class Cast Exception");
                s.addProgress(e.getMessage());
            } finally {
                s.addProgress("Finished");
                s.endOperation();
                s = null;
                closeConnection();
            }
        }
        
        private int getStreams() {
            try {
                out = new ObjectOutputStream(soc.getOutputStream());
                out.flush();
                in = new ObjectInputStream(soc.getInputStream());
            } catch(IOException e) {
                return 1;
            }
            s.addProgress("got I/O streams");
            return 0;
        }
        
        private int connectToServer() {
            if(address == null) { MainApp.createMessageDialog("No device address provided");return 1;}
            else if (address.length() == 0){ MainApp.createMessageDialog("Invalid device address"); return 2;}
            else {
                s.addProgress("Attempting Connection");
                try {
                    s.addProgress("Server: "+InetAddress.getByName(address).toString());
                    soc = new Socket(InetAddress.getByName(address), Actions.PORT);
                    if (getStreams() == 0)
                        return 0;
                    else return 5;
                } catch (UnknownHostException e) {
                    s.addProgress("Unknown Host");
                    return 3;
                } catch (IOException ex) {
                    ex.printStackTrace();
                    s.addProgress("An I/O error occurred");
                    return 4;
                }
            }
        }
        
        private void closeConnection() {
            try {
                out.close();
                in.close();
                soc.close();
            } catch (IOException e) {
                e.printStackTrace();
                CommonUtils.log("Failed to close connection successfully",this);
            }
        }
    }
}
