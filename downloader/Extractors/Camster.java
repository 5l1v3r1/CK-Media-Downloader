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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */
public class Camster extends GenericExtractor {
    public Camster() {
        
    }

    public Camster(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),getStarName(configureUrl(url)));
    }
    
    public Camster(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,getStarName(configureUrl(url)));
    }
    
    public Camster(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String getStarName(String url) {
        return getId(url, "https?://(?:www[.])?camster[.]com/webcam/(?<id>[^/?#]+)/?(?:/\\d+(?:#/myBio)?)?");
    }
    
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String page = getPage(url, false).toString(), name = getStarName(url);
        String thumb = getId(page, "(?<id>https?://static-preview-g[.]camster[.]com/\\S+"+name+"\\S+[.]jpg)");
        verify(page, name);
        
        String thumbName = CommonUtils.addId(CommonUtils.getThumbName(thumb), CommonUtils.getCurrentTimeStamp().replaceAll("[:/]", "-"));
        if (CommonUtils.checkImageCache(thumbName))
            new File(MainApp.imageCache.getAbsolutePath() + File.separator + thumbName).delete();
        CommonUtils.saveFile(thumb, thumbName, MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath() + File.separator + thumbName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        String page = getPage(url, false).toString();
        String m3u8Url = verify(page, getStarName(url));
        
        Map<String, String> q = CommonUtils.parseM3u8Formats(m3u8Url);
        MediaDefinition media = new MediaDefinition();
        Map<String, MediaQuality> qualities = new HashMap<>();
        q.keySet().iterator().forEachRemaining(a ->
                qualities.put(a, new MediaQuality(q.get(a), "mp4", true)));
        media.addThread(qualities, videoName);
        return media;
    }
    
    private static String verify(String page, String name) throws OfflineException {
        String link = getId(page, "(?<id>https?://static-transcode-k8s-g[.]camster[.]com/\\S+_"+name+"[.]m3u8)");
        if (link.isEmpty())
            throw new OfflineException("No streams are avaliable", true);
        return link;
    }
    
    @Override public boolean isLive() {
        return true;
    }
    
    @Override public long getSize() {
        return -1;
    }
    
    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?camster[.]com/webcam/(?<id>[^/?#]+)/?(?:/\\d+(?:#/myBio)?)?"; 
        //https://www.camster.com/webcam/mia-khalifa/45054
        //https://www.camster.com/webcam/vicky-vicster/25199#/myBio
        //https://www.camster.com/webcam/sophia-sanchez/76965#/myBio
    }
}
