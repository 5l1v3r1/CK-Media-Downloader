/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import downloader.CommonUtils;
import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 *
 * @author christopher
 */
public class downloadedMedia implements Externalizable{
    private static final long serialVersionUID = 2, VERSION = 2;
    private String name, site, date;
    private File thumb, media;
    
    public downloadedMedia() {
        //for jvm
    }
    
    public downloadedMedia(String name, File thumb, File media, String site, String date) {
        this.name = name; this.thumb = thumb; this.media = media; this.site = site; this.date = date;
    }
    
    public downloadedMedia(String name, File thumb, File media, String site) {
        this(name,thumb,media,site,"----");
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
        
        return CommonUtils.getSizeText(bytes);
    }
    
    public String getDownloaded() {
        return "Downloaded from "+site;
    }
    
    public String getDate() {
        return date;
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

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(VERSION);
        out.writeObject(name);
        out.writeObject(site);
        out.writeObject(thumb);
        out.writeObject(media);
        out.writeObject(date);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        in.readObject(); //ignore version for now
        name = (String)in.readObject();
        site = (String)in.readObject();
        thumb = (File)in.readObject();
        media = (File)in.readObject();
        date = (String)in.readObject();
    }
}
