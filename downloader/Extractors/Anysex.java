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
public class Anysex extends GenericExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Anysex() { //this contructor is used for when you jus want to search
        
    }

    public Anysex(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(getPage(url,false,true)),videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        String thumbLink = getMetaImage(getPage(url,false));
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Elements results = getPage(url,false).select("div.watched").select("ul.box").select("li.item");
        
        Random randomNum = new Random(); int count = 0; boolean got = results.isEmpty();
        video v = null; int limit = results.size() > 100 ? results.size() / 10 : results.size() < 10 ? results.size() : results.size() / 5; 
        while(!got) {
            if (count > results.size()) break;
            byte i = (byte)randomNum.nextInt(limit); count++;
            String link = null;
            for(Element a :results.get(i).select("a")) {
                if(a.attr("href").matches("/\\d+/"))
                    link = addHost(a.attr("href"),"anysex.com");
            } 
            
            try {
                File thumb = downloadThumb(link);
                v = new video(link,downloadVideoName(link),thumb,getSize(link), getDuration(link).toString());
            } catch (Exception e) {continue;}
            got = true;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://anysex.com/search/?q="+str.replaceAll(" ", "+");

	Elements lis = getPage(searchUrl,false).select("ul.box").select("li.item"); video v = null;
        Random rand = new Random(); int count = lis.size();
	while(count-- > 0) {
            Element li = lis.get(rand.nextInt(lis.size()));
            if (li.select("a").isEmpty()) continue;
            if (!CommonUtils.testPage(li.select("a").attr("href"))) continue; //test to avoid error 404
            String link = addHost(li.select("a").attr("href"),"anysex.com");
            try { v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link), getDuration(link).toString()); } catch (Exception e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(link, false).select("q").text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        Vector<String> words = new Vector<>();
        if (url != null) {
            Elements metas = getPage(url, false).select("meta");
            for(Element meta :metas)
                if (meta.attr("itemprop").equals("genre"))
                    words.add(meta.attr("content"));
        }
        return words;
    }
    
    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        Vector<String> words = new Vector<>();
        if (url != null) {
            Elements div = getPage(url, false).select("div.info_row.info_row_last");
            if (div.toString().contains("Models:"))
                words.add(div.select("span.last").select("meta").get(0).attr("content"));
        }
        return words;
    }
    
    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?anysex[.]com/(?<id>[\\d]+)/";
    }
}
