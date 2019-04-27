/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Youporn extends GenericQueryExtractor{
    
    public Youporn() { //used to when you only want to query
        
    }
    
    public Youporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Youporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Youporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> q = new HashMap<>();
        try {
            JSONArray json = (JSONArray)new JSONParser().parse(s.substring(0,s.indexOf("}];")+2));
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject quality = i.next();
                q.put((String)quality.get("quality"), CommonUtils.eraseChar((String)quality.get("videoUrl"),'\\'));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {        
        Document page = getPage(url,false,true);
        //String video = page.getElementById("player-html5").attr("src");
        
        Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition = [{")+18));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        GenericQuery thequery = new GenericQuery();
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.youporn.com/search?query="+search;
        
         Document page = getPage(searchUrl,false);
        
        Elements searchResults = page.select("div.video-box.four-column.video_block_wrapper");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            thequery.addLink("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"));
            String thumb = searchResults.get(i).select("a").select("img").attr("data-thumbnail");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb)));
            thequery.addPreview(parse("https://www.youporn.com"+searchResults.get(i).select("a").attr("href")));
            String title = Jsoup.parse(searchResults.get(i).select("div.video-box-title").toString()).body().text();
            thequery.addName(title);
            Document linkPage = getPage("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"),false);
            String video = linkPage.getElementById("player-html5").attr("src");
            thequery.addSize(CommonUtils.getContentSize(video));
	}
        return thequery;
    }
    
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Vector<File> thumb = new Vector<>();
         Document page = getPage(url,true);
		
	int small = page.toString().indexOf("videoFlipBookThumbnailSmall");
	int big = page.toString().indexOf("videoFlipBookThumbnailLarge");
	int num = page.toString().indexOf("numberOfThumbnails");
	String smallLink = CommonUtils.getLink(page.toString(),small+31,'"');
	String bigLink = CommonUtils.getLink(page.toString(),big+31,'"');
	int thumbs = CommonUtils.getThumbs(page.toString(),num+20,';');
		
	for(int i = 1; i < thumbs+1; i++) {
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(CommonUtils.replaceIndex(smallLink,i,"index")))) //if file not already in cache download it
                CommonUtils.saveFile(CommonUtils.replaceIndex(smallLink,i,"index"), CommonUtils.getThumbName(CommonUtils.replaceIndex(smallLink,i,"index")),MainApp.imageCache);
            thumb.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(CommonUtils.replaceIndex(smallLink,i,"index"))));
        }
        return thumb;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);

	return Jsoup.parse(page.select("div.watchVideoTitle").select("h1.heading2").toString()).body().text();
    } 

    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
        
	String thumb = page.getElementById("player-html5").attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,6))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,6),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,6));
    }
    
    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        
        //String which = new Random().nextInt(2) == 1 ? "relatedVideos" : "recommendedVideos";
        String which = "relatedVideos";
        
        Elements related = getPage(url,false).getElementById(which).select("div.video-box.four-column.video_block_wrapper");
        Random randomNum = new Random(); int count = 0; boolean got = related.isEmpty();
        video v = null;
        while(!got) {
            if (count > related.size()) break;
            int i = randomNum.nextInt(related.size()); count++;
            String link = null;
            for(Element a :related.get(i).select("a")) {
                if(a.attr("href").matches("/watch/\\d+/\\S+/"))
                    link = "http://youporn.com" + a.attr("href");
            }
            
            try {
                File thumb = downloadThumb(link);
                v = new video(link,downloadVideoName(link),thumb,getSize(link));
            } catch (Exception e) {continue;}
            got = true;
        }
        return v;
    }

    @Override  public video search(String str) throws IOException, GenericDownloaderException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.youporn.com/search?query="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
        Elements searchResults = page.select("div.video-box.four-column.video_block_wrapper");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String thumb = searchResults.get(i).select("a").select("img").attr("data-thumbnail");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            Document linkPage = getPage("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"),false);
            String video = linkPage.getElementById("player-html5").attr("src");
            v = new video("https://www.youporn.com"+searchResults.get(i).select("a").attr("href"),Jsoup.parse(searchResults.get(i).select("div.video-box-title").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb)),CommonUtils.getContentSize(video));
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException{
        Document page = getPage(link,false,true);
        
        Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition = [{")+18));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return getSize(media);
    }

    @Override protected String getValidURegex() {
        works = true;
        return "https?://(?:www.)?youporn.com/watch/(?<id>[\\d]+)/[\\S]+/"; 
    }
}
