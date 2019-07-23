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
public class Bigbootytube extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 4;
    
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
        String video = addHost(CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url:")+12,'\''), "");
        Map<String,String> qualities = new HashMap<>(); qualities.put("single",video);
        MediaDefinition media = new MediaDefinition(); media.addThread(qualities,videoName);
        
        return media;
        //super.downloadVideo(video,title,s);
    }
    
    int stop;
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+search.trim().replaceAll(" ", "+");
        
        GenericQuery thequery = new GenericQuery();
	Elements searchResults = getPage(searchUrl,false).select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            thequery.addLink(addHost(searchResults.get(i).attr("href"), "bigbootytube.xxx"));
            thequery.addThumbnail(downloadThumb(thequery.getLink(i)));
            String thumbBase = CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37, '\'');
            stop = Integer.parseInt(CommonUtils.getLink(searchResults.get(i).toString(), searchResults.get(i).toString().indexOf("onmouseover=\"KT_rotationStart(this, \'")+37+thumbBase.length()+3,')'));
            thequery.addPreview(parse(thumbBase));
            thequery.addName(downloadVideoName(thequery.getLink(i)));
            Document linkPage = getPage(searchResults.get(i).attr("href"),false);
            String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\'');
            thequery.addSize(CommonUtils.getContentSize(video));
            thequery.addDuration(getDuration(thequery.getLink(i)).toString());
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException{ 
        Vector<File> thumbs = new Vector<>();
        
        for(byte i = 1; i <= stop; i++) {
            String thumbLink = url+String.valueOf(i)+".jpg";
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
        }
        
        return thumbs;
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
	return Jsoup.parse(getPage(url,false).select("h1").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        String thumb = addHost(CommonUtils.getLink(page.toString(),page.toString().indexOf("preview_url:")+14,'\''), "");
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        
        
        Elements li = getPage(url,false).select("div.list-thumbs").get(0).select("li");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty(); video v = null;
        while(!got) {
            if (count > li.size()) break;
            byte i = (byte)randomNum.nextInt(li.size()); count++;
            String link = addHost(li.get(i).select("a.thumb-img").attr("href"), "bigbootytube.xxx");
            String thumb = configureUrl(li.get(i).select("a.thumb-img").select("img").attr("src"));
            String title = li.get(i).select("div.thumb-title").select("h3").select("a").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    continue;//throw new IOException("Failed to completely download page");
                Document linkPage = getPage(link,false);
                String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\'');
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),CommonUtils.getContentSize(video),getDuration(link).toString());
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "http://www.bigbootytube.xxx/search/?q="+str.trim().replaceAll(" ", "+");
        
	Elements searchResults = getPage(searchUrl,false).select("div.list-thumbs").get(0).select("li").select("div.thumb").select("div.thumb-content").select("a.thumb-img");
        Random rand = new Random(); int count = searchResults.size(); video v = null;
	while(count-- > 0) {
            byte i = (byte)rand.nextInt(searchResults.size());
            if (!CommonUtils.testPage(searchResults.get(i).attr("href"))) continue; //test to avoid error 404
            //try {verify(page);} catch (GenericDownloaderException e) {continue;}
            try {
                Document linkPage = getPage(searchResults.get(i).attr("href"),false);
                String video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url:")+12,'\''), link = addHost(searchResults.get(i).attr("href"), "bigbootytube.xxx");
                v = new video(link,downloadVideoName(link),downloadThumb(link),CommonUtils.getContentSize(video),getDuration(link).toString());
            } catch(Exception e) { v = null; continue;}
            break; //if u made it this far u already have a vaild video
        }
        return v;        
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(link,false).select("span.btn-ico").get(1).text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    private Vector<String> getlist(int i) throws IOException, GenericDownloaderException {
        if (url == null)
            return null;
        Vector<String> words = new Vector<>();
        Elements a = getPage(url, false).select("ul.list-baiges").select("li").get(i).select("a");
        a.forEach(s -> words.add(s.text()));
        return words;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return getlist(1);
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return getlist(0);
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?bigbootytube[.]xxx/videos/(?<id>[\\d]+)/[\\S]+/";
    }
}
