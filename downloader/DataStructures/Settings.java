/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import downloaderProject.MainApp;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public class Settings implements Externalizable{
    private static final long serialVersionUID = 3L, version = 6L;
    private File videoFolder, pictureFolder, importFolder, sharedFolder, FFmpegFolder, streamFolder;
    private Map<String,Boolean> supportedSite;
    private boolean darkTheme;
    
    public Settings() {
        supportedSite = new HashMap<>();
        videoFolder = pictureFolder = importFolder = FFmpegFolder = null;
    }
    
    //set folder to download to with String
    public void setStreamFolder(String folder) {
        setStreamFolder(new File(folder));
    }
    
    //set folder to download to with File
    public void setStreamFolder(File folder) {
        streamFolder = folder;
    }
    
    //return stream folder 
    public File getStreamFolder() {
        return streamFolder;
    }
    
    //set folder where ffmpeg is located with String
    public void setFFmpegFolder(String folder) {
        setFFmpegFolder(new File(folder));
    }
    
    //set folder where ffmpeg is located with File
    public void setFFmpegFolder(File folder) {
        FFmpegFolder = folder;
    }
    
    //return ffmpeg folder 
    public File getFFmpegFolder() {
        return FFmpegFolder;
    }
    
    //set folder to download to with String (for shared media)
    public void setSharedFolder(String folder) {
        setSharedFolder(new File(folder));
    }
    
    //set folder to download to with File (for shared media)
    public void setSharedFolder(File folder) {
        sharedFolder = folder;
    }
    
    //return shared folder
    public File getSharedFolder() {
        return sharedFolder;
    }
    
    //set where the open dialog will initial display when choosing import file
    public void setImportFolder(File folder) {
        importFolder = folder;
    }
    
    //get folder for initial display for imports
    public File getImportFolder() {
        //if (!importFolder.exists())
        return importFolder;
    }
    
    //set folder to download to with File (for video)
    public void setVideoFolder(File folder) {
        videoFolder = folder;
        if (!videoFolder.exists()) videoFolder.mkdirs();
    }
    
    //set folder to download to with String (for video)
    public void setVideoFolder(String folder) {
        setVideoFolder(new File(folder));
    }
    
    //get folder to download to (for video)
    public File getVideoFolder() {
        return videoFolder;
    }
    
    //set folder to download to with File (for picture)
    public void setPictureFolder(File folder) {
        pictureFolder = folder;
        if (!pictureFolder.exists()) pictureFolder.mkdirs();
    }
    
    //set folder to download to with String (for picture)
    public void setPictureFolder(String folder) {
        setPictureFolder(new File(folder));
    }
    
    //get folder to download to (for picture)
    public File getPictureFolder() {
        return pictureFolder;
    }
    
    //make sure even tho file is not null that it exists
    public boolean pictureFolderValid() {
        return pictureFolder.exists();
    }
    
    //make sure even tho file is not null that it exists
    public boolean videoFolderValid() {
        return videoFolder.exists();
    }
    
    //make sure even tho file is not null that it exists
    public boolean sharedFolderValid() {
        return sharedFolder.exists();
    }
    
    //make sure even tho file is not null that it exists
    public boolean FFmpegFolderValid() {
        return FFmpegFolder.exists();
    }
    
    //make sure even tho file is not null that it exists
    public boolean streamFolderValid() {
        return streamFolder.exists();
    }
    
    //set sites in list with a hashmap
    public void setSites(HashMap<String,Boolean> map) {
        supportedSite = map;
    }
    
    //set/add sites in list from a set ... new additions deafult true for querying
    public void setSites(Set<String> set) {
        Iterator<String> i = set.iterator();
        while(i.hasNext())
            supportedSite.put(i.next(), true);
    }
    
    //set/add sites in list from a vector ... new additions deafult true for querying
    public void setSites(Vector<String> s) {
        for(int i = 0; i < s.size(); i++)
            supportedSite.put(s.get(i), true);
    }
    
    //check to see if a site is in the list
    public boolean isSupported(String site) {
        return supportedSite.containsKey(site);
    }
    
    //add a site to the list
    public void addSupport(String site) {
        supportedSite.put(site, true);
    }
    
    //check to see if a site is enabled for querying
    public boolean isEnabled(String site) {
        return supportedSite.get(site);
    }
    
    //set the status of site for querying
    public void setEnabled(String site, boolean state) {
        supportedSite.replace(site, state);
    }
    
    //get all sites in list as a set
    public Set<String> getSupportedSites() {
        return supportedSite.keySet();
    }
    
    public boolean dark() {
        return darkTheme;
    }
    
    public void setDark(boolean enable) {
        darkTheme = enable;
    }
    
    public void initDownloadFolder(MainApp.OsType OS) {
        String home = System.getProperty("user.home");
        switch(OS) {
            case Windows:
                String homeFolder = System.getProperty("user.home");
                setVideoFolder(new File(homeFolder+"\\Videos"));
                setPictureFolder(new File(homeFolder+"\\Pictures"));
                setImportFolder(new File(homeFolder));
                setSharedFolder(new File(homeFolder+"\\DownloaderShared"));
                break;
            case Linux:
            case Apple:
            default:
                setVideoFolder(new File(home+File.separator+"Videos"));
                setPictureFolder(new File(home+File.separator+"Pictures"));
                setImportFolder(new File(home));
                setSharedFolder(new File(home+File.separator+"Shared"));
        }
        setFFmpegFolder(new File("."));
        setStreamFolder(new File(videoFolder.getAbsolutePath()));
    }
    
    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(version);
        out.writeObject(videoFolder); out.writeObject(pictureFolder);
        out.writeObject(importFolder); out.writeObject(sharedFolder);
        out.writeObject(supportedSite); out.writeObject(darkTheme);
        out.writeObject(FFmpegFolder); out.writeObject(streamFolder);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long v = (long)in.readObject();
        if (v == 3L) {
            videoFolder = (File)in.readObject(); pictureFolder = (File)in.readObject();
            importFolder = (File)in.readObject(); sharedFolder = (File)in.readObject();
            supportedSite = (Map<String,Boolean>)in.readObject(); darkTheme = (boolean)in.readObject();
            FFmpegFolder = new File(".");
            setStreamFolder(new File(videoFolder.getAbsolutePath()));
        } else if (v == 4L || v == 5L) {
            loadV4(in);
            FFmpegFolder = FFmpegFolder == null || FFmpegFolder.getAbsolutePath().contains("Stars") ? new File(".") : FFmpegFolder;
            setStreamFolder(new File(videoFolder.getAbsolutePath())); 
        } else if (v == 6L) {
            loadV4(in);
            FFmpegFolder = FFmpegFolder == null || FFmpegFolder.getAbsolutePath().contains("Stars") ? new File(".") : FFmpegFolder;
            streamFolder = (File)in.readObject();
            if (streamFolder == null)
                setStreamFolder(new File(videoFolder.getAbsolutePath()));
        } else {
            initDownloadFolder(MainApp.OS);
            darkTheme = false;
        }
        Vector<String> l = new Vector<>();
        for (String site : supportedSite.keySet()) {
            if (isAllLower(site))
                l.add(site); //gotta remove the old version of the extractor names (there were in all common)
        }
        l.forEach((s) -> {
            supportedSite.remove(s);
        });
    }
    
    private void loadV4(ObjectInput in) throws IOException, ClassNotFoundException {
        videoFolder = (File)in.readObject(); pictureFolder = (File)in.readObject();
        importFolder = (File)in.readObject(); sharedFolder = (File)in.readObject();
        supportedSite = (Map<String,Boolean>)in.readObject(); darkTheme = (boolean)in.readObject();
        FFmpegFolder = (File)in.readObject();
    }
    
    private boolean isAllLower(String str) {
        for(int i = 0; i < str.length(); i++)
            if (Character.isUpperCase(str.charAt(i)))
                return false;
        return true;
    }
}
