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
public class Redtube extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 5;
    
    public Redtube() { //this contructor is used for when you jus want to query
        
    }
    
    public Redtube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Redtube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Redtube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> q = new HashMap<>();
        try {
            JSONArray json = (JSONArray)new JSONParser().parse(s.substring(0,s.indexOf("}],")+2));
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject temp = i.next();
                if (!((String)temp.get("videoUrl")).isEmpty())
                    q.put((String)temp.get("quality"), (String)temp.get("videoUrl"));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        
	int mediaIndex = page.toString().indexOf("mediaDefinition:");
        Map<String,String> qualities = getQualities(page.toString().substring(mediaIndex+17));
		
	//String defaultVideo = CommonUtils.getBracket(page.toString(),mediaIndex);
	//String videoLink = CommonUtils.eraseChar(CommonUtils.getLink(defaultVideo,defaultVideo.indexOf("videoUrl")+11,'"'),'\\');
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities, videoName);
        return media;
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String searchUrl = "https://www.redtube.com/?search=/"+search.trim().replaceAll(" ", "+")+"/";
        GenericQuery thequery = new GenericQuery();
        
	Elements searchResults = getPage(searchUrl,false).getElementById("search_results_block").select("li.videoblock_list");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage(addHost(searchResults.get(i).select("a").attr("href"),"www.redtube.com"))) continue; //test to avoid error 404
            String link = addHost(searchResults.get(i).select("a").attr("href"),"www.redtube.com");
            String title = Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text();
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
            thequery.addDuration(getDuration(link).toString());
            try {thequery.addSize(getSize(link));}catch(GenericDownloaderException | IOException e) { thequery.addSize(-1);}
	}
        return thequery;
    }
    
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Vector<File> thumbs = new Vector<>();
        Document page = getPage(url,false);
        
	int thumbIndex = page.toString().indexOf("thumbs:");
	String definition = CommonUtils.getBracket(page.toString(),thumbIndex, 1);
	String mainLink = CommonUtils.eraseChar(CommonUtils.getLink(definition,definition.indexOf("urlPattern:")+13,'"'),'\\');
	String temp = CommonUtils.getBracket(definition,definition.indexOf("urlPattern:"));
	
	int max = Integer.parseInt(temp.substring(1, temp.length()-1));
	int width = CommonUtils.getThumbs(definition,definition.indexOf("thumbWidth"),',');
	int height = CommonUtils.getThumbs(definition,definition.indexOf("thumbHeight"),',');
	for(int i = 0; i <= max; i++) {
            String link = CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,SKIP)))
                CommonUtils.saveFile(link, CommonUtils.getThumbName(link,SKIP), MainApp.imageCache);
            File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,SKIP));
            Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
            for(int j = 0; j < split.size(); j++)
                thumbs.add(split.get(j));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
	return getTitle(getPage(url,false));
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        int posterIndex = page.toString().indexOf("poster:");
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(), posterIndex+9, '"'),'\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP+2))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP+2),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP+2));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        
        Elements li = getPage(url,false).getElementById("related_videos_tab").select("li.videoblock_list");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            byte i = (byte)randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a").get(0).attr("href"),"www.redtube.com");
            String title = li.get(i).select("div.video_title").select("a").text();
            try {v = new video(link,title,downloadThumb(link),getSize(link),getDuration(link).toString()); } catch(Exception e) {} 
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.redtube.com/?search=/"+str.trim().replaceAll(" ", "+")+"/";
        	
	Elements searchResults = getPage(searchUrl,false).getElementById("search_results_block").select("li.videoblock_list");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            if (!CommonUtils.testPage(addHost(searchResults.get(i).select("a").attr("href"),"www.redtube.com"))) continue; //test to avoid error 404
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = addHost(searchResults.get(i).select("a").attr("href"),"www.redtube.com");
            try {v = new video(link,Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text(),new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link), getDuration(link).toString()); } catch(GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
        }
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        
	int mediaIndex = page.toString().indexOf("mediaDefinition:");
        Map<String,String> qualities = getQualities(page.toString().substring(mediaIndex+17));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities, videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(link, false));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("div.video-infobox-content").select("a").forEach(a -> {
            if (a.attr("href").matches("/tag/[\\S]+|/redtube/[\\S]+"))
                words.add(a.text());
        });
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("div.video-infobox-content").select("a").forEach(a -> {
            if (a.toString().matches("<a href=\"/pornstar/[\\S]+\">[\\S]+</a>"))
                words.add(a.text());
        });
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?redtube[.]com/(?<id>[\\S]+)"; 
    }
}
