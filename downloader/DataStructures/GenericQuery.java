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
import java.net.SocketTimeoutException;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public class GenericQuery implements Externalizable{
    private static final long serialVersionUID = 1L;
    private static final long VERSION = 2L;
    protected Vector<String> link, name, duration;
    protected Vector<File> thumbnails;
    protected Vector<Vector<File>> preview;
    protected Vector<Long> size;
    
    public GenericQuery() {
        init();
    }
    
    public GenericQuery(GenericQuery g) {
        init();
        
        for(int i = 0; i < g.thumbnailCount(); i++) {
            this.thumbnails.add(g.getThumbnail(i));
            this.link.add(g.getLink(i));
            this.preview.add(g.getPreview(i));
            this.name.add(g.getName(i));
            this.size.add(g.getSize(i));
            this.duration.add(g.getDuration(i));
        }
    }
    
    private void init() {
        if (thumbnails == null) this.thumbnails = new Vector<>();
        if (link == null) this.link = new Vector<>();
        if (preview == null) this.preview = new Vector<Vector<File>>();
        if (name == null) this.name = new Vector<>();
        if (size == null) this.size = new Vector<>();
        if (duration == null) this.duration = new Vector<>();
    }
    
    public synchronized void addQuery(GenericQuery g) throws SocketTimeoutException {
        for(int i = 0; i < g.thumbnailCount(); i++) {
            this.thumbnails.add(g.getThumbnail(i));
            this.link.add(g.getLink(i));
            this.preview.add(g.getPreview(i));
            this.name.add(g.getName(i));
            this.size.add(g.getSize(i));
            this.duration.add(g.getDuration(i));
        }
    }
    
    public synchronized void addLink(String url) {
        link.add(url);
    }
    
    public synchronized void addName(String name) {
        this.name.add(name);
    }
    
    public synchronized void addThumbnail(File thumb) {
        thumbnails.add(thumb);
    }
    
    public synchronized void addSize(long s) {
        size.add(s);
    }
    
    public synchronized void addDuration(String d) {
        duration.add(d);
    }
    
    //thumbnails from search
    public synchronized int thumbnailCount() {
        return thumbnails.size();
    }
    
    public synchronized String getLink(int i) {
        if ((i > -1) && (i < link.size()))
            return link.get(i);
        return null;
    }
    
    public synchronized String getName(int i) {
        if ((i > -1) && (i < name.size()))
            return name.get(i);
        return null;
    }
    
    public synchronized File getThumbnail(int i) {
        if ((i > -1) && (i < thumbnails.size()))
            return thumbnails.get(i);
        return null;
    }
    
    public synchronized long getSize(int i) {
        if ((i > -1) && (i < size.size()))
            return size.get(i);
        return -1;
    }
    
    public synchronized String getDuration(int i) {
        if ((i > -1) && (i < size.size()))
            return duration.get(i);
        return "----";
    }
    
     //preview of a search item
    public synchronized void addPreview(Vector<File> preview) {
        this.preview.add(preview);
    }
    
    //return thumbnails of a video from one of the search results
    public synchronized Vector<File> getPreview(int i) {
        if ((i > -1) && (i < preview.size()))
            return preview.get(i);
        return null;
    }

    public Vector<File> getDependencies() {
        Vector<File> f = new Vector<>();
        f.addAll(thumbnails);
        preview.forEach((f2) -> {
            f.addAll(f2);
        });
        return f;
    }

    @Override public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(VERSION);
        out.writeObject(link);
        out.writeObject(name);
        out.writeObject(preview);
        out.writeObject(thumbnails);
        out.writeObject(size);
        out.writeObject(duration);
    }

    @Override public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        long version = (long)in.readObject();
        if (version == 2L) {
            link = (Vector<String>)in.readObject();
            name = (Vector<String>)in.readObject();
            preview = (Vector<Vector<File>>)in.readObject();
            thumbnails = (Vector<File>)in.readObject();
            size = (Vector<Long>)in.readObject();
            duration = (Vector<String>)in.readObject();
        }
    }
}

