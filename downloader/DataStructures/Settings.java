/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import downloaderProject.MainApp;
import static downloaderProject.MainApp.username;
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
    private static final long serialVersionUID = 3L;
    private static final long version = 4L;
    private File videoFolder, pictureFolder, importFolder, sharedFolder, profileFolder;
    private Map<String,Boolean> supportedSite;
    private boolean darkTheme;
    
    public Settings() {
        supportedSite = new HashMap<>();
        videoFolder = pictureFolder = importFolder = null;
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
    
    //set where to save profile pics of stars
    public void setProfileFolder(File folder) {
        profileFolder = folder;
    }
    
    public File getProfileFolder() {
        return profileFolder;
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
    
    //set sites in list with a hashmap
    public void setSites(HashMap<String,Boolean> map) {
        supportedSite = map;
    }
    
    //set/add sites in list from a set ... new additions deafult true for querying
    public void setSites(Set set) {
        Iterator i = set.iterator();
        while(i.hasNext())
            supportedSite.put((String)i.next(), true);
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
    public Set getSupportedSites() {
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
                MainApp.settings.preferences.setVideoFolder(new File(homeFolder+"\\Videos"));
                MainApp.settings.preferences.setPictureFolder(new File(homeFolder+"\\Pictures"));
                MainApp.settings.preferences.setImportFolder(new File(homeFolder));
                MainApp.settings.preferences.setSharedFolder(new File(homeFolder+"\\DownloaderShared"));
                break;
            case Linux:
            case Apple:
            default:
                MainApp.settings.preferences.setVideoFolder(new File(home+File.separator+"Videos"));
                MainApp.settings.preferences.setPictureFolder(new File(home+File.separator+"Pictures"));
                MainApp.settings.preferences.setImportFolder(new File(home+username));
                MainApp.settings.preferences.setSharedFolder(new File(home+File.separator+"Shared"));
        }
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(version);
        out.writeObject(videoFolder); out.writeObject(pictureFolder);
        out.writeObject(importFolder); out.writeObject(sharedFolder);
        out.writeObject(supportedSite); out.writeObject(darkTheme);
        out.writeObject(profileFolder);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long version = (long)in.readObject();
        if (version == 3L) {
            videoFolder = (File)in.readObject(); pictureFolder = (File)in.readObject();
            importFolder = (File)in.readObject(); sharedFolder = (File)in.readObject();
            supportedSite = (Map<String,Boolean>)in.readObject(); darkTheme = (boolean)in.readObject();
        } else if (version == 4L) {
            videoFolder = (File)in.readObject(); pictureFolder = (File)in.readObject();
            importFolder = (File)in.readObject(); sharedFolder = (File)in.readObject();
            supportedSite = (Map<String,Boolean>)in.readObject(); darkTheme = (boolean)in.readObject();
            profileFolder = (File)in.readObject();
        } else {
            initDownloadFolder(MainApp.OS);
            darkTheme = false;
        }
    }
}
