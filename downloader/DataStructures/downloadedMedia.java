/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import downloaderProject.MainApp;
import java.io.File;
import java.io.Serializable;

/**
 *
 * @author christopher
 */
public class downloadedMedia implements Serializable{
    private static final long serialVersionUID = -75282981916575898l;
    String name, site; //add url
    File thumb, media;
    
    public downloadedMedia(String name, File thumb, File media, String site) {
        this.name = name; this.thumb = thumb; this.media = media; this.site = site;
    }
    
    public File getThumb() {
        return thumb;
    }
    
    public boolean exists() {
        return media.exists();
    }
    
    public boolean folderExists() {
        return media.getParentFile().exists();
    }
    
    public String getLocation() {
        return media.getAbsolutePath();
    }
    
    public String getFolder() {
        return media.getParent();
    }
    
    public String getName() {
        return this.name;
    }
    
    public String getSize() {
        long bytes = 0; 
        if (media.isDirectory()) {
            File[] files = media.listFiles();
            for(File f:files)
                if (f.exists())
                    bytes += f.length();
        } else bytes = media.length();
        
        return MainApp.getSizeText(bytes);
    }
    
    public String getDownloaded() {
        return "Downloaded from "+site;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof downloadedMedia) {
            downloadedMedia temp = (downloadedMedia)o;
            if ((this.name.equals(temp.name)) && (this.site.equals(temp.site))) {
                if (this.thumb.getAbsolutePath().equals(temp.thumb.getAbsolutePath()))
                   return this.media.getAbsolutePath().equals(temp.media.getAbsolutePath());
                else return false;
            } else return false;
        } else return false;
    }
}
