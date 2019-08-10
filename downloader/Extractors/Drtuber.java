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
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Drtuber extends GenericExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Drtuber() { //this contructor is used for when you jus want to search
        
    }
        
    public Drtuber(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Drtuber(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Drtuber(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String link) throws PageParseException, IOException {
        String id = link.split("/")[4];
        String rawJson = Jsoup.connect("http://www.drtuber.com/player_config_json/"+id+"?vid="+id).ignoreContentType(true).execute().body();
        try {
            Map<String,String> q = new HashMap<>();
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            JSONObject formats = ((JSONObject)json.get("files"));
            Iterator<String> i = formats.keySet().iterator();
            while(i.hasNext()) {
                String format = i.next();
                q.put(format,(String)formats.get(format));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(url),videoName);
        
        return media;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
	return Jsoup.parse(getPage(url,false).select("h1.title").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String thumb = getPage(url,false).select("video").attr("poster");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws PageParseException, IOException {
        if (url == null) return null;
        String id = url.split("/")[4]; video v = null;
        String rawJson = Jsoup.connect("http://www.drtuber.com/player_config_json/"+id+"?vid="+id).ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            JSONArray related = (JSONArray)((JSONObject)json.get("lists")).get("related");
            boolean got = false; Random rand = new Random(); int count = related.size();
            while(count-- > 0) {
                if (got) break;
                JSONObject item = (JSONObject)related.get(rand.nextInt(related.size()));
                String title = String.valueOf(item.get("title"));
                String link = "http://www.drtuber.com/video/"+String.valueOf(item.get("VID")) + "/" + title.replaceAll(" ","-");
                try {v = new video(link,title,downloadThumb(link),getSize(link), getDuration(link).toString());} catch (Exception e){}
                got = true;
            }
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://www.drtuber.com/search/videos/"+str.trim().replaceAll(" ", "%20");

        Elements searchResults = getPage(searchUrl,false).getElementById("search_results").select("a");
        Random rand = new Random(); int count = searchResults.size(); video v = null;
        
	while (count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).attr("href"),"www.drtuber.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            //try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            try {
                v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link),getDuration(link).toString());
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(link),videoName);
        
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException {
        String id = link.split("/")[4]; long secs = 0;
        String rawJson = Jsoup.connect("http://www.drtuber.com/player_config_json/"+id+"?vid="+id).ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            secs = Integer.parseInt(((String)json.get("duration")));
        } catch (ParseException e) {
            CommonUtils.log(e.getMessage(), this);
        }
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException {
        return getDuration(url);
    }
    
    private Vector<String> getlist(String selector) throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select(selector).select("a").forEach(a -> words.add(a.text()));
        return words;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return getlist("div.categories_list");
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return getlist("span.autor.models");
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:(www|m)[.])?drtuber[.]com/video/(?<id>[\\d]+)/[\\S]+"; 
        //https://www.drtuber.com/video/2073066/lucie-wilde-busty
    }
}
