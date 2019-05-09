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
public class Anysex extends GenericExtractor implements Searchable{
    private static final int SKIP = 4;
    
    public Anysex() { //this contructor is used for when you jus want to search
        
    }

    public Anysex(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Anysex(String url, File thumb, String videoName){
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
        Elements results = getPage(url,false).select("div.watched").select("ul.box").select("li.item");
        
        Random randomNum = new Random(); int count = 0; boolean got = results.isEmpty();
        video v = null; int limit = results.size() > 100 ? results.size() / 10 : results.size() < 10 ? results.size() : results.size() / 5; 
        while(!got) {
            if (count > results.size()) break;
            int i = randomNum.nextInt(limit); count++;
            String link = null;
            for(Element a :results.get(i).select("a")) {
                if(a.attr("href").matches("/\\d+/"))
                    link = "http://anysex.com" + a.attr("href");
            } 
            
            try {
                File thumb = downloadThumb(link);
                v = new video(link,downloadVideoName(link),thumb,getSize(link));
            } catch (Exception e) {continue;}
            got = true;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://anysex.com/search/?q="+str.replaceAll(" ", "+");
	Document page = getPage(searchUrl,false);

	Elements lis = page.select("ul.box").select("li.item"); video v = null;
        Random rand = new Random(); int count = lis.size();
	while(count-- > 0) {
            Element li = lis.get(rand.nextInt(lis.size()));
            if (li.select("a").isEmpty()) continue;
            if (!CommonUtils.testPage(li.select("a").attr("href"))) continue; //test to avoid error 404
            String link = "https://anysex.com" + li.select("a").attr("href");
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
        return "https?://(?:www[.])?anysex[.]com/(?<id>[\\d]+)/";
    }
}
