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
import downloader.DataStructures.MediaQuality;
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
public class Youporn extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 6;
    
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
    
    private static Map<String, MediaQuality> getQualities(String s) throws PageParseException {
        Map<String, MediaQuality> q = new HashMap<>();
        try {
            JSONArray json = (JSONArray)new JSONParser().parse(s.substring(0,s.indexOf("}];")+2));
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject quality = i.next();
                q.put((String)quality.get("quality"), new MediaQuality(CommonUtils.eraseChar((String)quality.get("videoUrl"),'\\')));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {        
        Document page = getPage(url,false,true);
        //String video = page.getElementById("player-html5").attr("src");
        
        Map<String, MediaQuality> qualities = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition = [{")+18));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities, videoName);
        
        return media;
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        GenericQuery thequery = new GenericQuery();
        String searchUrl = "https://www.youporn.com/search?query="+search.trim().replaceAll(" ", "+");
        
        Elements searchResults = getPage(searchUrl,false).select("div.video-box.four-column.video_block_wrapper");
	for(byte i = 0; i < searchResults.size(); i++) {
            String link = addHost(searchResults.get(i).select("a").attr("href"),"www.youporn.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            thequery.addLink(link);
            String thumb = searchResults.get(i).select("a").select("img").attr("data-thumbnail");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb)));
            thequery.addPreview(parse(link));
            String title = Jsoup.parse(searchResults.get(i).select("div.video-box-title").toString()).body().text();
            thequery.addName(title);
            String video = getPage(link,false).getElementById("player-html5").attr("src");
            thequery.addSize(CommonUtils.getContentSize(video));
            thequery.addDuration(getDuration(link).toString());
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
		
	for(byte i = 1; i < thumbs+1; i++) {
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
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
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
                    link = addHost(a.attr("href"),"youporn.com");
            }
            
            try {
                File thumb = downloadThumb(link);
                v = new video(link,downloadVideoName(link),thumb,getSize(link),getDuration(link).toString());
            } catch (Exception e) {continue;}
            got = true;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.youporn.com/search?query="+str.trim().replaceAll(" ", "+");
        
        Elements searchResults = getPage(searchUrl,false).select("div.video-box.four-column.video_block_wrapper");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).select("a").attr("href"),"youporn.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumb = searchResults.get(i).select("a").select("img").attr("data-thumbnail");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            Document linkPage = getPage(link,false);
            String video = linkPage.getElementById("player-html5").attr("src");
            v = new video(link,Jsoup.parse(searchResults.get(i).select("div.video-box-title").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb)),CommonUtils.getContentSize(video),getDuration(link).toString());
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException{
        Document page = getPage(link,false,true);
        
        Map<String, MediaQuality> qualities = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition = [{")+18));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(link,false));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    private Vector<String> getlist(String select) throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("div.categoriesWrapper").select("a").forEach(a -> {
           if (a.attr("data-espnode").equals(select))
               words.add(a.text());
        });
        return words;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        Vector<String> list = getlist("category_tag");
        list.addAll(getlist("porntag_tag"));
        return list;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return getlist("pornstar_tag");
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?youporn[.]com/watch/(?<id>[\\d]+)/[\\S]+/"; 
        //https://www.youporn.com/watch/13287019/milf-stepmom-madisin-lee-fucks-son-and-gets-creampie-in-gfe-with-mom/
    }
}
