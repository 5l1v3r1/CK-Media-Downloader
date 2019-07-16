/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Externalizable;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Vector;

/**
 *
 * @author christopher
 */

//this class is to save video info for download later
public class video implements Externalizable{
    private static final long serialVersionUID = 1L, version = 3L;
    private File thumbnail;
    private Vector<File> preview;
    private String link, name, duration;
    private long size;
    
    public video() { //to satisfy externalizable
    	
    }
    
    public video(String link, String name, File thumbnail, long size, String duration) {
        this(link,name,thumbnail,size, duration, null);
    }
    
    public video(String link, String name, File thumbnail, long size, String duration, Vector<File> preview) {
        this.link = link.split(" ")[0]; this.name = name;
        this.thumbnail = thumbnail;
        this.preview = preview;
        this.size = size;
        this.duration = duration;
    }    
    
    public long getSize() {
        return size;
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
    
    public String getDuration() {
        return duration;
    }
    
    public Vector<File> getDependencies() {
        Vector<File> f = new Vector<>();
        f.add(thumbnail);
        if (preview != null)
            f.addAll(preview);
        return f;
    }
    
    @Override public boolean equals(Object o) {
        if (o instanceof video) {
            video temp = (video)o;
            if((this.link.equals(temp.link)) && (this.name.equals(temp.name)))
                return this.thumbnail.equals(temp.thumbnail);
            else return false;
        } else return false;
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(version);
        out.writeObject(thumbnail);
        out.writeObject(preview);
        out.writeObject(link);
        out.writeObject(name);
        out.writeObject(size);
        out.writeObject(duration);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long v = (long)in.readObject();
        if (v == 1) {
            loadPre(in);
        } else if (v == 2) {
            loadPre(in);
            size = (long)in.readObject();
        } else if (v == 3) {
            loadPre(in);
            size = (long)in.readObject();
            duration = (String)in.readObject();
        }
    }
    
    private void loadPre(ObjectInput in) throws IOException, ClassNotFoundException {
        thumbnail = (File)in.readObject();
        preview = (Vector<File>)in.readObject();
        link = (String)in.readObject();
        name = (String)in.readObject();
    }
}
