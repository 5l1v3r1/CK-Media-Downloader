/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.DataStructures;

/**
 *
 * @author christopher
 */
public class MediaQuality {
    private String url, type;
    boolean live;
    
    public MediaQuality(String url) {
        this(url, "mp4", false);
    }
    
    public MediaQuality(String url, String type) {
        this(url, type.replaceAll("[.]", ""), false);
    }
    
    public MediaQuality(String url, String type, boolean live) {
        this.url = url;
        this.type = type;
        this.live = live;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isLive() {
        return live;
    }
    
    @Override public String toString() {
        return url;
    }
}
