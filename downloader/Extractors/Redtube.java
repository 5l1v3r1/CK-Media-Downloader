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
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Redtube extends GenericQueryExtractor{
    private static final int SKIP = 5;
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
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.redtube.com/?search=/"+search+"/";
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
		
	Elements searchResults = page.getElementById("search_results_block").select("li.videoblock_list");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.redtube.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String link = "https://www.redtube.com"+searchResults.get(i).select("a").attr("href");
            String title = Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text();
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            thequery.addLink(link);
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(title);
            try {thequery.addSize(getSize(link));}catch(GenericDownloaderException | IOException e) { thequery.addSize(-1);}
	}
        return thequery;
    }
    
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Vector<File> thumbs = new Vector<File>();
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
        Document page = getPage(url,false);

	return getTitle(page);
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
    
    @Override protected void setExtractorName() {
        extractorName = "Redtube";
    }

    @Override public video similar() throws IOException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related_videos_tab").select("li.videoblock_list");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = "https://www.redtube.com" + li.get(i).select("a").get(0).attr("href");
            String title = li.get(i).select("div.video_title").select("a").text();
                try {v = new video(link,title,downloadThumb(link),getSize(link)); } catch(Exception e) {} 
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.redtube.com/?search=/"+str+"/";
        
        Document page = getPage(searchUrl,false); video v = null;
		
	Elements searchResults = page.getElementById("search_results_block").select("li.videoblock_list");
	for(int i = 0; i < searchResults.size(); i++) {
            if (!CommonUtils.testPage("https://www.redtube.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String thumb = searchResults.get(i).select("span.video_thumb_wrap").select("img").attr("data-thumb_url");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = "https://www.redtube.com"+searchResults.get(i).select("a").attr("href");
            try {v = new video(link,Jsoup.parse(searchResults.get(i).select("div.video_title").toString()).body().text(),new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)),getSize(link)); } catch(GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
        }
        return v;
    }
    
    private static long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
	
	int mediaIndex = page.toString().indexOf("mediaDefinition:");
		
	String defaultVideo = CommonUtils.getBracket(page.toString(),mediaIndex);
	String videoLink = CommonUtils.eraseChar(CommonUtils.getLink(defaultVideo,defaultVideo.indexOf("videoUrl")+11,'"'),'\\');
        return CommonUtils.getContentSize(videoLink);
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
}
