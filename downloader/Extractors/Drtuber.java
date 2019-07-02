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
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
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
        Pattern pattern = Pattern.compile("https?://(?:(?:www|m).)?drtuber.com/video/([\\d]+)/[\\S]+");
        Matcher matcher = pattern.matcher(link);
        if(matcher.matches()) {
           String id = matcher.group(1);
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
        } else throw new PageParseException("Couldnt match video url");
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {        
        Map<String,String> qualities = getQualities(url);
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
	
	return Jsoup.parse(page.select("h1.title").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
       Document page = getPage(url,false);
        String thumb = page.select("video").attr("poster");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws PageParseException, IOException {
        if (url == null) return null;
        Pattern pattern = Pattern.compile("https?://(?:(?:www|m).)?drtuber.com/video/([\\d]+)/[\\S]+");
        Matcher matcher = pattern.matcher(url); video v = null;
        if(matcher.matches()) {
           String id = matcher.group(1);
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
                    try {v = new video(link,title,downloadThumb(link),getSize(link));} catch (Exception e){}
                    got = true;
                }
            } catch (ParseException e) {
               throw new PageParseException(e.getMessage());
            }
        } else throw new PageParseException("Couldnt match video url");
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	String searchUrl = "https://www.drtuber.com/search/videos/"+str.trim().replaceAll(" ", "%20");
        Document page = getPage(searchUrl,false); video v = null;
        Elements searchResults = page.getElementById("search_results").select("a");
        Random rand = new Random(); int count = searchResults.size();
        
	while (count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).attr("href"),"www.drtuber.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            //try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            try {
                v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link));
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

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:(www|m)[.])?drtuber[.]com/video/(?<id>[\\d]+)/[\\S]+"; 
    }
}
