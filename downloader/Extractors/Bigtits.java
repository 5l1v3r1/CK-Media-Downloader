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
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Bigtits extends GenericExtractor{
    private static final int SKIP = 2;
    
    public Bigtits() { //this contructor is used for when you jus want to search
        
    }
    
    public Bigtits(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Bigtits(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Bigtits(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        Element div = page.getElementById("playerCont");
	String video = CommonUtils.getLink(div.toString(), div.toString().indexOf("<source src=")+13, '\"');
        Map<String,String> qualities = new HashMap<>();qualities.put("single",video); 
        MediaDefinition media = new MediaDefinition(); media.addThread(qualities, videoName);
        
        return media;
        //super.downloadVideo(video,title,s);
    }
   
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
	String temp = Jsoup.parse(page.select("div.vid_title").select("h1").toString()).body().text();
	
	return temp.substring(temp.lastIndexOf('>')+2);
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        Element div = page.getElementById("playerCont");
        String thumb = CommonUtils.getLink(div.toString(),div.toString().indexOf("poster:")+9,'\"');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override protected void setExtractorName() {
        extractorName = "Bigtits";
    }

    @Override public video similar() throws IOException {
    	/*if (url == null) return null;
        
        video v = null;
        
        Document page = getPage(url,false);
        Elements li = page.select("ul.videos").select("li.videobox");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
        	if (count > li.size()) break;
        	int i = randomNum.nextInt(li.size()); count++;
        	String link = "http://www.bigtits.com" + li.get(i).select("a.thumb_click").attr("href");
            String thumb = li.get(i).select("img.scrub_thumb").attr("src");
            String title = li.get(i).select("div.title").select("a").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)));
                break;
            }
        return v;*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "http://www.bigtits.com/search?q="+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("ul.videos").select("li");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("div.thumb_container").select("a").select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = "http://www.bigtits.com" + li.get(i).select("div.thumb_container").select("a").attr("href");
        	String name = li.get(i).select("div.thumb_container").select("a").select("img").attr("title");
        	if (link.isEmpty() || name.isEmpty()) continue;
                Document linkPage = getPage(link,false);
                Element div = linkPage.getElementById("playerCont");
                String video = CommonUtils.getLink(div.toString(), div.toString().indexOf("<source src=")+13, '\"');
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),CommonUtils.getContentSize(video));
        	break;
        }
        
        return v;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return CommonUtils.getContentSize(getVideo().iterator().next().get("single"));
    }
    
    public String getId(String link) {
        Pattern p = Pattern.compile("https://(www.)?bigtits.com/videos/watch/[\\S]+/([\\d]+)");
        return p.matcher(link).group(2);
    }

    @Override public String getId() {
        return getId(url);
    }
}
