/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PrivateVideoException;
import static downloader.Extractors.GenericExtractor.configureUrl;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
public class Dailymotion extends GenericExtractor{
    
    public Dailymotion() { //this contructor is used for when you jus want to search
        
    }
    
    public Dailymotion(String url)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Dailymotion(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Dailymotion(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String src) {
        Map<String, String> links = new HashMap<>();
        
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(src);
            JSONObject qualities = (JSONObject) ((JSONObject)json.get("metadata")).get("qualities");
            Iterator i = qualities.keySet().iterator();
            while(i.hasNext()) {
                String q = (String)i.next();
                if(q.equals("auto")) continue; 
                links.put(q, (String)((JSONObject)((JSONArray)qualities.get(q)).get(1)).get("url"));
            }
        } catch (ParseException e) {
            System.out.println("error parsing "+url);
        }
        return links;
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.pcClient).get().html());
        verify(page);
        Map<String, String> qualities = getQualities(page.toString().substring(page.toString().indexOf("var __PLAYER_CONFIG__ = {")+24, page.toString().indexOf("};")+1));
        int max = 144;
        Iterator i = qualities.keySet().iterator();
        while(i.hasNext()) {
            int temp = Integer.parseInt((String)i.next());
            if(temp > max)
                max = temp;
        }
        
	String video = qualities.get(String.valueOf(max));
        
        super.downloadVideo(video,downloadVideoName(url),s);
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        String title = Jsoup.parse(page.select("title").toString()).text();
        
        if(title.equals("Private Video - dailymotion"))
            throw new PrivateVideoException();
        if(title.equals("404 Page not found - Dailymotion"))
            throw new PageNotFoundException();
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
         verify(page);
         Elements meta = page.select("meta"); String title = null;
         for(int i = 0; i < meta.size(); i++) {
             if (meta.get(i).attr("property").equals("og:title")) {
                 title = meta.get(i).attr("content"); break;
             }
         }
	return title == null ? "DailyMotion Video" : title.substring(0,title.indexOf("- Video Dailymotion")-1);
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
       Document page = getPage(url,false);
        verify(page);
        Elements meta = page.select("meta"); String thumb = null;
         for(int i = 0; i < meta.size(); i++) {
             if (meta.get(i).attr("property").equals("og:image")) {
                 thumb = meta.get(i).attr("content"); break;
             }
         }
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,1))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,1),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,1));
    }

    @Override
    protected void setExtractorName() {
        extractorName = "Dailymotion";
    }

    @Override
    public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public video search(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
