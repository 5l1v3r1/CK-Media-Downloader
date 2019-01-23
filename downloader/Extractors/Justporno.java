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
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Justporno extends GenericExtractor{
    private static final int SKIP = 4;
    
    public Justporno() { //this contructor is used for when you jus want to search
        
    }
    
    public Justporno(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Justporno(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Justporno(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException {        
        Document page = getPage(url,false,true);
        String video;
        if (!page.select("video").isEmpty())
            video = getDefaultVideo(page);
        else video = CommonUtils.getLink(page.toString(),page.toString().indexOf("video_url: '")+12,'\'');
        
        Map<String,String> qualities = new HashMap<>();
        qualities.put("single",video); MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
        //super.downloadVideo(video,title,s);
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        if (!page.select("h1").isEmpty())
            return Jsoup.parse(page.select("h1").toString()).body().text();
        else return Jsoup.parse(page.select("h2").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        String thumb;
        if (!page.select("video").isEmpty()) {
            thumb = page.select("video").attr("poster"); if (thumb.length() < 1) return null;
            thumb = thumb.startsWith("http") ? thumb : "https:" + thumb;
        } else 
            thumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("preview_url: '")+14,'\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override protected void setExtractorName() {
        extractorName = "Justporno";
    }

    @Override public video similar() throws IOException {
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.select("div.thumb-box").select("li");
        for(int i = 0; i < li.size(); i++) {
        	String link = li.get(i).select("a").attr("href");
            String thumb = li.get(i).select("img").attr("src");
            if (thumb.length() < 1) li.get(i).select("img").attr("data-original");
            thumb = thumb.startsWith("http") ? thumb : "https:" + thumb;
            String title = li.get(i).select("p.thumb-item-desc").select("span").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                Document linkPage = getPage(link,false); String video;
                if (!linkPage.select("video").isEmpty())
                    video = getDefaultVideo(linkPage);
                else video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url: '")+12,'\'');
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),CommonUtils.getContentSize(video));
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "http://justporno.tv/search?query="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("ul").select("li");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img").attr("src"); 
                thumbLink = thumbLink.startsWith("http") ? thumbLink : "https:" + thumbLink; 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a").attr("href");
        	String name = li.get(i).select("a").attr("title");
        	if (link.isEmpty() || name.isEmpty()) continue;
                Document linkPage = getPage(link,false); String video;
                if (!linkPage.select("video").isEmpty())
                    video = getDefaultVideo(linkPage);
                else video = CommonUtils.getLink(linkPage.toString(),linkPage.toString().indexOf("video_url: '")+12,'\'');
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),CommonUtils.getContentSize(video));
        	break;
        }
        
        return v;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
}
