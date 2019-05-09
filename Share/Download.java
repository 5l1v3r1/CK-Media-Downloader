/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Share;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloaderProject.DataIO;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author christopher
 */
public class Download {
    private ExecutorService app;
    private OperationStream s;
    private Socket soc;
    private ServerSocket server;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    
    public Download(OperationStream s) {
        this.s = s;
    }
    
    public void receive() {
        app = Executors.newSingleThreadExecutor();   
        app.execute(new server());
        app.shutdown();
    }
    
    private class server implements Runnable {
        @Override public void run() {
            if (openConnection() == 0) {
                s.addProgress("Connection Successful");
                processConnection();
            } else s.addProgress("Finished");
            try {server.close();} catch(IOException e) {}
        }
        
        private void receiveWhole(File saveLocation) throws IOException, ClassNotFoundException {
            FileOutputStream fos;
            String filename = (String)in.readObject();
            
            fos = new FileOutputStream(new File(saveLocation.getAbsolutePath()+File.separator+filename));
            s.addProgress("1%");
            fos.write((byte[])in.readObject()); s.addProgress("98%");
            fos.flush(); fos.close(); s.addProgress("100%");
        }
        
        private void receivePackets(File saveLocation) throws IOException, ClassNotFoundException {
            long buffSize = (long)in.readObject(), totalSize = (long)in.readObject();
            int loops = (int)(totalSize / buffSize);
            String filename =  (String)in.readObject();
            FileOutputStream fos = new FileOutputStream(saveLocation.getAbsolutePath()+File.separator+filename);
            
            for(int i = 0; i <= loops; i++) {
                fos.write((byte[])in.readObject());
                s.addProgress(String.format("%.0f",(float)((i+1)/(loops+1))*100)+"%");
            } 
        }
        
        private void receiveMedia() throws IOException, ClassNotFoundException {
            long freeMemory = Runtime.getRuntime().freeMemory();
            if(freeMemory > 25 * MainApp.BYTE * MainApp.BYTE)
                freeMemory = 25 * MainApp.BYTE * MainApp.BYTE; //25mb
            out.writeObject(freeMemory); out.flush();
            int transmissionMode = (int)in.readObject();
            if (transmissionMode == 1) 
                receiveWhole(MainApp.settings.preferences.getSharedFolder());
            else receivePackets(MainApp.settings.preferences.getSharedFolder());
        }
        
        private void receiveFile(String name) throws FileNotFoundException, IOException, ClassNotFoundException {
            FileOutputStream fos = new FileOutputStream(new File(MainApp.imageCache.getAbsolutePath()+File.separator+name));
            
            fos.write((byte[])in.readObject());
            fos.flush(); fos.close();
            MainApp.settings.cacheUpdate();
        }
        
        private void receiveSave() throws IOException, ClassNotFoundException {
            String name = (String)in.readObject();
            s.addProgress("Receiving dependencies");
            while(!name.equals("EOF")) {
                if(downloader.CommonUtils.checkImageCache(name)) {
                    out.writeObject("Yes"); out.flush();
                    name = (String)in.readObject(); continue;
                } else {
                    out.writeObject("No"); out.flush();
                    receiveFile(name);
                }
                name = (String)in.readObject();
            }
            
            s.addProgress("Receiving videos");
            Vector<video> receivedVideos = (Vector<video>)in.readObject();
            for(int i = 0; i < receivedVideos.size(); i++) {
                receivedVideos.get(i).adjustThumbnail(MainApp.imageCache.getAbsolutePath());
                receivedVideos.get(i).adjustPreview(MainApp.imageCache.getAbsolutePath());
                DataIO.saveVideo(receivedVideos.get(i));
            }
        }
        
        private void processConnection() {
            try {
                int type = (int)in.readObject();
                switch(type) {
                    case 1:
                        receiveSave();
                        break;
                    case 2:
                        if(!MainApp.settings.preferences.sharedFolderValid()) MainApp.settings.preferences.getSharedFolder().mkdirs();
                        receiveMedia();
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
                s.addProgress("I/O Exception occurred");
                s.addProgress(e.getMessage());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                s.addProgress("Class Cast Exception");
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
                e.printStackTrace();
                return 1;
            }
            s.addProgress("got I/O streams");
            return 0;
        }

        private int openConnection() {
            try {
                s.addProgress("Waiting for peer to connect");
                server = new ServerSocket(Actions.PORT,1);
                soc = server.accept();
                if(getStreams() == 0) {
                    s.addProgress("Connected to "+soc.getInetAddress().getHostName()); 
                    return 0;
                } else return 1;
            } catch (IOException e) {
                e.printStackTrace();
                return 2;
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
