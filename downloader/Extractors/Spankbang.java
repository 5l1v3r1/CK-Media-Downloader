/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageParseException;
import downloader.Exceptions.VideoDeletedException;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import downloaderProject.MainApp;
import org.jsoup.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection.Response;

/**
 *
 * @author christopher
 */

public class Spankbang extends GenericQueryExtractor implements Playlist, Searchable{
    private static final int SKIP = 3;
    private String playlistUrl = null;
    private final String JSONURL = "https://spankbang.com/api/videos/stream";
    
    public Spankbang() { //this contructor is used for when you jus want to query
        
    }
    
    public Spankbang(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isPlaylist(url)) {
            playlistUrl = convertUrl(configureUrl(url));
            this.url = convertUrl(getFirstUrl(url));
        } else
            this.url = convertUrl(configureUrl(url));
        this.videoThumb = downloadThumb(convertUrl(configureUrl(this.url)));
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Spankbang(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isPlaylist(url)) {
            playlistUrl = convertUrl(configureUrl(url));
            this.url = convertUrl(getFirstUrl(url));
        } else
            this.url = convertUrl(configureUrl(url));
        this.videoThumb = thumb;
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Spankbang(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> quality = new HashMap<>();
        try {
            JSONObject json = (JSONObject)new JSONParser().parse(s);
            Iterator<String> i = json.keySet().iterator();
            while(i.hasNext()) {
                String id = i.next();
                if (id.startsWith("stream_url_"))
                    if (!((String)json.get(id)).isEmpty())
                        quality.put(id.replace("stream_url_", ""),(String)json.get(id));
            }
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return quality;
    }
    
    @Override public MediaDefinition getVideo() throws MalformedURLException, IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException{        
        Response r = getPageResponse(url, false);
        Document page = r.parse();
        verify(page);
        
        /*qualities.put("240P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_240p")+24)));
        qualities.put("320P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_320p")+24)));
        qualities.put("480P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_480p")+24)));
        qualities.put("720P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_720p")+24)));
        qualities.put("1080P",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_1080p")+25)));
        qualities.put("4K",getQuality(page.toString().substring(page.toString().indexOf("var stream_url_4k")+22)));*/
        
        String streamKey = page.getElementById("video").attr("data-streamkey");
        String cookie = r.cookie("sb_csrf_session");
        
        StringBuilder params = new StringBuilder();
        params.append("sb_csrf_session="+URLEncoder.encode(cookie)+"&data=0&");
        params.append("id="+URLEncoder.encode(streamKey));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(CommonUtils.sendPost(JSONURL,params.toString(),false,url,"application/json")),videoName);
        //https://spankbang.com/1y4qi/video/vanessa+del+red+dress
        
        return media;
    }
    
     private static void verify(Document page) throws GenericDownloaderException{
        if (page.getElementById("video_removed") != null) 
            throw new VideoDeletedException();
        else if(!page.select("div.message").isEmpty())
            throw new VideoDeletedException(page.select("div.message").select("span.text").get(0).text());
    }
    
    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://spankbang.com/s/"+search+"/";
        GenericQuery thequery = new GenericQuery();
        
        Document page = getPage(searchUrl,true);
        
	Elements searchResults = page.select("div.video-item");
	for(int i = 0; i < searchResults.size(); i++)  {
            String link = addHost(searchResults.get(i).select("a.thumb").attr("href"),"spankbang.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            thequery.addLink(link);
            String thumbLink = "https:"+searchResults.get(i).select("a.thumb").select("img").attr("data-src"); //src for pc
            if (!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(downloadVideoName(addHost(searchResults.get(i).select("a.thumb").attr("href"),"spankbang.com")));
            thequery.addSize(getSize(link));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Vector<File> thumbs = new Vector<>();
        try {
            Document page = getPage(url,false);
            
            Elements previewImg = page.select("figure.thumbnails").select("img"); //div containing imgs tags
			
            Iterator<Element> img = previewImg.iterator();
            while(img.hasNext()) { //loop through all img tags
               	String thumb = img.next().attr("src");
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                    CommonUtils.saveFile("https:"+thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
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
        String thumb = getMetaImage(page).startsWith("http") ? getMetaImage(page) : "https:" + getMetaImage(page);

        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    private static String convertUrl(String url) {
        return url.replace("https://m.", "https://www.");
    }
    
    private static boolean isPlaylist(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        return url.matches("https://(www.)?spankbang.com/[a-zA-Z0-9]+/playlist/[\\S]*");
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.video-list.video-rotate").get(page.select("div.video-list.video-rotate").size()- 1).select("div.video-item");
        int count = li.size(); Random rand = new Random();
        
        while(count-- > 0){
            int i = rand.nextInt(li.size());
            String link = addHost(li.get(i).select("a.thumb").attr("href"),"spankbang.com");
            try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
            String title = li.get(i).select("div.inf").select("a").text();
            try {if (title.length() < 1) title = downloadVideoName(link);} catch (Exception e) {continue;}
                try {v = new video(link,title,downloadThumb(link),getSize(link)); }catch(Exception e) {continue;}
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        str = str.trim(); 
        str = str.replaceAll(" ", "+");
        String searchUrl = "https://spankbang.com/s/"+str+"/";
        
        Document page = getPage(searchUrl,true); video v = null;
        
	Elements searchResults = page.select("div.video-item"); int count = searchResults.size(); Random rand = new Random();

	while(count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            String link = addHost(searchResults.get(i).select("a.thumb").attr("href"),"spankbang.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            try {verify(getPage(link,false)); } catch (GenericDownloaderException e) {continue;}
            String thumbLink = "https:"+searchResults.get(i).select("a.thumb").select("img").attr("data-src"); //src for pc
            if (!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            try {
                long size = getSize(link);
                v = new video(link,downloadVideoName(link),new File(MainApp.imageCache+File.separator+CommonUtils.parseName(thumbLink,".jpg")),size);
            } catch (Exception e) {
                v = null; continue;
            }
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private static String getFirstUrl(String url) throws IOException, GenericDownloaderException {
        Document page = getPage(url,false);
        Elements div = page.select("div.results").select("div.video-item");
        return "https://spankbang.com" + div.get(0).select("a").get(0).attr("href");
    }
    
    @Override public boolean isPlaylist() {
        return playlistUrl != null;
    }

    @Override public Vector<String> getItems() throws IOException, GenericDownloaderException {
        Document page = getPage(playlistUrl,false);
        Elements div = page.select("div.results").select("div.video-item");
        Vector<String> list = new Vector<>();
        for(int i = 0; i < div.size(); i++)
            list.add(addHost(div.get(i).select("a").get(0).attr("href"),"spankbang.com"));
        return list;
    }
    
    private long getSize(String link) throws IOException, GenericDownloaderException {
        Response r = getPageResponse(link, false);
        Document page = r.parse();
        verify(page);
        
        String streamKey = page.getElementById("video").attr("data-streamkey");
        String cookie = r.cookie("sb_csrf_session");
        
        StringBuilder params = new StringBuilder();
        params.append("sb_csrf_session="+URLEncoder.encode(cookie)+"&data=0&");
        params.append("id="+URLEncoder.encode(streamKey));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(CommonUtils.sendPost(JSONURL,params.toString(),false,url,"application/json")),videoName);
        return getSize(media);
    }

    @Override  protected String getValidRegex() {
        works = true;
        return "https?://(?:[^/]+[.])?spankbang[.]com/(?<id>[\\S]+)/(?:video|playlist|embed)/[\\S]+"; 
    }
}