/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;
import java.util.Vector;

/**
 *
 * @author christopher
 */

//this class is to save video info for download later
public class video implements Serializable{
    private static final long serialVersionUID = 1L;
    File thumbnail;
    Vector<File> preview;
    String link, name;
    
    
    public video(String link, String name, File thumbnail) {
        this(link,name,thumbnail,null);
    }
    
    public video(String link, String name, File thumbnail, Vector<File> preview) {
        this.link = link; this.name = name;
        this.thumbnail = thumbnail;
        this.preview = preview;
    }    
    
    public int getPreviewCount() {
        if (preview == null) return 0;
        else return preview.size();
    }
    
    public File getPreview(int i) {
        if (preview == null) return null;
        else {
            if((i >= 0) && (i < preview.size()))
                return preview.get(i);
            else return null;
        }
    }
    
    public void adjustThumbnail(String newPath) {
        thumbnail = new File(newPath+File.separator+thumbnail.getName());
    }
    
    public void adjustPreview(String newPath) {
        if (preview != null) {
            for(int i = 0; i < preview.size(); i++)
                preview.set(i,new File(newPath+File.separator+preview.get(i).getName()));
        }
    }
    
    public File getThumbnail() {
        return thumbnail;
    }
    
    public String getLink() {
        return link;
    }
    
    public String getName() {
        return name;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof video) {
            video temp = (video)o;
            if((this.link.equals(temp.link)) && (this.name.equals(temp.name)))
                return this.thumbnail.equals(temp.thumbnail);
            else return false;
        } else return false;
    }
}
