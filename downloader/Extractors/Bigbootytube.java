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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Bigbootytube extends GenericQueryExtractor{
    private static final int SKIP = 4;
    
    public Bigbootytube() { //this contructor is used for when you jus want to query
        
    }
    
    public Bigbootytube(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Bigbootytube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Bigbootytube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        String video = CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url:")+12,'\'');
        Map<String,String> qualities = new HashMap<>(); qualities.put("single",video);
        MediaDefinition media = new MediaDefinition(); media.addThread(qualities,videoName);
        
        return media;
        //super.downloadVideo(video,title,s);
    }
    
    int stop;
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        
        search = search.replaceAll(" ", "+");
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+search;
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            thequery.addLink(searchResults.get(i).attr("href"));
            thequery.addThumbnail(downloadThumb(thequery.getLink(i)));
            String thumbBase = CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37, '\'');
            stop = Integer.parseInt(CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37+thumbBase.length()+3,')'));
            thequery.addPreview(parse(thumbBase));
            thequery.addName(downloadVideoName(thequery.getLink(i)));
            Document linkPage = getPage(searchResults.get(i).attr("href"),false);
            String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\'');
            thequery.addSize(CommonUtils.getContentSize(video));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException{ 
        Vector<File> thumbs = new Vector<>();
        
        for(int i = 1; i <= stop; i++) {
            String thumbLink = url+String.valueOf(i)+".jpg";
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
        }
        
        return thumbs;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
	
	return Jsoup.parse(page.select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        String thumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("preview_url:")+14,'\'');
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.list-thumbs").get(0).select("li");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = li.get(i).select("a.thumb-img").attr("href");
            String thumb = li.get(i).select("a.thumb-img").select("img").attr("src");
            String title = li.get(i).select("div.thumb-title").select("h3").select("a").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                Document linkPage = getPage(link,false);
                String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\'');
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),CommonUtils.getContentSize(video));
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException{
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            try {
                Document linkPage = getPage(searchResults.get(i).attr("href"),false);
                String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\'');
                v = new video(searchResults.get(i).attr("href"),downloadVideoName(searchResults.get(i).attr("href")),downloadThumb(searchResults.get(i).attr("href")),CommonUtils.getContentSize(video));
            } catch(Exception e) { v = null; continue;}
            break; //if u made it this far u already have a vaild video
        }
        return v;        
    }

    @Override protected String getValidURegex() {
        works = true;
        return "https?://(?:www.)?bigbootytube.xxx/videos/(?<id>[\\d]+)/[\\S]+/";
    }
}
