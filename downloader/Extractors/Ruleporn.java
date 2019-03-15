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
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Ruleporn extends GenericQueryExtractor{
    
    public Ruleporn() { //this contructor is used for when you jus want to query
        
    }
    
    public Ruleporn(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Ruleporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Ruleporn(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);
        
        return media;
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if ((page.select("video") == null) || (page.select("video").isEmpty()))
            throw new VideoDeletedException("No video found");
        else if (page.select("video").attr("poster").length() < 1)
            throw new VideoDeletedException("No video found");
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "-");
        String searchUrl = "https://ruleporn.com/search/"+search+"/";
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.row").select("div.item-inner-col.inner-col");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).select("a").get(0).attr("href"))) continue; //test to avoid error 404
            try {verify(getPage(searchResults.get(i).select("a").get(0).attr("href"),false));} catch (GenericDownloaderException e) {continue;}
            thequery.addLink(searchResults.get(i).select("a").get(0).attr("href"));
            String thumbLink = searchResults.get(i).select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(searchResults.get(i).select("span.title").text());
            thequery.addSize(getSize(searchResults.get(i).select("a").get(0).attr("href")));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{ 
        Vector<File> thumbs = new Vector<>();
        
        try {
            Document page = getPage(url,true,false);
            String base = page.select("video").attr("poster");
            for(int i = 0; i < 11; i++) {
                String thumb = base.substring(0,base.indexOf("-")+1) + String.valueOf(i) + base.substring(base.indexOf("-")+2);
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
                    CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb)));		
            }
        } catch (NullPointerException e) {
            return thumbs; //return parse(url); //possible it went to interstitial? instead
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        return page.select("div.item-tr-inner-col.inner-col").get(0).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
       Document page = getPage(url,false);
        
        String thumb = page.select("video").attr("poster");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Ruleporn";
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.row").select("div.item-col.col");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.size() == 0) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = li.get(i).select("a").attr("href");
        	try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String thumb = li.get(i).select("span.image").select("img").attr("src");
            String title = li.get(i).select("span.title").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb)),getSize(link));
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        str = str.trim(); 
        str = str.replaceAll(" ", "-");
        String searchUrl = "https://ruleporn.com/search/"+str+"/";
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("div.row").select("div.item-inner-col.inner-col");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).select("a").get(0).attr("href"))) continue; //test to avoid error 404
            try {verify(getPage(searchResults.get(i).select("a").get(0).attr("href"),false));} catch (GenericDownloaderException e) {continue;}
            String thumbLink = searchResults.get(i).select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = searchResults.get(i).select("a").get(0).attr("href");
            v = new video(link,searchResults.get(i).select("span.title").text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink)),getSize(link));
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException{
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }

    @Override public long getSize() throws IOException, GenericDownloaderException{
        return getSize(url);
    }
    
    @Override public String getId() {
        Pattern p = Pattern.compile("https://(www.)?ruleporn.com/([\\S]+)/");
        return p.matcher(url).group(2);
    }
}
