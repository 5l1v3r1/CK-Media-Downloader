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
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Instagram extends GenericExtractor{
    //https://www.instagram.com/p/BQ0eAlwhDrw //multithread vids
    //https://www.instagram.com/p/BnOu_cIgL7Y/?taken-by=calista.barrow
    //https://www.instagram.com/p/BnR9FRNFh1I/ //multithread, multimedia post
    
    public Instagram() {
        
    }
    
    public Instagram(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private MediaDefinition getMetaImg(Document page, MediaDefinition media) {
        String picLink = getMetaImage(page);
        Map<String,String> qualities = new HashMap<>(); qualities.put("single",picLink);
        media.addThread(qualities, CommonUtils.parseName(picLink,".jpg"));
        return media;
    }

    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true); MediaDefinition media = new MediaDefinition();
        
	if (isVideo(page)) { //download video
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            Map<String,String> qualities = new HashMap<>(); qualities.put("single",videoLink);
            media.addThread(qualities,CommonUtils.parseName(videoLink,".mp4"));
            return media;
        } else if(isProfilePage(page)) //download profile pic
            return getMetaImg(page, media);
        else { //download pic/s &&|| vids
            Vector<String> resources = getMatches(page.toString(), "display_resources[\"']:\\[(?<json>.+?}\\],.+?)\\]", "json");
            if (!resources.isEmpty()) {
                if (resources.size() != 1)
                    resources.remove(0);
                for(int i = 0; i < resources.size(); i++) {
                    String link;
                    if (resources.get(i).contains("\"is_video\":true"))
                        link = CommonUtils.getLink(resources.get(i), resources.get(i).indexOf("video_url")+12, '\"');
                    else
                        link = parseBracket(resources.get(i).substring(0,resources.get(i).indexOf("]")));
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                    if (link.contains(".mp4"))
                        media.addThread(qualities, CommonUtils.parseName(link,".mp4"));
                    else media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
                }
                return media;
            } else
                return getMetaImg(page, media);
        }
    }
    
    private static String getLinks(String src) {
        String[] tokens = src.split("\"");
        return tokens[3];
    }
    
    private static String parseBracket(String bracket) {
        Vector<String> raw = getMatches(bracket, "\\{(?<brack>.+?)\\}", "brack");
        return getLinks(raw.get(raw.size() - 1)); //highest quality is usually mentioned last
    }
    
    private static boolean isVideo(Document page) {
        Elements metas = page.select("meta");
        for(int i = 0; i < metas.size(); i++) {
            if (metas.get(i).attr("property").equals("og:video"))
                return true;
        }
        return false;
    }
    
    private static boolean isProfilePage(Document page) {
        return !page.toString().contains("display_resources");
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        if (isVideo(page)) {
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            return CommonUtils.parseName(videoLink, ".mp4");
        } else if (page.toString().contains("\"is_video\":true")) {
            int occur = page.toString().indexOf("video_url");
            String link = CommonUtils.getLink(page.toString(), occur+12, '\"');
            return CommonUtils.parseName(link,".mp4");
        } else return downloadThumb(url).getName();
    }
    
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        
        String thumbLink = getMetaImage(page);
        if(!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.parseName(thumbLink,".jpg"));
    }

    @Override public video similar() {
        return null;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        Document page = getPage(url, false);
        if (isVideo(page)) {
            try {
                long secs = (long)Math.ceil(Double.parseDouble(getId(page.toString(),"\"video_duration\":(?<id>[^,]+),")));
                GameTime g = new GameTime();
                g.addSec(secs);
                return g;
            } catch(NumberFormatException e) {
                CommonUtils.log(e.getMessage(), this);
                return null;
            }
        } else return null;
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?instagram[.]com/((?:p/(?<id>[^/?#&]+)/([\\S]+)?)|(?<id2>[\\S]+))";
    }
}
