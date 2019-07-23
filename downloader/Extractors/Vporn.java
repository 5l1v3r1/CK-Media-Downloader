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
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
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
public class Vporn extends GenericExtractor implements Searchable{
    private static final byte SKIP = 3;
    
    public Vporn() { //this contructor is used for when you jus want to search
        
    }
    
    public Vporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Vporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Vporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }


    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
	Elements rawQualities = getPage(url,false,true).getElementById("vporn-video-player").select("source");
	Map<String,String> qualities = new HashMap<>();
		
	for(byte i = 0; i < rawQualities.size(); i++)
            qualities.put(rawQualities.get(i).attr("label"),rawQualities.get(i).attr("src"));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
     private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        return Jsoup.parse(getPage(url,false).select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        String thumb = getPage(url,false).getElementById("vporn-video-player").attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        video v = null;
        Elements li = getPage(url,false).select("div.thumblist.videos").select("div.video");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = li.get(i).select("a.links").attr("href");
            String thumb = li.get(i).select("img").attr("src");
            String title = li.get(i).select("span.cwrap").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                try { v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link),getDuration(link).toString()); } catch (GenericDownloaderException | IOException e) {}
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://www.vporn.com/search?q="+str.trim().replaceAll(" ", "+");
        
        //is not a an actually li but...
        Elements li = getPage(searchUrl,false).select("div.thumblist.videos").select("div.video");
        int count = li.size(); Random rand = new Random(); video v = null;
        
        while(count-- >0) {
            int i = rand.nextInt(li.size());
            String thumbLink = li.get(i).select("img.imgvideo").attr("src");
            String link= li.get(i).select("a").attr("href");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String name = li.get(i).select("div.thumb-info").select("span").get(0).text();
            if (link.isEmpty() || name.isEmpty()) continue;
            try { v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link), getDuration(link).toString()); } catch(GenericDownloaderException | IOException e) {}
            break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
	Elements rawQualities = getPage(link,false,true).getElementById("vporn-video-player").select("source");
	Map<String,String> qualities = new HashMap<>();
		
	for(byte i = 0; i < rawQualities.size(); i++)
            qualities.put(rawQualities.get(i).attr("label"),rawQualities.get(i).attr("src"));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(link,false).select("span.video-duration.hide-sm-down").text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    private Vector<String> getlinks(String regex) throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("p.tags_p").select("a").forEach(a -> {
            if (a.attr("href").matches(regex))
                words.add(a.text());
        });
        return words;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return getlinks("/[\\S]+/");
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return getlinks("https://www.vporn.com/[\\S]+/");
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?vporn[.]com/[\\S]+/[\\S]+/(?<id>[\\d]+)/"; 
    }
}
