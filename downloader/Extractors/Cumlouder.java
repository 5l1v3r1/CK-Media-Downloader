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
public class Cumlouder extends GenericExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Cumlouder() { //this contructor is used for when you jus want to search
        
    }
    
    public Cumlouder(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Cumlouder(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Cumlouder(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);
        
        return media;
        //super.downloadVideo(video,this.videoName,s);
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
       Document page = getPage(url,false);
        
        return Jsoup.parse(page.select("div.video-top").select("h1").text()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = page.select("video").get(0).attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related_videos").select("div").get(0).select("a.muestra-escena");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).attr("href"),"www.cumlouder.com");
            String thumb = li.get(i).select("img.thumb").attr("data-lazy");
            String title = li.get(i).select("img.thumb").attr("title");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link),getDuration(link).toString());
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	str = str.trim(); str = str.replaceAll(" ", "%20");
    	String searchUrl = "https://www.cumlouder.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.medida").select("a.muestra-escena");
        int count = li.size(); Random rand = new Random();
        
        while(count-- > 0) {
            int i = rand.nextInt(li.size());
            String thumbLink = li.get(i).select("img.thumb").attr("src"); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = addHost(li.get(i).select("a").attr("href"),"www.cumlouder.com");
            String name = li.get(i).select("img.thumb").attr("title");
            if (link.isEmpty() || name.isEmpty()) continue;
            v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link),getDuration(link).toString());
            break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(link, false).select("div.duracion").text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        Elements li = getPage(url, false).select("ul.tags").select("li");
        if (!li.isEmpty())
            li.select("a.tag-link").forEach(c -> words.add(c.text()));
        else return null;
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> stars = new Vector<>();
        Elements ul = getPage(url, false).select("ul.pornstars");
        if (!ul.isEmpty())
            ul.select("a.pornstar-link").forEach(c -> stars.add(c.text()));
        else return null;
        return stars;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?cumlouder[.]com/porn-video/(?<id>[^/]+)/?"; 
    }
}
