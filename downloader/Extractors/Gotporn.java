/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Gotporn extends GenericExtractor implements Searchable{
    private static final byte SKIP = 3;
    
    public Gotporn() { //this contructor is used for when you jus want to search
        
    }
    
    public Gotporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Gotporn(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Gotporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        return getH1Title(getPage(url,false));
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String thumb = getMetaImage(getPage(url,false));
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPageCookie(url,false,true);
        
        Map<String,String> qualities = getDefaultVideo(page);
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Elements related = getPage(url, false).getElementById("videoRelatedVideos").select("a.video-item-link.gravity-push");
        boolean got = false; Random rand = new Random(); int count = related.size(); video v = null;
        while(count-- > 0) {
            if (got) break;
            Element item = related.get(rand.nextInt(related.size()));
            String title = String.valueOf(item.attr("data-title"));
            if (title == null || title.isEmpty())
                title = item.select("span.text").text();
            String link = item.attr("href");
            //String thumb = item.select("img").attr("src");
            GameTime g = new GameTime();
            g.addSec(getSeconds(item.select("span.duration").text()));
            try {v = new video(link,title, downloadThumb(link), getSize(link), g.toString());} catch (Exception e){}
            got = true;
        }
        return v;
    }
    
    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://www.gotporn.com/results?search_query="+str.trim().replaceAll(" ", "+");

        Elements searchResults = getPage(searchUrl,false).select("li.video-item.poptrigger").select("a.video-item-link.gravity-push");
        Random rand = new Random(); int count = searchResults.size(); video v = null;
        
	while (count-- > 0) {
            Element item = searchResults.get(rand.nextInt(searchResults.size()));
            String link = item.attr("href");
            String title = item.attr("data-title");
            if (title == null || title.isEmpty())
                title = item.select("span.text").text();
            GameTime g = new GameTime();
            g.addSec(getSeconds(item.select("span.duration").text()));
            try {
                v = new video(link,title,downloadThumb(link),getSize(link),g.toString());
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPageCookie(link,false,true);
        
        Map<String,String> qualities = getDefaultVideo(page);
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return getSize(media, CommonUtils.StringCookies(cookieJar));
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("ul.suggestions-container.tag-suggestions").select("a").forEach(a -> words.add(a.text()));
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?gotporn[.]com/[\\S]+/video-(?<id>[\\d]+)";
        //https://www.gotporn.com/busty-ebony-banged-outdoors-after-titfuck/video-7097722
    }
}
