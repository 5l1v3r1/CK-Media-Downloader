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
public class Homemoviestube extends GenericExtractor implements Searchable{
    private static final byte SKIP = 5;
    
    public Homemoviestube() { //this contructor is used for when you jus want to search
        
    }

    public Homemoviestube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Homemoviestube(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Homemoviestube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        String thumbLink = addHost(getMetaImage(getPage(url,false)),"");
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        
        
        Elements li = getPage(url,false).getElementById("videoTabs").getElementById("ctab1").select("div.film-item.col-lg-4.col-md-75.col-sm-10.col-xs-10");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            byte i = (byte)randomNum.nextInt(li.size()); count++;
            String link = li.get(i).select("a").get(0).attr("href");
            if (link.matches(getValidRegex()))
                try {v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link), "----"); } catch(Exception e) {continue;}
            else continue;
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "http://www.homemoviestube.com/search/"+str.replaceAll(" ", "-")+"/page1.html";

	Elements divs = getPage(searchUrl,false).select("div.film-item.col-lg-4.col-md-75.col-sm-10.col-xs-10"); video v = null;
        int count = divs.size(); Random rand = new Random();
        
	while(count-- > 0) {
            Element div = divs.get(rand.nextInt(divs.size()));
            if (div.select("a").isEmpty()) continue;
            if (!div.select("a").get(0).attr("href").matches(getValidRegex())) continue;         
            if (!CommonUtils.testPage(div.select("a").get(0).attr("href"))) continue; //test to avoid error 404
            String link = div.select("a").get(0).attr("href");
            try { v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link), "----"); } catch (Exception e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(url,false).select("span.desc").text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        Elements a = getPage(url, false).select("ul.item-list-stat").select("a");
        Vector<String> words = new Vector<>();
        a.forEach(s -> {
           if (s.attr("href").matches("//www.homemoviestube.com/search/[\\S]+/page1.html|/channels/[\\d]+/[\\S]+/page1.html"))
               words.add(s.text());
        });
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?homemoviestube[.]com/videos/(?<id>[\\d]+)/[\\S]+[.]html"; 
        //https://www.homemoviestube.com/videos/339466/busty-girl-eye-contact-orgasm.html
    }
}
