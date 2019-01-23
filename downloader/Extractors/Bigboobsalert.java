/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Bigboobsalert extends GenericExtractor{    
    public Bigboobsalert() { //this contructor is used for when you jus want to search
        
    }

    public Bigboobsalert(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Bigboobsalert(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
        this.url = changeHttp(this.url);
    }
    
    public Bigboobsalert(String url, File thumb, String videoName){
        super(url,thumb,videoName);
        this.url = changeHttp(this.url);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException {
        Document page = getPage(url,false,true);
        
        Elements links = page.select("table.bg5").select("a"); MediaDefinition media = new MediaDefinition();
        for(Element link: links) {
            if (!link.attr("href").matches("pics/[\\S]+[.]jpg")) continue;
            Map<String,String> qualities = new HashMap<>();
            qualities.put("single","http://www.bigboobsalert.com/"+ link.attr("href"));
            media.addThread(qualities,CommonUtils.getThumbName(link.attr("href")));
        } media.setAlbumName(videoName);
        
        //super.downloadVideo(video.replace("cdn", "cdn3").replace("s12-1", "s12"),title,s);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        return getPage(url,false).select("a.gallery_title").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Document page = getPage(url,false);
        
        String thumbLink = "http://www.bigboobsalert.com/";
        Elements a = page.select("table.bg5").select("a");
        for(Element link: a) {
            if (!link.attr("href").matches("pics/[\\S]+[.]jpg")) continue;
            thumbLink += link.attr("href"); break;
        }
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Bigboobsalert";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public long getSize() throws IOException {
        Document page = getPage(url,false); long total = 0;
        
        Elements links = page.select("table.bg5").select("a");
        for(Element link: links) {
            if (!link.attr("href").matches("pics/[\\S]+[.]jpg")) continue;
            total += CommonUtils.getContentSize("http://www.bigboobsalert.com/" + link.attr("href"));
        } 
        return total;
    }
}
