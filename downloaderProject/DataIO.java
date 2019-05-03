/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloaderProject;

import downloader.CommonUtils;
import downloader.DataStructures.Device;
import downloader.DataStructures.Settings;
import downloader.DataStructures.downloadedMedia;
import downloader.DataStructures.historyItem;
import downloader.DataStructures.itemProgress;
import downloader.DataStructures.video;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public class DataIO {
    
    public static Settings loadSettings() {
        ObjectInputStream in;
        Settings stats;
        try {
            in = new ObjectInputStream(new FileInputStream(new File(MainApp.saveDir.getAbsolutePath()+File.separator+"settings.dat")));
            stats = (Settings)in.readObject();
            in.close();
            return stats;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            return null;
        } catch (ClassNotFoundException ex) {
           CommonUtils.log("Class not found","DataIO");
           return null;
        }
    }
    
    public static void saveSettings(Settings stats) throws FileNotFoundException, IOException{
       ObjectOutputStream out;
       File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"settings.dat");
       if (!save.exists()) save.createNewFile();
       
       out = new ObjectOutputStream(new FileOutputStream(save));
       out.writeObject(stats);
       out.flush(); out.reset();
       out.close();
    }
    
    public static void saveVideo(video v) throws FileNotFoundException, IOException{
       ObjectOutputStream out;
       File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"laterVideos.dat");
       
       Vector<video> videos = loadVideos();
       out = new ObjectOutputStream(new FileOutputStream(save));
       
       if(videos == null) videos = new Vector<>();
       if (!videos.contains(v)) videos.add(v);
           
       out.writeObject(videos);
       out.flush(); out.reset();
       out.close();
    }
    
    public static Vector<video> loadVideos() {
        ObjectInputStream in;
        Vector<video> videos;
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"laterVideos.dat");
        
        try {
            in = new ObjectInputStream(new FileInputStream(save));
            videos = (Vector<video>)in.readObject();
            in.close(); //save.delete();
            return videos;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            CommonUtils.log("IOException: "+ex.getMessage(),"DataIO");
            return null;
        } catch (ClassNotFoundException ex) {
           CommonUtils.log("Class not found","DataIO");
           return null;
        } catch (ClassCastException e) {
           CommonUtils.log(e.getMessage(),"DataIO");
           return null; 
        }
    }
    
    public static void saveDevice(Device d) throws FileNotFoundException, IOException{
       ObjectOutputStream out;
       File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"devices.dat");
       
       Vector<Device> device = loadDevices();
       out = new ObjectOutputStream(new FileOutputStream(save));
       
       if(device == null) device = new Vector<>();
       if (!device.contains(d)) device.add(d);
           
       out.writeObject(device);
       out.flush(); out.reset();
       out.close();
    }
    
    public static Vector<Device> loadDevices() {
        ObjectInputStream in;
        Vector<Device> device;
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"devices.dat");
        
        try {
            in = new ObjectInputStream(new FileInputStream(save));
            device = (Vector<Device>)in.readObject();
            in.close(); //save.delete();
            return device;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            CommonUtils.log("IOException: "+ex.getMessage(),"DataIO");
            return null;
        } catch (ClassNotFoundException ex) {
           CommonUtils.log("Class not found","DataIO");
           return null;
        }
    }
    
    public static void saveHistory(historyItem h) throws FileNotFoundException, IOException {
       ObjectOutputStream out;
       File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"history.dat");
       
       Vector<historyItem> history = loadHistory();
       out = new ObjectOutputStream(new FileOutputStream(save));
       
       if(history == null) history = new Vector<>();
       history.add(h);
           
       out.writeObject(history);
       out.flush(); out.reset();
       out.close();
    }
    
    public static Vector<historyItem> loadHistory() {
        ObjectInputStream in;
        Vector<historyItem> history;
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"history.dat");
        
        try {
            in = new ObjectInputStream(new FileInputStream(save));
            history = (Vector<historyItem>)in.readObject();
            in.close(); //save.delete();
            return history;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            CommonUtils.log("IOException: "+ex.getMessage(),"DataIO");
            return null;
        } catch (ClassNotFoundException ex) {
           CommonUtils.log("Class not found","DataIO");
           return null;
        }
    }
    
    public synchronized static void saveDownloaded(downloadedMedia d) throws FileNotFoundException, IOException {
       ObjectOutputStream out;
       File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"downloaded.dat");
       
       Vector<downloadedMedia> downloaded = loadDownloaded();
       out = new ObjectOutputStream(new FileOutputStream(save));
       
       if(downloaded == null) downloaded = new Vector<>();
       if (!downloaded.contains(d)) downloaded.add(d);
           
       out.writeObject(downloaded);
       out.flush(); out.reset();
       out.close();
    }
    
    public synchronized static Vector<downloadedMedia> loadDownloaded() {
        ObjectInputStream in;
        Vector<downloadedMedia> downloaded;
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"downloaded.dat");
        
        try {
            in = new ObjectInputStream(new FileInputStream(save));
            downloaded = (Vector<downloadedMedia>)in.readObject();
            in.close(); //save.delete();
            return downloaded;
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException ex) {
            CommonUtils.log("IOException: "+ex.getMessage(),"DataIO");
            return null;
        } catch (ClassNotFoundException ex) {
           CommonUtils.log("Class not found","DataIO");
           return null;
        }
    }
    
    public static long readProgress(File progressFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream in;
        
        in = new ObjectInputStream(new FileInputStream(progressFile));
        itemProgress item = (itemProgress)in.readObject();
        in.close();
        return item.bytes();
    }
    
    public static void writeProgress(File progressFile, String url, long progress) throws FileNotFoundException, IOException {
        ObjectOutputStream out;
        
        out = new ObjectOutputStream(new FileOutputStream(progressFile));
        out.writeObject(new itemProgress(url,progress));
        out.flush(); out.close();
    }
    
    public static void clearVideos() {
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"laterVideos.dat");
        save.delete();
    }
    
    public static void clearDevices() {
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"devices.dat");
        save.delete();
    }
    
    public static void clearHistory() {
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"history.dat");
        save.delete();
    }
    
    public static void clearDownloaded() {
        File save = new File(MainApp.saveDir.getAbsolutePath()+File.separator+"downloaded.dat");
        save.delete();
    }
    
    public synchronized static int getDownloadedCount() {
        Vector<downloadedMedia> downloaded = loadDownloaded();
        if(downloaded == null)
            return 0;
        else return downloaded.size();
    }
    
    public synchronized static int getSaveVideoCount() {
        Vector<video> videos = loadVideos();
        if(videos == null)
            return 0;
        else return videos.size();
    }
    
    public synchronized static int getHistoryCount() {
        Vector<historyItem> history = loadHistory();
        if(history == null)
            return 0;
        else return history.size();
    }
    
    /*public synchronized static Vector<String> loadDictionary() {
        Vector<String> words = new Vector<>(); 
        
        Scanner reader = new Scanner(System.class.getResource("/data/dictionary.dat").getFile());
        while(reader.hasNextLine()) words.add(reader.nextLine());
        reader.close();
        return words;
    }*/
    
    public synchronized static Vector<String> loadIgnoreWords() throws FileNotFoundException {
        Vector<String> words = new Vector<>();
        
        Scanner reader = new Scanner(System.class.getResourceAsStream("/data/conjunctions.txt"));
        while(reader.hasNextLine()) words.add(reader.nextLine()); reader.close();
        reader = new Scanner(System.class.getResourceAsStream("/data/prepositions.txt"));
        while(reader.hasNextLine()) words.add(reader.nextLine()); reader.close();
        reader = new Scanner(System.class.getResourceAsStream("/data/pronouns.txt"));
        while(reader.hasNextLine()) words.add(reader.nextLine()); reader.close();
        return words;
    }
    
    public synchronized static Vector<String> loadStarList() {
        Vector<String> words = new Vector<>();
        
        Scanner reader = new Scanner(System.class.getResourceAsStream("/data/starList.dat"));
        while(reader.hasNextLine()) words.add(reader.nextLine()); reader.close();
        return words;
    }
    
    public synchronized static DataCollection loadCollectedData() {
        ObjectInputStream in;
        
        try {
            in = new ObjectInputStream(new FileInputStream(new File(MainApp.saveDir+File.separator+"habits.dat")));
            DataCollection dc = (DataCollection)in.readObject();
            in.close();
            return dc;
        } catch (FileNotFoundException | ClassNotFoundException e) {
            CommonUtils.log(e.getMessage(),"DataIO");
            return new DataCollection(true);
        } catch(IOException e) {
            e.printStackTrace();
            return new DataCollection(true);
        } catch (ClassCastException e) {
            CommonUtils.log("old verison of data collection couldnt be used","DataIO");
            return new DataCollection(true);
        }
    }
    
    public synchronized static void saveCollectedData(DataCollection dc) throws FileNotFoundException, IOException{
        ObjectOutputStream out;
        
        out = new ObjectOutputStream(new FileOutputStream(new File(MainApp.saveDir+File.separator+"habits.dat")));
        out.writeObject(dc);
        out.flush(); out.close();
    }
    
    public static File[] getExempt() {
        Vector<File> files = new Vector<>();
        Vector<video> videos = loadVideos();
        
        //get thumbs from saved videos to exempt
        if(videos != null)
             for(video v:videos)
                 files.addAll(v.getDependencies());
        
        //get thumbs from suggested videos to exempt
        DataCollection d = DataIO.loadCollectedData();
        if (d != null) files.addAll(d.getExempt());
        
        //get thumbs from download history to exempt
        Vector<downloadedMedia> media = loadDownloaded();
        if (media != null)
            for(downloadedMedia m: media)
                files.add(m.getThumb());
        
        //get query dependencies
        Vector<historyItem> history = loadHistory();
        if(history != null)
            for(historyItem h: history)
                files.addAll(h.getSearchResult().getDependencies());
        
        File[] f = new File[files.size()];
        files.toArray(f); Arrays.parallelSort(f);
        return f;
    }
    
    public static void clearCache() {
        File[] files = MainApp.imageCache.listFiles();
        File[] files2 = MainApp.pageCache.listFiles();
        
        File[] exemptFiles = getExempt();
        
        //remove unused thumbnails
        for(File f:files)
            if (Arrays.binarySearch(exemptFiles, f) < 0)
                f.delete();
        
        //remove all saved pages
        for(File f:files2)
            f.delete();    
    }
    
    public static long getCacheSize() {
        File[] files = MainApp.imageCache.listFiles();
        File[] files2 = MainApp.pageCache.listFiles();
        long total = 0;
        
        if (files != null)
            for(File f:files)
                total+=f.length();
        if (files2 != null)
            for(File f:files2)
                total+=f.length();
        
        return total;
    }
    
    public static void moveFile(File file, File destination) {
        FileInputStream fis; FileOutputStream fos;
        
        try {
            fis = new FileInputStream(file);
            fos = new FileOutputStream(destination.getAbsolutePath()+File.separator+file.getName());
            byte[] bytes = new byte[(int)file.length()];
            fis.read(bytes);
            fos.write(bytes);
            fis.close();
            fos.flush();
            fos.close();
            CommonUtils.log("Moved "+file.getName(),"DataIO");
            file.delete();
            CommonUtils.log("Deleted "+file.getName(),"DataIO");
        } catch (FileNotFoundException e) {
            CommonUtils.log("Didnt find "+file.getName()+" and failed to move","DataIO");
        } catch(IOException e) {
            CommonUtils.log("Error moving "+file.getName()+" "+e.getMessage(),"DataIO");
        }
    }
}
