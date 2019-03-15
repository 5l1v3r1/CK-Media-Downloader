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
import java.net.MalformedURLException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Instagram extends GenericExtractor{
    private Document html;
    //https://www.instagram.com/p/BQ0eAlwhDrw
    
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
    
    public Instagram(Document page) throws MalformedURLException {
        html = page;
        this.url = getCanonicalLink(page);
        String thumbLink = getMetaImage(page);
        if(!CommonUtils.checkImageCache(CommonUtils.parseName(thumbLink,".jpg"))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.parseName(thumbLink,".jpg"),MainApp.imageCache);
        this.videoThumb = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.parseName(thumbLink,".jpg"));
        if (isVideo(page)) {
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            }
            this.videoName = CommonUtils.parseName(videoLink, ".mp4");
        } else this.videoName = videoThumb.getName();
    }

    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page;
        if (html != null) page = this.html;
        else page = getPage(url,false,true); MediaDefinition media = new MediaDefinition();
        
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
            if (page.toString().contains("\"is_video\":true")) {
               while((occur = page.toString().indexOf("video_url",from)) != -1) {
                    from = occur + 1; count++; 
                    String link = CommonUtils.getLink(page.toString(), occur+12, '\"');
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                    if (link.contains(".mp4"))
                         media.addThread(qualities, CommonUtils.parseName(link,".mp4"));
                    else media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
                } 
            } else {
                while((occur = page.toString().indexOf("display_resources",from)) != -1) {
                    if (first) {first = false; from = occur + 1; continue;}
                    from = occur + 1; count++; 
                    String brack = CommonUtils.getSBracket(page.toString(),occur); String link = parseBracket(brack);
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                    if (link.contains(".mp4"))
                         media.addThread(qualities, CommonUtils.parseName(link,".mp4"));
                    else media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
                } if (count < 1) { //if there really was jus one
                     occur = page.toString().indexOf("display_resources",0);
                     if(occur != -1) {
                         String brack = CommonUtils.getSBracket(page.toString(),occur); String link = parseBracket(brack);
                         Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                         media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
                     } else  {
                         String picLink = getMetaImage(page);
                         Map<String,String> qualities = new HashMap<>(); qualities.put("single",picLink);
                         media.addThread(qualities, CommonUtils.parseName(picLink,".jpg"));
                    }
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
    
    @Override protected void setExtractorName() {
        extractorName = "Instagram";
    }

    @Override public video similar() {
        return null;
    }

    @Override public video search(String str) {
        return null;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        Document page;
        if (html != null)
            page = html;
        else page = getPage(url,false,true);
        String videoLink = null; long total = 0;
        if (isVideo(page)) { //download video
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
        } else if(isProfilePage(page)) //download profile pic
            videoLink = getMetaImage(page);
        else { //download pic/s
            int occur, from = 0; boolean first = true; int count = 0;
            if (page.toString().contains("\"is_video\":true")) { 
                while((occur = page.toString().indexOf("video_url",from)) != -1) {
                    from = occur + 1; count++; 
                    String link = CommonUtils.getLink(page.toString(), occur+12, '\"');
                    total += CommonUtils.getContentSize(link);
                }
                return total;
            } else {
                while((occur = page.toString().indexOf("display_resources",from)) != -1) {
                    if (first) {first = false; from = occur + 1; continue;}
                    from = occur + 1; count++;
                    String brack = CommonUtils.getSBracket(page.toString(),occur);
                    total += CommonUtils.getContentSize(parseBracket(brack));
                } if (count < 1) { //if there really was jus one
                    occur = page.toString().indexOf("display_resources",0);
                    if(occur != -1) {
                         String brack = CommonUtils.getSBracket(page.toString(),occur);
                         total += CommonUtils.getContentSize(parseBracket(brack));
                    } else
                         total += CommonUtils.getContentSize(getMetaImage(page));
                }
                return total;
            }
        }
        if (videoLink == null) return -1;
        else return CommonUtils.getContentSize(videoLink);
    }
    
    public String getId(String link) {
        Pattern p;
        if (link.matches("https://(www.)?instagram.com/p/[\\S]+(/[?]taken-by=[\\S]*)?"))
            p = Pattern.compile("https://(www.)?instagram.com/p/([\\S]+)(/[?]taken-by=[\\S]*)?");
        else p = Pattern.compile("https://(www.)?instagram.com/([\\S]+)/");
        Matcher m = p.matcher(link);
        return m.find() ? m.group(2) : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
