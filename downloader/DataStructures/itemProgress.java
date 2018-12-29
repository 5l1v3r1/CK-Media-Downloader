/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

import java.io.Serializable;

/**
 *
 * @author christopher
 */
public class itemProgress implements Serializable{
    private static final long serialVersionUID = 1L;
    String link;
    long progress;
    
    public itemProgress(String link) {
        this(link,0);
    }
    
    public itemProgress(String link, long progress) {
        this.link = link;
        this.progress = progress;
    }
    
    public String link() {
        return link;
    }
    
    public long bytes() {
        return progress;
    }
    
    public void setProgress(long bytes) {
        progress = bytes;
    }
}
