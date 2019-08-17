/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.OfflineException;
import downloader.Exceptions.PrivateVideoException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */
public class Chaturbate extends GenericExtractor {
    
    public Chaturbate() {
        
    }

    public Chaturbate(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),getStarName(configureUrl(url)));
    }
    
    public Chaturbate(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,getStarName(configureUrl(url)));
    }
    
    public Chaturbate(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String getStarName(String url) {
        return getId(url, "https?://(?:www.)?chaturbate.com/(?<id>[^/?#]+)/?");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String thumb = "https://roomimg.stream.highwebmedia.com/ri/"+getStarName(url)+".jpg";
        String name = CommonUtils.getThumbName(thumb);
        
        if (CommonUtils.checkImageCache(name))
           new File(MainApp.imageCache.getAbsolutePath() + File.separator + name).delete();
        CommonUtils.saveFile(thumb, name, MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath() + File.separator + name);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        String page = getPage(url, false, true).toString();
        Map<String, String> q = CommonUtils.parseM3u8Formats(verify(page).get(0));
        MediaDefinition media = new MediaDefinition();
        Map<String, MediaQuality> qualities = new HashMap<>();
        q.keySet().iterator().forEachRemaining(a -> 
                qualities.put(a, new MediaQuality(q.get(a), "mp4", true)));
        media.addThread(qualities, videoName);
        return media;
    }
    
    private Vector<String> verify(String page) throws GenericDownloaderException {
        if (page.contains("Room is currently offline"))
            throw new OfflineException(videoName);
        Vector<String> m3u8Links = getMatches(page, "(?<m3u8>https?://\\S+/playlist[.]m3u8)", "m3u8");
        if (m3u8Links.isEmpty())
            throw new PrivateVideoException(videoName+" is possibly in private mode");
        else return m3u8Links;
    }
    
    @Override public boolean isLive() {
        return true;
    }
    
    @Override public long getSize() {
        return -1;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www.)?chaturbate.com/(?<id>[^/?#]+)/?"; 
        //https://chaturbate.com/lilly_pink
    }
}
