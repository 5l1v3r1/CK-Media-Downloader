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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornpics extends GenericExtractor{
    private static final int SKIP = 2;

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
        Document page = getPage(url,false,true);
        Elements a = page.select("a.rel-link");
        MediaDefinition media = new MediaDefinition();
        for(Element item :a) {
            Map<String,String> qualities = new HashMap<>(); qualities.put("single",item.attr("href"));
            media.addThread(qualities, CommonUtils.getThumbName(item.attr("href"),SKIP));
        } return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        return page.select("title").text().replace(" - PornPics.com","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        String thumbLink = page.select("a.rel-link").get(0).attr("href");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Pornpics";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        long total = 0;
        Document page = getPage(url,false);
        Elements a = page.select("a.rel-link");
        for(Element item :a)
            total += CommonUtils.getContentSize(item.attr("href"));
        return total;
    }
    
    public String getId(String link) {
        Pattern p = Pattern.compile("https://(www.)?pornpics.com/galleries/([\\S]+)/");
        Matcher m = p.matcher(link);
        return m.find() ? m.group(2) : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
