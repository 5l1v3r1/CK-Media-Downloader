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
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Instagram extends GenericExtractor{
    
    public Instagram(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Instagram(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException{
        Document page = Jsoup.parse(Jsoup.connect(url).get().html()); MediaDefinition media = new MediaDefinition();
        
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
           String picLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:image"))
                    picLink = metas.get(i).attr("content");
            Map<String,String> qualities = new HashMap<>(); qualities.put("single",picLink);
            media.addThread(qualities,CommonUtils.parseName(picLink,".jpg"));
            return media;
        } else { //download pic/s
           int occur, from = 0; boolean first = true; int count = 0;
           while((occur = page.toString().indexOf("display_resources",from)) != -1) {
               if (first) {first = false; from = occur + 1; continue;}
               from = occur + 1; count++; 
               String brack = CommonUtils.getSBracket(page.toString(),occur); String link = parseBracket(brack);
               Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
               media.addThread(qualities, CommonUtils.getThumbName(link));
           } if (count < 1) { //if there really was jus one
               occur = page.toString().indexOf("display_resources",0);
               if(occur != -1) {
                    String brack = CommonUtils.getSBracket(page.toString(),occur); String link = parseBracket(brack);
                    Map<String,String> qualities = new HashMap<>(); qualities.put("single",link);
                    media.addThread(qualities, CommonUtils.parseName(link,".jpg"));
               } else  {
                   String picLink = null;
                    Elements metas = page.select("meta");
                    for(int i = 0; i < metas.size(); i++)
                        if(metas.get(i).attr("property").equals("og:image"))
                            picLink = metas.get(i).attr("content");
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
        Document page;
         if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
         else {
             String html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
             page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
         }
        if (isVideo(page)) {
            String videoLink = null;
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++) {
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
            }
            return CommonUtils.parseName(videoLink, ".mp4");
        } else return downloadThumb(url).getName();
    }
    
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page;//https://www.instagram.com/p/Bky0zJyA4kY-2nbW7RQ6oN71vNFEu-2lawigG00/
         if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,false))) //check to see if page was downloaded previous
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,false)));
         else {
             String html = Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
             page = Jsoup.parse(html);
            CommonUtils.savePage(html, url, false);
         }
        
        String thumbLink = null;
        Elements metas = page.select("meta");
        for(int i = 0; i < metas.size(); i++) {
            if(metas.get(i).attr("property").equals("og:image"))
                thumbLink = metas.get(i).attr("content");
        }
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
        Document page = Jsoup.parse(Jsoup.connect(url).get().html());
        String videoLink = null; long total = 0;
        if (isVideo(page)) { //download video
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:video"))
                    videoLink = metas.get(i).attr("content");
        } else if(isProfilePage(page)) { //download profile pic
            Elements metas = page.select("meta");
            for(int i = 0; i < metas.size(); i++)
                if(metas.get(i).attr("property").equals("og:image"))
                    videoLink = metas.get(i).attr("content");
        } else { //download pic/s
           int occur, from = 0; boolean first = true; int count = 0;
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
               } else  {
                    Elements metas = page.select("meta");
                    for(int i = 0; i < metas.size(); i++)
                        if(metas.get(i).attr("property").equals("og:image"))
                            total += CommonUtils.getContentSize(metas.get(i).attr("content"));
               }
           }
           return total;
        }
        if (videoLink == null) return -1;
        else return CommonUtils.getContentSize(videoLink);
    }
}
