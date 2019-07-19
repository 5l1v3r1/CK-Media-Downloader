/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Yourporn extends GenericExtractor implements Searchable{
    private static final byte SKIP = 2;
    private byte cdn = 3;
    private static final int mb = 5242880;
    
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
        
        imgs.forEach((img) -> {
            links.add(img.attr("src").replaceAll("/s/","/n/"));
        });
        return links;
    }
	
    private static Vector<String> getImages(String url) throws IOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
	//thumb == first image
        
        Elements pagination = page.getElementById("center_control").select("a");
        Vector<String> links = new Vector<>();
	if (pagination.size() > 0)
            for(Element link: pagination)
                links.addAll(getImages(getPage(addHost(link.attr("href"),"pics.vc"),false)));
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
            }
        } else {
            String video = addHost(CommonUtils.eraseChar(page.select("span.vidsnfo").attr("data-vnfo").split("\"")[3],'\\'),"sxyprn.com");
            //String video = "https://www.yourporn.sexy"+page.select("video.player_el").attr("src");
            Map<String,String> qualities = new HashMap<>();
            String test = video.replace("cdn", "cdn"+cdn);
            Pattern p = Pattern.compile("(.+/)s(\\d+)-1(/.+)"); //replace things like "s12-1", "s12"
            Matcher m = p.matcher(test);
            test = m.replaceAll("$1s$2$3");
            while(CommonUtils.getContentSize(test) < mb) {
                test = test.replace("cdn"+cdn, "cdn"+(cdn+1));
                cdn++;
                if (cdn > 20)
                    break;
            }
            qualities.put("single",test);
            media.addThread(qualities,videoName);
        }
        return media;
    }
    
    private static boolean isAlbum(String url) {
        return url.matches("http://pics[.]vc/watch[?]g=[\\S]+");
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if (page.getElementById("center_control") != null) return;
        if (page.getElementById("player_el") == null) {
            if (!page.select("span.page_message").isEmpty())
                throw new VideoDeletedException(page.select("span.page_message").text());
            else {
                if(page.select("span.vidsnfo").isEmpty())
                    throw new VideoDeletedException("Video could not be found");
            }
        }
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        verify(page);
        //return page.select("meta").get(6).attr("content").replace(" on YourPorn. Sexy","");
        if (!isAlbum(url)) {
            String raw = getTitleTag(page);
            return raw.contains("#") ? (raw.indexOf("#") == 0 ? raw.replace(" on YourPorn. Sexy","") : raw.substring(0,raw.indexOf("#")-1).trim()) : raw.replace(" on YourPorn. Sexy","");
        } else
            return page.select("div.gall_header").select("h2").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        verify(page);
        
        String thumbLink;
        if (!isAlbum(url))
            try {
                thumbLink = "https:"+page.getElementById("player_el").attr("poster");
            } catch (NullPointerException e) {
                thumbLink = "https:"+getMetaImage(page);
            }
        else
            thumbLink = getImages(url).get(0);
        
        if (thumbLink.equals("https:null"))
            throw new PageNotFoundException("Couldnt find a thumbnail");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        if (isAlbum(url)) return null;
        
        Elements post = getPage(url,false).select("div.post_control");
        Random randomNum = new Random(); int count = 0; boolean got = post.isEmpty();
        video v = null;
        while(!got) {
            if (count > post.size()) break;
            int i = randomNum.nextInt(post.size()); count++;
            String link = null;
            for(Element a :post.get(i).select("a")) {
                if(a.attr("href").matches("/post/\\S+.html"))
                    link = addHost(a.attr("href"),"sxyprn.com");
            }
            
            try {
                File thumb = downloadThumb(link);
                v = new video(link,downloadVideoName(link),thumb,getSize(link),getDuration(link).toString());
                got = true;
            } catch (Exception e) { }   
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException{
        String searchUrl = "https://sxyprn.com/"+str.trim().replaceAll(" ", "+")+".html";
        Document page = getPage(searchUrl,false); video v = null;
        
        Elements searchResults = page.select("div.search_results").select("div.post_el_small");
        int count = searchResults.size(); Random rand = new Random();
        
	while(count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).select("div.post_control").select("a").attr("href"),"sxyprn.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            try {
                v = new video(link,downloadVideoName(link),downloadThumb(link),getSize(link),getDuration(link).toString());
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
     
        MediaDefinition media = new MediaDefinition();
        if (isAlbum(link)) {
            media.setAlbumName(this.videoName);
            Vector<String> links = getImages(url);
            for(int i = 0; i < links.size(); i++) {
                Map<String,String> qualities = new HashMap<>();
                qualities.put("single",links.get(i));
                media.addThread(qualities, CommonUtils.getPicName(links.get(i)));
            }
        } else {
            String video = addHost(CommonUtils.eraseChar(page.select("span.vidsnfo").attr("data-vnfo").split("\"")[3],'\\'),"sxyprn.com");
            Map<String,String> qualities = new HashMap<>();
            String test = video.replace("cdn", "cdn"+cdn);
            Pattern p = Pattern.compile("(.+/)s(\\d+)-1(/.+)");
            Matcher m = p.matcher(test);
            test = m.replaceAll("$1s$2$3");
            while(CommonUtils.getContentSize(test) < mb) {
                test = test.replace("cdn"+cdn, "cdn"+(cdn+1));
                cdn++;
                if (cdn > 20)
                    break;
            }
            qualities.put("single",test);
            media.addThread(qualities,videoName);
        }
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        if (isAlbum(link))
            return new GameTime();
        else {
            GameTime g = new GameTime();
            g.addSec(getSeconds(getId(getPage(link,false).toString(),"duration\\s*:\\s*<[^>]+>(?<id>[\\d:]+)")));
            return g;
        }
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://((?:www[.])?(?:yourporn[.]sexy|sxyprn[.]com)/post/(?<id>[\\S]+)[.]html(?:[?][\\S]*)?)|http://pics[.]vc/watch[?]g=(?<id2>[\\S]+)";
    }
}
