/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Random;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Homemoviestube extends GenericExtractor implements Searchable{
    private static final int SKIP = 5;
    
    public Homemoviestube() { //this contructor is used for when you jus want to search
        
    }

    public Homemoviestube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Homemoviestube(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(this.url);
    }
    
    public Homemoviestube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        MediaDefinition media = new MediaDefinition();
        media.addThread(getDefaultVideo(page),videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        return getPage(url,false).select("h1").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        String thumbLink = getMetaImage(page);
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("videoTabs").getElementById("ctab1").select("div.film-item.col-lg-4.col-md-75.col-sm-10.col-xs-10");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = li.get(i).select("a").get(0).attr("href");
            if (link.matches("http://www.homemoviestube.com/videos/[\\d]+/[\\S]+.html"))
                try {v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link)); } catch(Exception e) {continue;}
            else continue;
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "http://www.homemoviestube.com/search/"+str.replaceAll(" ", "-")+"/page1.html";
	Document page = getPage(searchUrl,false);

	Elements divs = page.select("div.film-item.col-lg-4.col-md-75.col-sm-10.col-xs-10"); video v = null;
        int count = divs.size(); Random rand = new Random();
        
	while(count-- > 0) {
            Element div = divs.get(rand.nextInt(divs.size()));
            if (div.select("a").isEmpty()) continue;
            if (!div.select("a").get(0).attr("href").matches("http://www.homemoviestube.com/videos/[\\d]+/[\\S]+.html")) continue;         
            if (!CommonUtils.testPage(div.select("a").get(0).attr("href"))) continue; //test to avoid error 404
            String link = div.select("a").get(0).attr("href");
            try { v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link)); } catch (Exception e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String,String> q = getDefaultVideo(page);
        return CommonUtils.getContentSize(q.get(q.keySet().iterator().next()));
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?homemoviestube[.]com/videos/(?<id>[\\d]+)/[\\S]+[.]html"; 
    }
}
