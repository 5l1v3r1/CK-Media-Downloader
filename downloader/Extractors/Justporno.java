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
import java.util.HashMap;
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
public class Justporno extends GenericExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Justporno() { //this contructor is used for when you jus want to search
        
    }
    
    public Justporno(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Justporno(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Justporno(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{        
        Document page = getPage(url,false,true);
        
        MediaDefinition media = new MediaDefinition();
        if (!page.select("video").isEmpty())
            media.addThread(getDefaultVideo(page),videoName);
        else {
            String video = CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url: '")+12,'\'');
            Map<String, MediaQuality> qualities = new HashMap<>();
            qualities.put("single", new MediaQuality(video)); 
            media.addThread(qualities,videoName);
        }
        return media;
        //super.downloadVideo(video,title,s);
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        if (!page.select("h1").isEmpty())
            return Jsoup.parse(page.select("h1").toString()).body().text();
        else return Jsoup.parse(page.select("h2").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        //ik this wont be the video thumb ...but its not on page so
        String thumb = addHost(getMatches(page.toString(),"(?<img>//img.justporno.sex/images/\\S+.jpg)", "img").get(0), "");
        /*if (!page.select("video").isEmpty()) {
            thumb = configureUrl(page.select("video").attr("poster"));
            if (thumb.isEmpty()) return null;
        } else 
            thumb = configureUrl(CommonUtils.getLink(page.toString(),page.toString().indexOf("preview_url: '")+14,'\''));*/
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        Elements li = getPage(url,false).select("div.thumb-box").select("li"); 
        Random rand = new Random(); int count = li.size(); video v = null;
        
        while(count-- > 0) {
            byte i = (byte)rand.nextInt(li.size());
            String link = li.get(i).select("a").attr("href");
            String thumb = li.get(i).select("img").attr("src");
            if (thumb.length() < 1) li.get(i).select("img").attr("data-original");
            thumb = thumb.startsWith("http") ? thumb : "https:" + thumb;
            String title = li.get(i).select("p.thumb-item-desc").select("span").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link), "----");
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "http://justporno.tv/search?query="+str.trim().replaceAll(" ", "+");
        
        Elements li = getPage(searchUrl,false).select("ul").select("li"); 
        int count = li.size(); Random rand = new Random(); video v = null;
        
        while (count-- > 0) {
            byte i = (byte)rand.nextInt(li.size());
            String thumbLink = configureUrl(li.get(i).select("img").attr("src")); 
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = li.get(i).select("a").attr("href");
            String name = li.get(i).select("a").attr("title");
            if (link.isEmpty() || name.isEmpty()) continue;
            v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link), "----");
            break;
        }
        
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String, MediaQuality> q;
        
        if (!page.select("video").isEmpty())
            q = getDefaultVideo(page);
        else {
            String video = CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url: '")+12,'\'');
            q = new HashMap<>();
            q.put("single", new MediaQuality(video)); 
        }
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()).getUrl());
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        String[] tokens = getPage(url, false).select("p.ptag").text().split(",");
        for(String s : tokens)
            words.add(s.trim());
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:xxx[.])?justporno[.](?:tv|es)?/[\\S]+/(?<id>[\\d]+)/[\\S]+";
        //https://justporno.tv/hd/6876/black_booty
    }
}
