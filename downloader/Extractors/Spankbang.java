/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.VideoDeletedException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import downloaderProject.MainApp;
import org.jsoup.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author christopher
 */

public class Spankbang extends GenericQueryExtractor implements Playlist{
    //this class will eventually not be static and will have an output stream to send status messages
    private static final int SKIP = 3;
    private String playlistUrl = null;
    
    public Spankbang() { //this contructor is used for when you jus want to query
        
    }
    
    public Spankbang(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isPlaylist(url)) {
            playlistUrl = convertUrl(configureUrl(url));
            this.url = convertUrl(getFirstUrl(url));
        } else
            this.url = convertUrl(configureUrl(url));
        this.videoThumb = downloadThumb(convertUrl(configureUrl(this.url)));
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Spankbang(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isPlaylist(url)) {
            playlistUrl = convertUrl(configureUrl(url));
            this.url = convertUrl(getFirstUrl(url));
        } else
            this.url = convertUrl(configureUrl(url));
        this.videoThumb = thumb;
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Spankbang(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private String getQuality(String s) {
        return CommonUtils.getLink(s,0,'\'');
    }
    
    @Override public MediaDefinition getVideo() throws MalformedURLException, IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException{        
        Document page = getPage(url,false,true);
        verify(page);
                
	//Elements video = page.select("video").select("source");
        Map<String,String> qualities = new HashMap<>();
        qualities.put("240P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_240p")+24)));
        qualities.put("320P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_320p")+24)));
        qualities.put("480P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_480p")+24)));
        qualities.put("720P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_720p")+24)));
        qualities.put("1080P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_1080p")+25)));
        qualities.put("4K",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_4k")+22)));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
     private static void verify(Document page) throws GenericDownloaderException{
        if (page.getElementById("video_removed") != null) 
            throw new VideoDeletedException();
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://spankbang.com/s/"+search+"/";
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,true);
        
	Elements searchResults = page.select("div.video-item");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"))) continue; //test to avoid error 404
            try {verify(getPage("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"),false)); } catch (GenericDownloaderException e) {continue;}
            thequery.addLink("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"));
            String thumbLink = "https:"+searchResults.get(i).select("a.thumb").select("img").attr("data-src"); //src for pc
            if (!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = "https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href");
            Document linkPage = getPage(link,false);
            String video = linkPage.select("video").select("source").attr("src");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(downloadVideoName("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href")));
            thequery.addSize(CommonUtils.getContentSize(video));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Vector<File> thumbs = new Vector<>();
        try {
            Document page = getPage(url,false);
            
            Elements previewImg = page.select("figure.thumbnails").select("img"); //div containing imgs tags
			
            Iterator<Element> img = previewImg.iterator();
            while(img.hasNext()) { //loop through all img tags
               	String thumb = img.next().attr("src");
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                    CommonUtils.saveFile("https:"+thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
            }
                
        } catch(IOException e) {
           throw e; //rethrow exception
        } 
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        
        verify(page);
	Elements title = page.select("div.info").select("h1");
        if(title.attr("title").length() < 1) 
            title = page.select("div.left").select("h1");

	return title.attr("title");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        
        verify(page);
        String thumb = getMetaImage(page).startsWith("http") ? getMetaImage(page) : "https:" + getMetaImage(page);

        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    private static String convertUrl(String url) {
        return url.replace("https://m.", "https://www.");
    }
    
    private static boolean isPlaylist(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if (url.matches("https://(www.)?spankbang.com/[a-zA-Z0-9]+/playlist/[\\S]*"))
            return true;
        else return false;
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Spankbang";
    }

    @Override public video similar() throws IOException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.video-list.video-rotate").get(page.select("div.video-list.video-rotate").size()- 1).select("div.video-item");
        for(int i = 0; i < li.size(); i++) {
        	String link = "https://spankbang.com" + li.get(i).select("a.thumb").attr("href");
        	try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String title = li.get(i).select("div.inf").select("a").text();
            try {if (title.length() < 1) title = downloadVideoName(link);} catch (Exception e) {continue;}
                Document linkPage = getPage(link,false);
                String video = linkPage.select("video").select("source").attr("src");
                try {v = new video(link,title,downloadThumb(link),CommonUtils.getContentSize(video)); }catch(Exception e) {continue;}
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://spankbang.com/s/"+str+"/";
        
        Document page = getPage(searchUrl,true); video v = null;
        
	Elements searchResults = page.select("div.video-item");
        //get first valid video
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"))) continue; //test to avoid error 404
            try {verify(getPage("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"),false)); } catch (GenericDownloaderException e) {continue;}
            String thumbLink = "https:"+searchResults.get(i).select("a.thumb").select("img").attr("data-src"); //src for pc
            if (!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try {
                Document linkPage = getPage("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"),false);
                String video = getDefaultVideo(linkPage);
                v = new video("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"),downloadVideoName("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href")),new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")),CommonUtils.getContentSize(video));
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private static String getFirstUrl(String url) throws IOException {
        Document page = getPage(url,false);
        Elements div = page.select("div.results").select("div.video-item");
        return "https://spankbang.com" + div.get(0).select("a").get(0).attr("href");
    }
    
    @Override
    public boolean isPlaylist() {
        return playlistUrl != null;
    }

    @Override
    public Vector<String> getItems() throws IOException, GenericDownloaderException {
        Document page = getPage(playlistUrl,false);
        Elements div = page.select("div.results").select("div.video-item");
        Vector<String> list = new Vector<>();
        for(int i = 0; i < div.size(); i++)
            list.add("https://spankbang.com" + div.get(i).select("a").get(0).attr("href"));
        return list;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getDefaultVideo(getPage(url,false,true)));
    }
}