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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Drtuber extends GenericExtractor{
    private static final int SKIP = 4;
    
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
    
    private Map<String,String> getQualities() throws PageParseException, IOException {
        Pattern pattern = Pattern.compile("https?://(?:(?:www|m).)?drtuber.com/video/([\\d]+)/[\\S]+");
        Matcher matcher = pattern.matcher(url);
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
        Map<String,String> qualities = getQualities();
        
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

    @Override protected void setExtractorName() {
        extractorName = "Drtuber";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
    	throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        Map<String,String> m = getVideo().iterator().next();
        return CommonUtils.getContentSize(m.get(m.keySet().iterator().next()));
    }
    
    public String getId(String link) {
        Pattern p = Pattern.compile("https://(?:(www|m).)?drtuber.com/video/(?<id>[\\d]+)/[\\S]+");
        Matcher m = p.matcher(link);
        return m.find() ? m.group("id") : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
