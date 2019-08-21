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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Porntrex extends GenericExtractor {
    private static final byte SKIP = 3;
    
    public Porntrex() { //this contructor is used for when you jus want to search
        
    }

    public Porntrex(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,null,downloadVideoName(configureUrl(url)));
        this.videoThumb = downloadThumb(configureUrl(url));
    }
    
    public Porntrex(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Porntrex(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("p.title-video").text();
    } 
	
    //getVideo thumbnail
    private File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPageCookie(url, false);
        String thumbLink = getMetaImage(page);
        if (thumbLink == null || thumbLink.isEmpty())
            thumbLink = "http:" + getId(page.toString(), "preview_url: '(?<id>//\\S+[.]jpg)'");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache, CommonUtils.StringCookies(cookieJar));
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        MediaDefinition media = new MediaDefinition();
        
        String vars = getId(getPage(url,false,true).toString(), "var flashvars = \\{(?<id>[^;]+?)\\};");
        Vector<String> values = getMatches(vars, "\\S+:\\s*'(?<value>[^']+?)'", "value");
        Vector<String> keys = getMatches(vars, "(?<keys>\\S+):\\s*'[^']+?'", "keys");
        
        Map<String, String> map = new HashMap<>();
        Map<String, MediaQuality> q = new HashMap<>();
        for(int i = 0; i < keys.size(); i++)
            map.put(keys.get(i), values.get(i));
        
        q.put(map.get("video_url_text"), new MediaQuality(map.get("video_url")));
        map.keySet().iterator().forEachRemaining(k -> {
               if(k.matches("video_alt_url\\d*")) {
                   q.put(map.get("video_alt_url"+(k.substring(13).isEmpty() ? "" : k.substring(13))+"_text"), new MediaQuality(map.get(k)));
               }
        });
        media.addThread(q,videoName);

        return media;
    }

    @Override public boolean allowNoThumb() { 
        return true;
    }
    
    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www.)?porntrex.com/video/(?<id>\\d+)/\\S+";
        //https://porntrex.com/video/833726/girlsgonepink-evelin-vienna
    }
}
