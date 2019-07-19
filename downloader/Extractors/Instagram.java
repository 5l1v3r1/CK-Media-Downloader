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
    //https://www.instagram.com/p/BQ0eAlwhDrw
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
        } else if(isProfilePage(page)) { //download profile pic
            String picLink = getMetaImage(page);
            Map<String,String> qualities = new HashMap<>(); qualities.put("single",picLink);
            media.addThread(qualities,CommonUtils.parseName(picLink,".jpg"));
            return media;
        } else { //download pic/s
            int occur, from = 0; boolean first = true; int count = 0;
            while((occur = page.toString().indexOf("display_resources",from)) != -1) {
                if (first) {first = false; from = occur + 1; continue;}
                String link;
                from = occur + 1; count++;
                int to = page.toString().indexOf("display_resources",from);
                to = to == -1 ? page.toString().length() : to;
                if (page.toString().substring(from,to).contains("\"is_video\":true")) {
                    occur = page.toString().indexOf("video_url");
                    link = CommonUtils.getLink(page.toString(), occur+12, '\"');
                } else {
                    String brack = CommonUtils.getSBracket(page.toString(),occur); 
                    link = parseBracket(brack);
                } 
                Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                if (link.contains(".mp4"))
                    media.addThread(qualities, CommonUtils.parseName(link,".mp4"));
                else media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
            } if (count < 1) { //if there really was jus one
                occur = page.toString().indexOf("display_resources",0);
                if(occur != -1) {
                    String link;
                    if (page.toString().contains("\"is_video\":true")) {
                        occur = page.toString().indexOf("video_url");
                        link = CommonUtils.getLink(page.toString(), occur+12, '\"');
                    } else {
                        String brack = CommonUtils.getSBracket(page.toString(),occur); 
                        link = parseBracket(brack);
                    } 
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                    if (link.contains(".mp4"))
                        media.addThread(qualities, CommonUtils.parseName(link,".mp4"));
                    else media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
                } else  {
                    String picLink = getMetaImage(page);
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",picLink);
                    media.addThread(qualities, CommonUtils.parseName(picLink,".jpg"));
                }
            }
           return media;
        }
    }
    
    private static String getLinks(String src) {
        String[] tokens = src.split("\"");
        return tokens[3];
    }
    
    private static String parseBracket(String bracket) {
        int occur, from = 0;
        Vector<String> link = new Vector<>();
        while((occur = bracket.indexOf('{',from)) != -1) {
            String src = CommonUtils.getBracket(bracket,occur);
            from = occur+src.length();
            link.add(getLinks(src));
        }
        return link.get(link.size()-1); //highest quality is usually mentioned last
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
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            }
            return CommonUtils.parseName(videoLink, ".mp4");
        } else if (page.toString().contains("\"is_video\":true")) {
            int occur = page.toString().indexOf("video_url");
            String link = CommonUtils.getLink(page.toString(), occur+12, '\"');
            return CommonUtils.parseName(link,".mp4");
        }  else return downloadThumb(url).getName();
    }
    
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);// https://www.instagram.com/p/Bky0zJyA4kY-2nbW7RQ6oN71vNFEu-2lawigG00/
        
        String thumbLink = getMetaImage(page);
        if(!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.parseName(thumbLink,".jpg"));
    }

    @Override public video similar() {
        return null;
    }
    
    @Override public GameTime getDuration() {
        return null;
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
