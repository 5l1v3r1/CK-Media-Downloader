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
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Ghettotube extends GenericExtractor{
    private static final int SKIP = 3;
    
    public Ghettotube() { //this contructor is used for when you jus want to search
        
    }
    
    public Ghettotube(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Ghettotube(String url, File thumb)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Ghettotube(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> q = new HashMap<>();
        try {
            String src = s.substring(s.indexOf("sources: [{")+9,s.indexOf("}],")+2).replaceAll("file", "\"file\"").replaceAll("label", "\"label\"");
            JSONArray json = (JSONArray)new JSONParser().parse(src);
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject temp = i.next();
                q.put((String)temp.get("label"), (String)temp.get("file"));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("playerInstance.setup({")));
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
	return getH1Title(getPage(url,false));
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        Document page = getPage(url,false);
        
        Elements scripts = page.select("div.play").select("script");
        String thumb = CommonUtils.getLink(scripts.get(scripts.size()-1).toString(), scripts.get(scripts.size()-1).toString().indexOf("image:")+8, '\'');
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }

    @Override protected void setExtractorName() {
        extractorName = "Ghettotube";
    }

    @Override public video similar() throws IOException {
    	/*if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements li = page.getElementById("related_videos").select("div.item");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (li.isEmpty()) got = true;
        while(!got) {
            System.out.println("trying");
        	if (count > li.size()) break; int range = li.size() > 6 ? 6 : li.size(); 
        	int i = randomNum.nextInt(range); count++;
        	String link = li.get(i).select("a.thumb-img").attr("href");
            String thumb = li.get(i).select("a.thumb-img").select("img").attr("src");
            String title = li.get(i).select("a.thumb-img").select("img").attr("alt");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
            		continue;//throw new IOException("Failed to completely download page");
                v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)));
                break;
            }
        return v;*/
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
    	str = str.trim(); str = str.replaceAll(" ", "+");
    	String searchUrl = "https://www.ghettotube.com/search/video/"+str;
    	
    	Document page = getPage(searchUrl,false);
        video v = null;
        
        Elements li = page.select("div.thumb-list").select("div.item");
        
        for(int i = 0; i < li.size(); i++) {
        	String thumbLink = li.get(i).select("img").attr("src"); 
        	if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
        	String link = li.get(i).select("a").attr("href");
        	String name = li.get(i).select("h2").select("a").text();
        	if (link.isEmpty() || name.isEmpty()) continue;
                Document linkPage =  Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.PCCLIENT).get().html());
                 Elements scripts = linkPage.select("div.play").select("script");
                String video = CommonUtils.getLink(scripts.get(scripts.size()-1).toString(), scripts.get(scripts.size()-1).toString().indexOf("file:")+7, '\"');
        	v = new video(link,name,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),CommonUtils.getContentSize(video));
        	break;
        }
        return v;
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        Elements scripts = page.select("div.play").select("script");
        String video = CommonUtils.getLink(scripts.get(scripts.size()-1).toString(), scripts.get(scripts.size()-1).toString().indexOf("file:")+7, '\"');
        return CommonUtils.getContentSize(video);
    }
}
