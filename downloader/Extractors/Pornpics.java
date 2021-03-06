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
import org.jsoup.UncheckedIOException;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornpics extends GenericExtractor{
    private static final byte SKIP = 2;

    public Pornpics() { //this contructor is used for when you jus want to search
        
    }

    public Pornpics(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Pornpics(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Pornpics(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Elements a = getPage(url,false,true).select("a.rel-link");
        MediaDefinition media = new MediaDefinition();
        a.forEach((item) -> {
            Map<String, MediaQuality> qualities = new HashMap<>(); 
            qualities.put("single", new MediaQuality(item.attr("href"), "jpg"));
            media.addThread(qualities, CommonUtils.getThumbName(item.attr("href"),SKIP));
        });
        media.setAlbumName(videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("title").text().replace(" - PornPics.com","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        String thumbLink = getPage(url,false).select("a.rel-link").get(0).attr("href");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?pornpics[.]com/galleries/(?<id>[\\S]+)/";
        //https://www.pornpics.com/galleries/blonde-lesbian-cutie-hadley-viscara-has-massive-tits-and-loves-to-show-em-off/
    }
}
