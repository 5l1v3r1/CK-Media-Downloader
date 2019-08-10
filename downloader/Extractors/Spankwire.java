/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
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
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Spankwire extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 5;
       
    public Spankwire() { //this contructor is used for when you jus want to query
        
    }
    
    public Spankwire(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Spankwire(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Spankwire(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String searchUrl = "https://spankwire.com/search/straight/keyword/"+search.trim().replaceAll(" ", "%2B");
        
        GenericQuery thequery = new GenericQuery();
	Elements searchResults = getPage(searchUrl,false).select("li.js-li-thumbs");
	for(byte i = 0; i < searchResults.size(); i++)  {
            String link = addHost(searchResults.get(i).select("a").get(0).attr("href"),"spankwire.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            thequery.addLink(link);
            String thumbLink = "https:"+searchResults.get(i).select("img").attr("data-original");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("div.video_thumb_wrapper__thumb-wrapper__title_video").select("a").toString()).body().text());
            long size = -1; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {}
            thequery.addSize(size);
            thequery.addDuration(getDuration(link).toString());
	}
        return thequery;
    }

    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
         Vector<File> thumbs = new Vector<>();
        
        Document page = getPage(url,false);
        
        String mainLink = CommonUtils.getLink(page.toString(),page.toString().indexOf("playerData.timeline_preview_url") + 35, '\'');
        String temp = CommonUtils.getBracket(page.toString(),page.toString().indexOf("timeline_preview_url"));
        int max = Integer.parseInt(temp.substring(1, temp.length()-1));
        
        for(int i = 0; i <= max; i++) {
            long result;
            String link = "https:"+CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,SKIP)))
                result = CommonUtils.saveFile(link, CommonUtils.getThumbName(link,SKIP), MainApp.imageCache);
            else result = -2;
            if (result == -2) {
                File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,SKIP));
                Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
                for(int j = 0; j < split.size(); j++)
                    thumbs.add(split.get(j));
            }
        }
        return thumbs;
    }
    
     private static Map<String,String> getQualities(String link) throws IOException, PageParseException {
        Map<String, String> qualities = new HashMap<>();
        String rawJson = Jsoup.connect("http://www.spankwire.com/api/video/"+getId(link,getRegex())+".json").ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            JSONObject videos = (JSONObject)json.get("videos");
            Iterator<String> i = videos.keySet().iterator();
            while(i.hasNext()) {
                String qualitiy = i.next();
                qualities.put(qualitiy.replace("quality_",""), (String)videos.get(qualitiy));
            }
        } catch (ParseException e) {
               throw new PageParseException(e.getMessage());
        }
        return qualities;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        //Document page = getPage(url,false,true);
	//Map<String,String> qualities = getQualities(page.toString());
        //qualities.put("single", page.select("div.shareDownload_container__item__dropdown").select("a").get(0).attr("href"));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(url),videoName);
        
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException,Exception{
	return Jsoup.parse(getPage(url,false).select("h1").get(0).toString()).body().text().trim();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
       Document page = getPage(url,false);
       String thumb;
        if(page.toString().contains("playerData.poster")) 
            thumb = "https:" + CommonUtils.getLink(page.toString(),page.toString().indexOf("'//",page.toString().indexOf("playerData.poster"))+1, '\'');
        else
            thumb = getMetaImage(page);
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, PageParseException {
    	String rawJson = Jsoup.connect("http://www.spankwire.com/api/video/"+getId(url,getRegex())+".json").ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            String link = addHost((String)((JSONObject)json.get("related")).get("url"),"spankwire.com");
            video v;
            try {v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link), getDuration(link).toString()); }catch (Exception e) { throw new PageParseException("["+this.getClass().getSimpleName()+"]"+e.getMessage());}
            return v;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://spankwire.com/search/straight/keyword/"+str.trim().replaceAll(" ", "%2B");
        
	Elements searchResults = getPage(searchUrl,false).select("li.js-li-thumbs");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).select("a").get(0).attr("href"),"spankwire.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumbLink = configureUrl(searchResults.get(i).select("img").attr("data-original"));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            long size; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {continue;}
            v = new video(link,Jsoup.parse(searchResults.get(i).select("div.video_thumb_wrapper__thumb-wrapper__title_video").select("a").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),size, getDuration(link).toString());
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException{
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(link),videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        String rawJson = Jsoup.connect("http://www.spankwire.com/api/video/"+getId(link,getRegex())+".json").ignoreContentType(true).execute().body();
        GameTime g = new GameTime();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            g.addSec((long)json.get("duration"));
        } catch (ParseException e) {
            CommonUtils.log(e.getMessage(), this);
        }
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }

    @Override protected String getValidRegex() {
        works = true;
        return getRegex();
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        String rawJson = Jsoup.connect("http://www.spankwire.com/api/video/"+getId(url,getRegex())+".json").ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            Iterator<JSONObject> i = ((JSONArray)json.get("categories")).iterator();
            while(i.hasNext())
                words.add((String)i.next().get("name"));
            i = ((JSONArray)json.get("tags")).iterator();
            while(i.hasNext())
                words.add((String)i.next().get("name"));
        } catch (ParseException e) {
            CommonUtils.log(e.getMessage(), this);
        }
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        String rawJson = Jsoup.connect("http://www.spankwire.com/api/video/"+getId(url,getRegex())+".json").ignoreContentType(true).execute().body();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(rawJson);
            Iterator<JSONObject> i = ((JSONArray)json.get("pornstars")).iterator();
            while(i.hasNext())
                words.add((String)i.next().get("name"));
        } catch (ParseException e) {
            CommonUtils.log(e.getMessage(), this);
        }
        return words;
    }
    
    private static String getRegex() {
        return "https?://(?:www[.])?spankwire[.]com/[\\S]+/video(?<id>[\\d]+)/(?:[\\d]+)?";
        //https://www.spankwire.com/Havana-Ginger-and-Maserati-fuck-with-a-strapon-dildo/video25708881/
    }
}
