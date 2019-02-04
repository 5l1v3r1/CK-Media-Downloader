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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *
 * @author christopher
 */
public abstract class GenericExtractor {
    protected File videoThumb;
    protected String videoName, url;
    protected String extractorName;
    protected Map<String,String> cookieJar;

    GenericExtractor(String url, File thumb, String videoName) {
       this();
       this.videoThumb = thumb;
       this.videoName = videoName;
       this.url = configureUrl(url);
       this.cookieJar = new HashMap<String,String>();
    }
    
    GenericExtractor() { //this contructor is used for when you jus want to query / search
        setExtractorName();
    }
    
    public String name() {
       return extractorName; 
    }
    
    protected abstract void setExtractorName();
    
    protected static Document getPage(String url, boolean mobile) throws IOException {
        return getPage(url,mobile,false);
    }
    
    protected static Document getPage(String url, boolean mobile, boolean force) throws FileNotFoundException, IOException {
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile)) && !force) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else { String html;
             if (mobile)
                html = Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).get().html();
             else html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, mobile);
        } //if not found in cache download it
        return page;
    }
    
    protected Document getPageCookie(String url, boolean mobile) throws IOException {
        return getPage(url,mobile,false);
    }
    
    protected Document getPageCookie(String url, boolean mobile, boolean force) throws FileNotFoundException, IOException {
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile)) && !force) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else { String html;
             if (mobile)
                html = addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.MOBILECLIENT).get().html();
             else html = addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.PCCLIENT).get().html();
            page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, mobile);
        } //if not found in cache download it
        return page;
    }
    
    private Connection addCookies(Connection c) {
        Iterator<String> i = cookieJar.keySet().iterator();
        while(i.hasNext()) {
            String cookie = i.next();
            c = c.cookie(cookie, cookieJar.get(cookie));
        }
        return c;
    } 
    
    protected static String getCanonicalLink(Document page) {
        for(Element link: page.select("link"))
            if(link.attr("rel").equals("canonical"))
                return link.attr("href");
        return null;
    }
    
    protected static String getMetaImage(Document page) {
        String thumbLink = null; 
	for(Element meta :page.select("meta"))
            if (meta.attr("property").equals("og:image") && !meta.attr("content").contains("static"))
                thumbLink = meta.attr("content");
        if (thumbLink == null)
            for(Element meta :page.select("meta"))
                if (meta.attr("itemprop").equals("og:image"))
                    thumbLink = meta.attr("content");
        if (thumbLink == null)
            for(Element meta :page.select("link"))
                if (meta.attr("rel").equals("image_src"))
                    thumbLink = meta.attr("href");
        return thumbLink;
    }
    
    protected static String getTitle(Document page) {
        String title = null;
        //title = page.select("title").text();
        //if (title.length() < 1)
        for(Element meta :page.select("meta"))
            if(meta.attr("property").equals("og:title"))
                title = meta.attr("content");
        if (title == null)
            for(Element meta :page.select("meta"))
                if(meta.attr("itemprop").equals("name"))
                    title = meta.attr("content");
        return title;
    }
    
    protected static String getH1Title(Document page) {
        return Jsoup.parse(page.select("h1").toString()).body().text();
    }
    
    protected String getDefaultVideo(Document page) {
        if (page.select("video").select("source").isEmpty())
            return page.select("video").attr("src");
        else return page.select("video").select("source").attr("src");
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
    
    protected void addCookie(String cookieName, String cookie) {
        cookieJar.put(cookieName, cookie);
    }
    
    protected void clearCookies() {
        cookieJar.clear();
    }
    
    public abstract video similar() throws IOException; //get a video from the related items list
    public abstract video search(String str) throws IOException; //search (similar to query except no img preview and only 1 result) 
    public abstract long getSize() throws IOException, GenericDownloaderException;
    
    protected static String configureUrl(String link) {
        if (!link.matches("http(s)?://[\\S]+")) return "https://" + link;
        else
            return link;
    }
    
    protected static String changeHttp(String link) {
        return link.replace("https", "http");
    }
}
