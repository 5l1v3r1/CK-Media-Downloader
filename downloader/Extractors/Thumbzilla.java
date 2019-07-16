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
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Thumbzilla extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 4;
    
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
    
    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException , GenericDownloaderException{
        if (!CommonUtils.testPage(url)) throw new PageNotFoundException("Could find video"); //test to avoid error 404
        Document page = getPage(url,false,true);
        
        verify(page);
        
	Elements qualities = page.select("a.qualityButton");
        Map<String,String> quality = new HashMap<>();
        for(int i = 0; i < qualities.size(); i++) {
            String qualityName = Jsoup.parse(qualities.get(i).toString()).body().text();
            String qualityLink = qualities.get(i).attr("data-quality");
            quality.put(qualityName, qualityLink);
        }
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        
        return media;
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        try {
            if (page.select("a.qualityButton").isEmpty()) {
                Elements sections = page.select("section.sectionVideoWrapper");
                Element section = null;
                for(Element s: sections)
                    if (!s.hasClass("videoInfoBottom")) {
                        section = s; break;
                    }
                if(section != null && !section.hasClass("videoInfoBottom")) {
                    if(!section.select("div.notice").isEmpty())
                        throw new VideoDeletedException(section.select("div.notice").get(0).text());
                    else throw new VideoDeletedException("Video removed");
                }
            }
        } catch (NullPointerException e) {
            
        }
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://www.thumbzilla.com/video/search?q="+search;
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("a.js-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            String link = addHost(searchResults.get(i).attr("href"),"www.thumbzilla.com");
            if (!link.matches("https://(www.)?thumbzilla.com/video/([\\S]+)/[\\S]+")) continue;
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false,true));} catch (GenericDownloaderException e) {continue;}
            thequery.addLink(link);
            String thumbLink = searchResults.get(i).select("img").attr("data-src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
            thequery.addPreview(parse(link));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("span.title").toString()).body().text());
            long size; try {size = getSize(link); } catch (GenericDownloaderException | IOException e) { size = -1;}
            thequery.addSize(size);
            thequery.addDuration(getDuration(link).toString());
	}
        return thequery;
    }

    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false);
        
        String thumb = page.select("img.mainImage.playVideo.removeWhenPlaying").attr("src");
        Vector<File> thumbs = new Vector<>();
        
        for(int i = 1; i <= 16; i++) { //there are usually 16 thumbs (according to my tests)
            String link = CommonUtils.changeIndex(thumb,i);
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(link,SKIP)))
                CommonUtils.saveFile(link,CommonUtils.getThumbName(link, SKIP),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(link, SKIP)));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (!CommonUtils.testPage(url)) throw new PageNotFoundException("Could find video"); //test to avoid error 404
        Document page = getPage(url,false);
        verify(page);
        
	return Jsoup.parse(page.select("h1.videoTitle").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (!CommonUtils.testPage(url)) throw new PageNotFoundException("Could find video"); //test to avoid error 404
        Document page = getPage(url,false);
        
        verify(page);
        
        String thumb = page.select("img.mainImage.playVideo.removeWhenPlaying").attr("src");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("ul.responsiveListing").select("li");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a").attr("href"),"www.thumbzilla.com");
            if (!link.matches("https://(www.)?thumbzilla.com/video/([\\S]+)/[\\S]+")) continue;
            String title = li.get(i).select("span.info").select("span.title").text();
            try {v = new video(link,title,downloadThumb(link),getSize(link), getDuration(link).toString()); } catch(Exception e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://www.thumbzilla.com/video/search?q="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
	Elements searchResults = page.select("a.js-thumb");
        int count = searchResults.size(); Random rand = new Random();
        
	while(count-- > 0){
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).attr("href"),"www.thumbzilla.com");
            if (!link.matches("https://(www.)?thumbzilla.com/video/([\\S]+)/[\\S]+")) continue;
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try { verify(getPage(link, false, true)); } catch (GenericDownloaderException e) {continue;}
            String thumbLink = searchResults.get(i).select("img").attr("data-src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try { v = new video(link,Jsoup.parse(searchResults.get(i).select("span.title").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),getSize(link), getDuration(link).toString()); } catch (GenericDownloaderException | IOException e) {}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    private long getSize(String link) throws IOException, GenericDownloaderException {
        if (!CommonUtils.testPage(link)) throw new PageNotFoundException("Could find video"); //test to avoid error 404
        Document page = getPage(link,false,true);
        
        verify(page);
        
	Elements qualities = page.select("a.qualityButton");
        Map<String,String> quality = new HashMap<>();
        for(int i = 0; i < qualities.size(); i++) {
            String qualityName = Jsoup.parse(qualities.get(i).toString()).body().text();
            String qualityLink = qualities.get(i).attr("data-quality");
            quality.put(qualityName, qualityLink);
        }
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        //would have to redirect to ph
        return new GameTime();
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?thumbzilla[.]com/video/(?<id>[\\S]+)/[\\S]+"; 
    }
}
