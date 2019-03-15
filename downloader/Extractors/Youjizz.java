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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Youjizz extends GenericExtractor{
	private static final int skip = 3;
	
    public Youjizz() { //this contructor is used for when you jus want to search
        
    }
    
    public Youjizz(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Youjizz(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private static Map<String, String> getQualities(String src, String s) {
        int happen, from = 0;
        
        Map<String, String> qualities = new HashMap<>();
        while((happen = src.indexOf(s,from)) != -1) {
            if(!qualities.containsKey(CommonUtils.getLink(src,happen+10,'\"')))
                qualities.put(CommonUtils.getLink(src,happen+10,'\"'), "https:"+CommonUtils.eraseChar(CommonUtils.getLink(src,src.indexOf("filename",happen)+11,'\"'), '\\'));
            from = happen +1;
        }
        return qualities;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        Document page = getPage(url,false,true);
        Map<String, String> quality = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("var encodings")),"quality");
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(quality,videoName);
        
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);

	return Jsoup.parse(page.select("title").toString()).text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
         Document page = getPage(url,false);
        
        String thumb = "https://"+CommonUtils.getLink(page.toString(),page.toString().indexOf("posterImage: '//") + 16, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Youjizz";
    }

    @Override public video similar() throws IOException, GenericDownloaderException{
    	if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related").select("div.video-thumb");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.size() == 0) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = "https://www.youjizz.com" + li.get(i).select("a").get(0).attr("href");
            String thumb = li.get(i).select("a").get(0).select("img").attr("src");
            if (thumb.length() < 1) thumb = li.get(i).select("a").get(0).select("img").attr("data-original");
            thumb = "https:" + thumb;
            String title = li.get(i).select("div.video-title").select("a").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                try {v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)),getSize(link)); } catch (GenericDownloaderException | IOException e) {};
                break;
            }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException{
    	str = str.trim(); str = str.replaceAll(" ", "-");
    	String searchUrl = "https://www.youjizz.com/search/"+str+"-1.html?";
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.video-thumb");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = "https:"+li.get(i).select("img").get(0).attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = "https://youjizz.com"+li.get(i).select("a").get(0).attr("href");
        	String name = li.get(i).select("div.video-title").select("a").text();
        	if (link.isEmpty() || name.isEmpty()) continue;
        	try {v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)),getSize(link)); } catch(GenericDownloaderException | IOException e) {}
        	break;
        }
        return v;
    }

    public static long getSize(String link) throws IOException, GenericDownloaderException {
        Document page = getPage(link,false,true);
        Map<String, String> quality = getQualities(CommonUtils.getSBracket(page.toString(), page.toString().indexOf("var encodings")),"quality");
        String video;
        
        //ik this ordered retarded
        if(quality.containsKey("720"))
            video = quality.get("720");
        else if(quality.containsKey("480"))
            video = quality.get("480");
        else if(quality.containsKey("1080"))
            video = quality.get("1080");
        else if(quality.containsKey("360"))
            video = quality.get("360");
        else if(quality.containsKey("288"))
            video = quality.get("288");
        else video = quality.get((String)quality.keySet().toArray()[0]);
        return CommonUtils.getContentSize(video);
    }
     
    @Override public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
    
    public String getId(String link) {
        Pattern p = Pattern.compile("https://(www.)?youjizz.com/videos/([\\S]+).html");
        Matcher m = p.matcher(link);
        return m.find() ? m.group(2) : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
