/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Befuck extends GenericExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Befuck() { //this contructor is used for when you jus want to search
        
    }
        
    public Befuck(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Befuck(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Befuck(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(getPage(url,false,true)),videoName);
        
        return media;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception {
	return Jsoup.parse(getPage(url,false).select("div.desc").select("span").get(0).toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String thumb = getPage(url,false).select("video").attr("poster");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://befuck.com/search/"+str.trim().replaceAll(" ", "+");
    	
        Elements li = getPage(searchUrl,false).select("figure"); 
        Random rand = new Random(); int count = li.size(); video v = null;
        
        while(count-- > 0) {
            byte i = (byte)rand.nextInt(li.size());
            String thumbLink = li.get(i).select("img").attr("data-src"); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = li.get(i).select("a").attr("href");
            String name = li.get(i).select("figcaption").text();
            if (link.isEmpty() || name.isEmpty()) continue;
            v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link),"----");
            break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Map<String, MediaQuality> q = getDefaultVideo(getPage(link,false,true));
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()).getUrl());
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        Vector<String> words = new Vector<>();
        if (url != null) {
            Document page = getPage(url, false);
            page.select("p.cats").select("a").forEach((a) -> words.add(a.text()));
            page.select("p.tags").select("a").forEach((a) -> words.add(a.text()));
        }
        return words;
    }
    
    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        Vector<String> words = new Vector<>();
        if (url != null)
            words.add(getPage(url, false).select("p.models").select("a").text());
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?befuck[.]com/videos/(?<id>[\\d]+)/[\\S]+";
        //https://befuck.com/videos/1930535/brunette-chocolate-groans-in-interracial-sexual-ecstasy
    }
}
