/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Eporner extends GenericExtractor{
    private static final byte SKIP = 6;
    
    public Eporner() { //this contructor is used for when you jus want to search
        
    }

    public Eporner(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Eporner(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Eporner(String url, File thumb, String videoName){
        super(url,thumb,videoName); 
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Elements a = getPageCookie(url, false).getElementById("hd-porn-dload").select("a");
        Map<String, MediaQuality> qualities = new HashMap<>();
        
        for(byte i = 0; i < a.size(); i++) {
            String link = a.get(i).attr("href"), regex = "/dload/\\S+/(?<id>\\d+)/\\d+-\\d+p.mp4";
            if (link.matches(regex))
                qualities.put(getId(link, regex), new MediaQuality(addHost(link, "www.eporner.com")));
        }
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        //gotta fix this
        if (page.getElementById("undervideomenu") != null)
            return page.getElementById("undervideomenu").select("h1").text();
        else if (page.getElementById("video-info") != null)
            return page.getElementById("video-info").select("h1").text();
        else return page.select("h1").text();
    }
    
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        String thumb = getMetaImage(getPage(url,false), true);
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private Vector<String> getList(String regex)throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        try {
            Elements a = getPage(url, false).getElementById("hd-porn-tags").select("a");
            for(int i = 0; i < a.size() - 1; i++)
                if (a.get(i).attr("href").matches(regex))
                    words.add(a.get(i).text());
        } catch (NullPointerException e) {
            return null;
        }
        return words;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return getList("/(category|search)/[\\S]+/");
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return getList("/pornstar/[\\S]+/");
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?eporner[.]com/hd-porn/(?<id>[\\S]+)/[\\S]+/"; 
        //https://www.eporner.com/hd-porn/lc9EdCuqibl/Busty-black-slut-tit-fucks-and-blows/
        //https://eporner.com/hd-porn/IF2uT0kcojN/Blonde-Lass-Delicate-Hands/
    }
}
