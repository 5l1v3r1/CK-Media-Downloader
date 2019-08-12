/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PrivateVideoException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Shesfreaky extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 1;
    
    public Shesfreaky() { //this contructor is used for when you jus want to query
        
    }
    
    public Shesfreaky(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Shesfreaky(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Shesfreaky(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        verify(page);
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);
        
        return media;
    }

    private static void verify(Document page) throws GenericDownloaderException {
       if (!page.select("p.private-video").isEmpty())
            throw new PrivateVideoException();
       String title = Jsoup.parse(page.select("title").toString()).text();
       if (title.equals("404 Not Found") || title.equals("404: File Not Found - ShesFreaky"))
           throw new PageNotFoundException();
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String searchUrl = "https://www.shesfreaky.com/search/videos/"+search.trim().replaceAll(" ", "-")+"/page1.html";
        GenericQuery thequery = new GenericQuery();
        
        Elements searchResults = getPage(searchUrl,false).select("div.blockItem.blockItemBox");
        for(byte i = 0; i < searchResults.size(); i++) {
            String link = searchResults.get(i).select("a").attr("href");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String title = searchResults.get(i).select("span.details").select("em").attr("title");
            String thumb = configureUrl(searchResults.get(i).select("span.thumb").select("img").attr("data-src"));
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
            thequery.addSize(getSize(link));
            thequery.addDuration(getDuration(link).toString());
         }
         return thequery;
    }

    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page; 
        Vector<File> thumbs = new Vector<>();
         page = getPage(url,false);
         
         Elements thumbLinks;
         try {
            thumbLinks = page.getElementById("vidSeek").select("div.vidSeekThumb");
         } catch (NullPointerException e) { //different config
             thumbLinks = page.select("div.row.vtt-thumbs").select("a");
         }
         
         if (thumbLinks != null) {
            for(byte i = 0; i < thumbLinks.size(); i++) {
                String link = configureUrl(thumbLinks.get(i).select("a").select("img").attr("src"));
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,SKIP))) //if file not already in cache download it
                   CommonUtils.saveFile(link,CommonUtils.getThumbName(link,SKIP),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,SKIP)));
            }
         }
         return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        verify(page);
	return Jsoup.parse(page.getElementById("n-vid-details").select("h2").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        verify(page);
        String thumbLink = page.select("video").attr("poster");
        if (thumbLink.isEmpty()) {
            Elements thumbLinks = page.select("div.vidSeekThumb");
            thumbLink = configureUrl(thumbLinks.get(0).select("a").select("img").attr("src"));
        } else 
            thumbLink = configureUrl(thumbLink);
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        
        Elements li = getPage(url,false).select("div.relatedSection").get(0).select("div.blockItem.blockItemBox");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            byte i = (byte)randomNum.nextInt(li.size()); count++;
            String link = li.get(i).select("a").attr("href");
            try {verify(getPage(link,false)); } catch(GenericDownloaderException e) {continue;}
            String title = li.get(i).select("em").attr("title");
            try {v = new video(link,title,downloadThumb(link),getSize(link),getDuration(link).toString());} catch(Exception e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.shesfreaky.com/search/videos/"+str.trim().replaceAll(" ", "-")+"/page1.html";
        
        Elements searchResults = getPage(searchUrl,false).select("div.blockItem.blockItemBox");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
         
        while(count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            String link = searchResults.get(i).select("a").attr("href");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String title = searchResults.get(i).select("span.details").select("em").attr("title");
            String thumb = configureUrl(searchResults.get(i).select("span.thumb").select("img").attr("data-src"));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            v = new video(link,title,new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link),getDuration(link).toString());
            break; //if u made it this far u already have a vaild video
         }
         return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Map<String,String> q = getDefaultVideo(getPage(link,false,true));
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        GameTime g = new GameTime();
        try {
            long secs = getSeconds(getPage(link,false).getElementById("n-vid-detail-right").select("p").get(0).text());
            g.addSec(secs);
        } catch (NullPointerException e) {
            CommonUtils.log(e.getMessage(), this);
        }
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("p.categories").select("a").forEach(a -> words.add(a.text()));
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?shesfreaky[.]com/video/[\\S]+-(?<id>\\d+)[.]html";
        //https://www.shesfreaky.com/video/latina-webcam-341040.html
    }
}
