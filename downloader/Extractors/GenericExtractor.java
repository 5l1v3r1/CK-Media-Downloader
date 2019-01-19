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
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public abstract class GenericExtractor {
    protected File videoThumb;
    protected String videoName, url;
    protected String extractorName;

    GenericExtractor(String url, File thumb, String videoName) {
       this();
       this.videoThumb = thumb;
       this.videoName = videoName;
       this.url = configureUrl(url);
    }
    
    GenericExtractor() { //this contructor is used for when you jus want to query / search
        setExtractorName();
    }
    
    public String name() {
       return extractorName; 
    }
    
    protected abstract void setExtractorName();
    
    protected static Document getPage(String url, boolean mobile) throws FileNotFoundException, IOException {
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile)));
        else { String html;
             if (mobile)
                html = Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).get().html();
             else html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, mobile);
        } //if not found in cache download it
        return page;
    }
    
    public abstract MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException;
    
    public String getVideoName() {
        return this.videoName;
    }
    
    public File getThumb() {
        return this.videoThumb;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    public abstract video similar() throws IOException; //get a video from the related items list
    public abstract video search(String str) throws IOException; //search (similar to query except no img preview and only 1 result) 
    public abstract long getSize() throws IOException, GenericDownloaderException;
    
    protected static String configureUrl(String link) {
        if (!link.matches("http(s)?://[\\S]+")) return "https://" + link;
        else
            return link;
    }
}
