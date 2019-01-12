/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Thumbzilla extends GenericQueryExtractor{
    private static final int skip = 4;
    
    public Thumbzilla() { //this contructor is used for when you jus want to query
        
    }
    
    public Thumbzilla(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Thumbzilla(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Thumbzilla(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException, UncheckedIOException, Exception{
        if (s != null) s.startTiming();
        
        Document page =  Jsoup.parse(Jsoup.connect(url).get().html());
                
	String title = Jsoup.parse(page.select("h1.videoTitle").toString()).body().text();
	Elements qualities = page.select("a.qualityButton");
        Map<String,String> quality = new HashMap<>();
        for(int i = 0; i < qualities.size(); i++) {
            String qualityName = Jsoup.parse(qualities.get(i).toString()).body().text();
            String qualityLink = qualities.get(i).attr("data-quality");
            quality.put(qualityName, qualityLink);
        }
        
        String video; //quality link
        if (quality.containsKey("720P"))
            video = quality.get("720P");
        else if (quality.containsKey("480P"))
            video = quality.get("480P");
        else if (quality.containsKey("1080P"))
            video = quality.get("1080P");
        else video = quality.get("240P");
        
        super.downloadVideo(video,title,s);
    }
    
    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.thumbzilla.com/video/search?q="+search;
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("a.js-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://www.thumbzilla.com"+searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            thequery.addLink("https://www.thumbzilla.com"+searchResults.get(i).attr("href"));
            String thumbLink = searchResults.get(i).select("img").attr("data-src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)));
            thequery.addPreview(parse("https://www.thumbzilla.com"+searchResults.get(i).attr("href")));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("span.title").toString()).body().text());
            long size; try {size = getSize("https://www.thumbzilla.com"+searchResults.get(i).attr("href")); } catch (GenericDownloaderException | IOException e) { size = -1;}
            thequery.addSize(size);
	}
        return thequery;
    }

    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException {
        Document page = getPage(url,false);
        
        String thumb = page.select("img.mainImage.playVideo.removeWhenPlaying").attr("src");
        Vector<File> thumbs = new Vector<>();
        
        for(int i = 1; i <= 16; i++) { //there are usually 16 thumbs (according to my tests)
            String link = CommonUtils.changeIndex(thumb,i);
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,skip)))
                CommonUtils.saveFile(link,CommonUtils.getThumbName(link, skip),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link, skip)));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
	return Jsoup.parse(page.select("h1.videoTitle").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = page.select("img.mainImage.playVideo.removeWhenPlaying").attr("src");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip));
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Thumbzilla";
    }

    @Override
    public video similar() throws IOException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("ul.responsiveListing").select("li");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = "https://www.thumbzilla.com" + li.get(i).select("a").attr("href");
            String title = li.get(i).select("span.info").select("span.title").text();
                try {v = new video(link,title,downloadThumb(link),getSize(link)); } catch(Exception e) {continue;}
                break;
            }
        return v;
    }

    @Override
    public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.thumbzilla.com/video/search?q="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("a.js-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://www.thumbzilla.com"+searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            String thumbLink = searchResults.get(i).select("img").attr("data-src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = "https://www.thumbzilla.com"+searchResults.get(i).attr("href");
            try { v = new video("https://www.thumbzilla.com"+searchResults.get(i).attr("href"),Jsoup.parse(searchResults.get(i).select("span.title").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)),getSize(link)); } catch (GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    private static long getSize(String link) throws IOException, GenericDownloaderException {
        Document page =  Jsoup.parse(Jsoup.connect(link).get().html());
        Elements qualities = page.select("a.qualityButton");
        Map<String,String> quality = new HashMap<>();
        for(int i = 0; i < qualities.size(); i++) {
            String qualityName = Jsoup.parse(qualities.get(i).toString()).body().text();
            String qualityLink = qualities.get(i).attr("data-quality");
            quality.put(qualityName, qualityLink);
        }
        
        String video; //quality link
        if (quality.containsKey("720P"))
            video = quality.get("720P");
        else if (quality.containsKey("480P"))
            video = quality.get("480P");
        else if (quality.containsKey("1080P"))
            video = quality.get("1080P");
        else video = quality.get("240P");
        return CommonUtils.getContentSize(video);
    }
    
    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
        
}
