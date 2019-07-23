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
public class Tube8 extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 4;
    
    public Tube8() { //this contructor is used for when you jus want to query
        
    }
    
    public Tube8(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Tube8(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Tube8(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String searchUrl = "https://www.tube8.com/searches.html?q=/"+search.trim().replaceAll(" ", "+")+"/";
        GenericQuery thequery = new GenericQuery();
        
	Elements searchResults = getPage(searchUrl,false).select("div.video_box");
	for(byte i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage(searchResults.get(i).select("p.video_title").select("a").attr("href"))) continue; //test to avoid error 404
            String link = searchResults.get(i).select("p.video_title").select("a").attr("href");
            String title = searchResults.get(i).select("p.video_title").select("a").attr("title");
            String thumb = searchResults.get(i).select("div.videoThumbsWrapper").select("img").attr("data-thumb");
            CommonUtils.log("thumb: "+thumb,this);
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
            long size; try { size = getSize(link); } catch (GenericDownloaderException | IOException e) {size = -1;}
            thequery.addSize(size);
            thequery.addDuration(getDuration(link).toString());
	}
        return thequery;
    }

    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Vector<File> thumbs = new Vector<>();
        
        Document page = getPage(url,false);
        
        String mainLink = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(),page.toString().indexOf("timeline_preview_url",page.toString().indexOf("var flashvars"))+23,'"'),'\\');
        String temp = CommonUtils.getBracket(page.toString(),page.toString().indexOf("timeline_preview_url",page.toString().indexOf("var flashvars")));
        int max = Integer.parseInt(temp.substring(1, temp.length()-1));
        
        for(byte i = 0; i <= max; i++) {
            String link = CommonUtils.replaceIndex(mainLink,i,String.valueOf(max));
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,SKIP+1)))
                CommonUtils.saveFile(link, CommonUtils.getThumbName(link,SKIP+1), MainApp.imageCache);
            File grid = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link,SKIP+1));
            Vector<File> split = CommonUtils.splitImage(grid, 5, 5, 0, 0);
            for(int j = 0; j < split.size(); j++)
                thumbs.add(split.get(j));
        }
        return thumbs;
    }
    
    private static Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> q = new HashMap<>();
        try {
            JSONArray json = (JSONArray)new JSONParser().parse(s.substring(0,s.indexOf("}],")+2));
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject temp = i.next();
                q.put(String.valueOf(temp.get("quality")), (String)temp.get("videoUrl"));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        Map<String, String> quality = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition")+17));
       
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        
        return media;   
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
	return Jsoup.parse(getPage(url,false).select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = CommonUtils.eraseChar(CommonUtils.getLink(page.toString(), page.toString().indexOf("image_url",page.toString().indexOf("var flashvars")) + 12,'\"'), '\\');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        Elements li = getPage(url,false).select("div.gridList.videosList").select("div.video_box");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = li.get(i).select("a").get(0).attr("href");
            String title = li.get(i).select("p.video_title").select("a").text();
            try {v = new video(link,title,downloadThumb(link),getSize(link), getDuration(link).toString());}catch(Exception e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.tube8.com/searches.html?q=/"+str.trim().replaceAll(" ", "+")+"/";
        
	Elements searchResults = getPage(searchUrl,false).select("div.video_box");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            if (!CommonUtils.testPage(searchResults.get(i).select("p.video_title").select("a").attr("href"))) continue; //test to avoid error 404
            String link = searchResults.get(i).select("p.video_title").select("a").attr("href");
            String title = searchResults.get(i).select("p.video_title").select("a").attr("title");
            String thumb = searchResults.get(i).select("div.videoThumbsWrapper").select("img").attr("data-thumb");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try { v = new video(link,title,new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link), getDuration(link).toString()); } catch (GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String, String> quality = getQualities(page.toString().substring(page.toString().indexOf("mediaDefinition")+17));
       
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        return getMetaDuration(getPage(link,false));
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("li.video-tag").select("a").forEach(a -> words.add(a.text()));
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?tube8[.]com/[\\S]+/[\\S]+/(?<id>[\\d]+)/"; 
    }
}
