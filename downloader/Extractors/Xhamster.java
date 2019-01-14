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
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
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
public class Xhamster extends GenericQueryExtractor{
	private static final int skip = 5;
    
    public Xhamster() { //this contructor is used for when you jus want to query
        
    }
    
    public Xhamster(String url)throws IOException, SocketTimeoutException, UncheckedIOException , Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, boolean mobile) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(convertUrl(url),thumb,downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb, String videoName){
        super(convertUrl(url),thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException {
        
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
 
	String video = page.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
        Map<String,String> qualities = new HashMap<>();
        qualities.put("single",video); MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);

        return media;
    }

    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://xhamster.com/search?q="+search;
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,false);
        
	Elements searchResults = page.select("div.thumb-list__item.video-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            thequery.addLink(searchResults.get(i).select("a").attr("href"));
            String thumbLink = searchResults.get(i).select("a").select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("div.video-thumb-info").select("a").toString()).body().text());
            Document linkPage = Jsoup.parse(Jsoup.connect(searchResults.get(i).select("a").attr("href")).get().html());
            String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
            thequery.addSize(CommonUtils.getContentSize(video));
	}
        return thequery;
    }
    
     //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException {
        Vector<File> thumbs = new Vector<File>();
        
        Document page = getPage(url,true);
        
        Elements previewImg = page.select("div.item").select("img"); //div containing imgs tags
			
        Iterator<Element> img = previewImg.iterator();
        while(img.hasNext()) { //loop through all img tags
            String thumb = img.next().attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip)));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        return Jsoup.parse(page.select("h1.entity-info-container__title").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,true);
        
	String thumb = page.select("header").select("img").attr("src");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip));
    }
    
    private static String convertUrl(String url) {
        url = url.replace("https://m", "https://www");
        if (url.matches("[\\S]*.m.xhamster.com/videos/[\\S]*"))
            return url.replaceFirst(".m", "");
        else return url;
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Xhamster";
    }

    @Override public video similar() throws IOException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.thumb-list__item.video-thumb");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.size() == 0) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = li.get(i).select("a").get(0).attr("href");
            String thumb = li.get(i).select("img").attr("src");
            String title = li.get(i).select("a.video-thumb-info__name").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                Document linkPage = Jsoup.parse(Jsoup.connect(url).get().html());
 
                String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)),CommonUtils.getContentSize(video));
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://xhamster.com/search?q="+str;
        
        Document page = getPage(searchUrl,false); video v = null;
        
	Elements searchResults = page.select("div.thumb-list__item.video-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String thumbLink = searchResults.get(i).select("a").select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            Document linkPage = Jsoup.parse(Jsoup.connect(url).get().html());
 
                String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
            v = new video(searchResults.get(i).select("a").attr("href"),Jsoup.parse(searchResults.get(i).select("div.video-thumb-info").select("a").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)),CommonUtils.getContentSize(video));
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
}
