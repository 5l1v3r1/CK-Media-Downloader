/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.File;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public class GenericQuery implements Serializable{
    private static final long serialVersionUID = 1L;
    protected Vector<String> link, name;
    protected Vector<File> thumbnails;
    protected Vector<Vector<File>> preview;
    protected Vector<Long> size;
    
    public GenericQuery() {
        thumbnails = new Vector<>();
        link = new Vector<>();
        name = new Vector<>();
        preview = new Vector<Vector<File>>();
        size = new Vector<>();
    }
    
    public GenericQuery(GenericQuery g) {
        if (thumbnails == null) this.thumbnails = new Vector<>();
        if (link == null) this.link = new Vector<>();
        if (preview == null) this.preview = new Vector<Vector<File>>();
        if (name == null) this.name = new Vector<>();
        if (size == null) this.size = new Vector<>();
        
        for(int i = 0; i < g.thumbnailCount(); i++) {
            this.thumbnails.add(g.getThumbnail(i));
            this.link.add(g.getLink(i));
            this.preview.add(g.getPreview(i));
            this.name.add(g.getName(i));
            this.size.add(g.getSize(i));
        }
    }
    
    public synchronized void addQuery(GenericQuery g) throws SocketTimeoutException {
        for(int i = 0; i < g.thumbnailCount(); i++) {
            this.thumbnails.add(g.getThumbnail(i));
            this.link.add(g.getLink(i));
            this.preview.add(g.getPreview(i));
            this.name.add(g.getName(i));
            this.size.add(g.getSize(i));
        }
    }
    
    public synchronized void addLink(String url) {
        link.add(url);
    }
    
    public synchronized void addName(String name) {
        this.name.add(name);
    }
    
    public synchronized  void addThumbnail(File thumb) {
        thumbnails.add(thumb);
    }
    
    public synchronized void addSize(long s) {
        size.add(s);
    }
    
    //thumbnails from search
    public synchronized  int thumbnailCount() {
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
}
