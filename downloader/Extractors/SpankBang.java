/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.VideoDeletedException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import org.jsoup.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

/**
 *
 * @author christopher
 */

public class SpankBang extends GenericQueryExtractor{
    //this class will eventually not be static and will have an output stream to send status messages
	private static final int skip = 3;
    
    public SpankBang() { //this contructor is used for when you jus want to query
        
    }
    
    public SpankBang(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public SpankBang(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(convertUrl(url),thumb,downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public SpankBang(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public void getVideo(OperationStream s) throws MalformedURLException, IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (s != null) s.startTiming();
        
        Document page =  Jsoup.parse(Jsoup.connect(url).get().html());
        verify(page);
                
	Elements video = page.select("video").select("source");
        
        super.downloadVideo(video.attr("src"),videoName,s);
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
            Document linkPage =  Jsoup.parse(Jsoup.connect(link).get().html());
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
        Vector<File> thumbs = new Vector<File>();
        try {
            
            Document page = getPage(url,false);
            
            Elements previewImg = page.select("figure.thumbnails").select("img"); //div containing imgs tags
			
            Iterator<Element> img = previewImg.iterator();
            while(img.hasNext()) { //loop through all img tags
               	String thumb = img.next().attr("src");
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                    CommonUtils.saveFile("https:"+thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip)));
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
        Elements metas = page.select("meta");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(metas.get(4).attr("content"),skip))) //if file not already in cache download it
            CommonUtils.saveFile("https:"+metas.get(4).attr("content"),CommonUtils.getThumbName(metas.get(4).attr("content"),skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(metas.get(4).attr("content"),skip));
    }
    
    private static String convertUrl(String url) {
        return url.replace("https://m.", "https://www.");
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Spankbang";
    }

    @Override
    public video similar() throws IOException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.video-list.video-rotate").get(page.select("div.video-list.video-rotate").size()- 1).select("div.video-item");
        for(int i = 0; i < li.size(); i++) {
        	String link = "https://spankbang.com" + li.get(i).select("a.thumb").attr("href");
        	try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String title = li.get(i).select("div.inf").select("a").text();
            try {if (title.length() < 1) title = downloadVideoName(link);} catch (Exception e) {continue;}
                Document linkPage =  Jsoup.parse(Jsoup.connect(link).get().html());
                String video = linkPage.select("video").select("source").attr("src");
                try {v = new video(link,title,downloadThumb(link),CommonUtils.getContentSize(video)); }catch(Exception e) {continue;}
                break;
            }
        return v;
    }

    @Override
    public video search(String str) throws IOException {
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
                Document linkPage =  Jsoup.parse(Jsoup.connect("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href")).get().html());
                String video = linkPage.select("video").select("source").attr("src");
                v = new video("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href"),downloadVideoName("https://spankbang.com"+searchResults.get(i).select("a.thumb").attr("href")),new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")),CommonUtils.getContentSize(video));
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        Document page =  Jsoup.parse(Jsoup.connect(url).get().html());
        verify(page);
                
	return CommonUtils.getContentSize(page.select("video").select("source").attr("src"));
    }
}