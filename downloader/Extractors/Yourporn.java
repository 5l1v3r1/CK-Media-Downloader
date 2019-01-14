/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Yourporn extends GenericExtractor{
    private static final int skip = 2;
    
    public Yourporn() { //this contructor is used for when you jus want to search
        
    }

    public Yourporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Yourporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Yourporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {

        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page);
     
	String video = "https://www.yourporn.sexy"+CommonUtils.eraseChar(page.select("span.vidsnfo").attr("data-vnfo").split("\"")[3],'\\');
        //String video = "https://www.yourporn.sexy"+page.select("video.player_el").attr("src");
        Map<String,String> qualities = new HashMap<>();
        qualities.put("single",video.replace("cdn", "cdn3").replace("s12-1", "s12"));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        //super.downloadVideo(video.replace("cdn", "cdn3").replace("s12-1", "s12"),title,s);
        return media;
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if (page.getElementById("player_el") == null)
            throw new VideoDeletedException();
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        verify(page);
        //return page.select("meta").get(6).attr("content").replace(" on YourPorn. Sexy","");
        String raw = page.select("meta").get(6).attr("content");
        return raw.contains("#") ? raw.substring(0,raw.indexOf("#") -4) : raw.replace(" on YourPorn. Sexy","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
         Document page = getPage(url,false);
         verify(page);
         String thumbLink = "https:"+page.getElementById("player_el").attr("poster");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,skip));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Yourporn";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
}
