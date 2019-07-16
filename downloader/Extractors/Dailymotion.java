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
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PageParseException;
import downloader.Exceptions.PrivateVideoException;
import downloaderProject.MainApp;
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
import org.jsoup.nodes.Element;

/**
 *
 * @author christopher
 */
public class Dailymotion extends GenericExtractor{
    private static final byte SKIP = 1;
    
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
    
    private Map<String,String> getQualities(String src) throws PageParseException {
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
            throw new PageParseException(e.getMessage());
        }
        return links;
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        verify(page); String link = null;
        for(Element i:page.select("meta")) {
            if (i.attr("property").equals("og:video:url")) {
                link = i.attr("content"); break;
            }
        }
        if (link == null) throw new PageNotFoundException("Couldnt get video");
        else {
            page = getPage(link,false,true);
            Map<String, String> qualities = getQualities(page.toString().substring(page.toString().indexOf("var config = {")+13, page.toString().indexOf("};")+1));
        
            MediaDefinition media = new MediaDefinition(); //all the qualities will have the same name
            media.addThread(qualities,videoName);
            
            return media;
            //super.downloadVideo(video,videoName,s);
        }
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
         String title = getTitle(page);
         if (title != null) {
        	 if (title.toLowerCase().contains("- video dailymotion"))
        		 return title.substring(0,title.length() - 20);
        	 else return title;
         } else return "DailyMotion Video";
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        Document page = getPage(url,false);
        verify(page);
        String thumb = getMetaImage(page);
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*@Override public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    	/*str = str.trim(); str = str.replaceAll(" ", "%20");
    	String searchUrl = "https://www.dailymotion.com/search/"+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("section.Video__wrap___2atEf.Video__video___2Qq1K.Video__small-horizontal___203Dv");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("div.Thumbnail__thumbnail___3Aff6.Thumb__thumbnail___1CmQk.Video__thumbnail___2MHSe").select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,2))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,2),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = "https://www.dailymotion.com" + li.get(i).select("a.Video__details___1Knex").attr("href");
        	String name = li.get(i).select("div.Details__title___1qhDj.Video__title___2PurE").text();
                System.out.println("link = "+link+"\nname = "+name);
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,22)));
        	break;
        }
        
        return v;*/
    //}
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(url, false));
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?dailymotion[.]com/video/(?<id>[\\S]+)"; 
    }
}
