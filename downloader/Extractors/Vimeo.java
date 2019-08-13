/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Vimeo extends GenericExtractor implements Playlist{
    private static final byte SKIP = 2;
    private String playlistUrl = null;
    
    public Vimeo() { //this contructor is used for when you jus want to search
        
    }
    
    public Vimeo(String url)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isUserAlbum(url) || isAlbum(url)) {
            this.playlistUrl = url;
            this.url = getFirstUrl(url);
        } else 
            this.url = url;
        this.videoThumb = downloadThumb(convertUrl(configureUrl(this.url)));
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Vimeo(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        if (isUserAlbum(url) || isAlbum(url)) {
            this.playlistUrl = url;
            this.url = getFirstUrl(url);
        } else 
            this.url = url;
        this.videoThumb = thumb;
        this.videoName = downloadVideoName(convertUrl(configureUrl(this.url)));
    }
    
    public Vimeo(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String, MediaQuality> getQualities(String src) throws PageParseException {
        Map<String, MediaQuality> qualities = new HashMap<>();
        
        try {
            JSONArray progressiveArray = (JSONArray)new JSONParser().parse(src);
            for(int i = 0; i < progressiveArray.size(); i++) {
                JSONObject quality = (JSONObject)progressiveArray.get(i);
                if(!quality.get("mime").equals("video/mp4")) continue;
                qualities.put((String)quality.get("quality"), new MediaQuality((String)quality.get("url")));
            }
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return qualities;
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        verify(page); 
        
        String newUrl = "https://player.vimeo.com/video/"+getId();
        page = getPage(newUrl,false,true);
        verify(page);
        
        Map<String, MediaQuality> qualities = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("progressive")));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        String title = Jsoup.parse(page.select("title").get(0).toString()).text();
        
        if(title.equals("VimeUhOh") || !page.select("h1.exception_title iris_header").isEmpty())
            throw new PageNotFoundException();
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        verify(page);  
	return Jsoup.parse(page.select("title").get(0).toString()).text().replace(" on Vimeo","");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        String newUrl = "https://player.vimeo.com/video/"+url.split("/")[url.split("/").length -1];
        Document page = getPage(newUrl,false);
        verify(page); String thumb = null;
        try {
            String thumbs = "{" + getId(page.toString(), "thumbs\":\\{(?<id>.+?)\\}") + "}";
            JSONObject thumbObject = (JSONObject)new JSONParser().parse(thumbs);
            thumb = (String)thumbObject.get("base");
        } catch (ParseException e) {
            CommonUtils.log("error parsing for thumb "+url,"Vimeo");
            throw e;
        }
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    private static String convertUrl(String url) {
        return url.replace("player.vimeo.com/video/", "vimeo.com/");
    }
    
    private static String convertUserAlbum(String url) {
        return url.matches("https?://(?:www)?vimeo[.]com/[\\S]+(?:/videos)?") ? url : url + "/videos";
    }
    
    private static boolean isUserAlbum(String url) {
        return url.matches("https?://(?:www)?vimeo[.]com/[\\S]+(?:/videos)?") && !url.matches("https?://(?:www)?vimeo[.]com/[\\d]+");
    }
    
    private static boolean isAlbum(String url) {
        return url.matches("https?://vimeo[.]com/(?:album|showcase)/(?<id>\\d+)(?:/video)");
    }
    
    @Override public video similar() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /*@Override public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    	/*str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://www.vimeo.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.iris_p_infinite__item span-1");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("div.iris_thumbnail.iris_thumbnail.iris_thumbnail--16-9").select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a.iris_video-vital__overlay.iris_link-box.iris_annotation-box.iris_chip-box").attr("href");
        	String name = li.get(i).select("span.iris_link.iris_link--gray-2").text();
                System.out.println("link = "+link+"\nname = "+name);
        	if (link.isEmpty() || name.isEmpty()) continue;
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)));
        	break;
        }
        
        return v;
    }*/
    
    private String getFirstUrl(String link) throws IOException, GenericDownloaderException {
        String page = getPage(convertUserAlbum(link), false).toString();
        return addHost(CommonUtils.getLink(page, page.indexOf("\"clip_id\"") + 10, ','),"vimeo.com");
    }
    
    @Override public boolean isPlaylist() {
        return playlistUrl != null;
    }

    @Override public Vector<String> getItems() throws IOException, GenericDownloaderException {
        Vector<String> list = new Vector<>();
        for(int i = 1; true; i++)
            try {
                String page = getPage(convertUserAlbum(playlistUrl)+"?page="+String.valueOf(i), false).toString();
                getMatches(page,"\"clip_id\":(?<clipId>\\d+),","clipId").forEach(m -> list.add(addHost(m, "vimeo.com")));
            } catch (PageNotFoundException e) {
                break;
            }
        return list;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        GameTime g = new GameTime();
        g.addSec(Integer.parseInt(getId(getPage(convertUrl(url),false).toString(),".*duration\":[{]\"raw\"[:](?<id>\\d+).*")));
        return g;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("meta").forEach(meta -> {
           if (meta.attr("property").equals("video:tag"))
               words.add(meta.attr("content"));
        });
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(((((?:www)?vimeo[.]com)|(player[.]vimeo[.]com/video))/(?<id>[\\d]+))|"
                + "(?:www)?vimeo[.]com/(?<id2>[\\S]+)(?:/videos)?)";
        //https://vimeo.com/41572389
    }
}
