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
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Yourporn extends GenericExtractor{
    private static final int skip = 2;
    
    public Yourporn() { //this contructor is used for when you jus want to search
        
    }

    public Yourporn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(changeHttp(configureUrl(url))),downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(url);
    }
    
    public Yourporn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(changeHttp(configureUrl(url))));
        this.url = changeHttp(url);
    }
    
    public Yourporn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static Vector<String> getImages(Document page) {
        Elements imgs = page.select("img.photo_el_img");
        Vector<String> links = new Vector<>();
        
	for(Element img :imgs)
            links.add(img.attr("src").replaceAll("/s/","/n/"));
        return links;
    }
	
    private static Vector<String> getImages(String url) throws IOException {
        Document page = getPage(url,false,true);
	//thumb == first image
        
        Elements pagination = page.getElementById("center_control").select("a");
        Vector<String> links = new Vector<>();
	if (pagination.size() > 0)
            for(Element link: pagination)
                links.addAll(getImages(getPage("http://pics.vc/"+ link.attr("href"),false)));
        else return getImages(page);
        return links;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        verify(page);
     
        MediaDefinition media = new MediaDefinition();
        if (isAlbum(url)) {
            media.setAlbumName(this.videoName);
            Vector<String> links = getImages(url);
            for(int i = 0; i < links.size(); i++) {
                Map<String,String> qualities = new HashMap<>();
                qualities.put("single",links.get(i));
                media.addThread(qualities, CommonUtils.getPicName(links.get(i)));
            } return media;
        } else {
            String video = "https://www.yourporn.sexy"+CommonUtils.eraseChar(page.select("span.vidsnfo").attr("data-vnfo").split("\"")[3],'\\');
            //String video = "https://www.yourporn.sexy"+page.select("video.player_el").attr("src");
            Map<String,String> qualities = new HashMap<>();
            //idk wtf them keep changind the cdn
            String test = video.replace("cdn", "cdn4").replace("s12-1", "s12");
            if (CommonUtils.getContentSize(test) < 1)
              test = test.replace("cdn4", "cdn3");
            if (CommonUtils.getContentSize(test) < 1)
                test = test.replace("cdn3", "cdn2");
            System.out.println("What was test "+test);
            qualities.put("single",test);
            media.addThread(qualities,videoName);

            return media;
        }
    }
    
    private static boolean isAlbum(String url) {
        return url.matches("http://pics.vc/watch[?]g=[\\S]+");
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if (page.getElementById("center_control") != null) return;
        if (page.getElementById("player_el") == null) {
            if (page.select("span.page_message") != null || !page.select("span.page_message").isEmpty())
                throw new VideoDeletedException(page.select("span.page_message").text());
            else throw new VideoDeletedException();
        }
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        verify(page);
        //return page.select("meta").get(6).attr("content").replace(" on YourPorn. Sexy","");
        if (!isAlbum(url)) {
            String raw = getTitle(page);
            return raw.contains("#") ? raw.substring(0,raw.indexOf("#")-1).trim() : raw.replace(" on YourPorn. Sexy","");
        } else
            return page.select("div.gall_header").select("h2").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        verify(page);
        
        String thumbLink = null;
        if (!isAlbum(url))
            thumbLink = "https:"+page.getElementById("player_el").attr("poster");
        else
            thumbLink = getImages(url).get(0);
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,skip));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Yourporn";
    }

    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
        String searchUrl = "https://yourporn.sexy/"+str.trim().replaceAll(" ", "+")+".html";
        Document page = getPage(searchUrl,false); video v = null;
        
        Elements searchResults = page.select("div.search_results").select("div.post_el_small");
        //get first valid video
	for(int i = 0; i < searchResults.size(); i++)  {
            String link = "https://yourporn.sexy" + searchResults.get(i).select("div.post_control").select("a").attr("href");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            try {
                v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link));
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        verify(page);
     
        if (isAlbum(link)) {
            long total = 0;
            Vector<String> links = getImages(link);
            for(int i = 0; i < links.size(); i++)
                total += CommonUtils.getContentSize(links.get(i));
            return total;
        } else {
            String video = "https://www.yourporn.sexy"+CommonUtils.eraseChar(page.select("span.vidsnfo").attr("data-vnfo").split("\"")[3],'\\');
            //idk wtf them keep changind the cdn
            String test = video.replace("cdn", "cdn4").replace("s12-1", "s12");
            if (CommonUtils.getContentSize(test) < 1)
              test = test.replace("cdn4", "cdn3");
            if (CommonUtils.getContentSize(test) < 1)
                test = test.replace("cdn3", "cdn2");
            System.out.println("What was test "+test);
            return CommonUtils.getContentSize(test);
        }
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
       return getSize(url);
    }
}
