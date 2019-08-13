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
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Porn extends GenericExtractor implements Searchable{
    private static final byte SKIP = 6;
    
    public Porn() { //this contructor is used for when you jus want to search
        
    }

    public Porn(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Porn(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Porn(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static Map<String, MediaQuality> getQualities(String src) throws PageParseException{
        Map<String, MediaQuality> links = new HashMap<>();
        
        try {
            src = src.replaceAll("id:\"","\"id\":\"").replaceAll("url","\"url\"").replaceAll("active","\"active\"").replaceAll("false","\"false\"").replaceAll("true","\"true\"").replaceAll("definition", "\"definition\"");
            JSONArray json = (JSONArray)new JSONParser().parse(src);
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject q = i.next();
                links.put((String)q.get("id"), new MediaQuality((String)q.get("url")));
            }
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return links;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        Map<String, MediaQuality> qualities = getQualities(page.toString().substring(page.toString().indexOf("streams:[")+8,page.toString().indexOf("}]",page.toString().indexOf("streams:["))+2));

        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);

        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        Document page = getPage(url,false);
        return CommonUtils.getLink(page.toString(),page.toString().indexOf("title",page.toString().indexOf("thumbCDN")+10)+7,'\"');
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
	String preThumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("thumbCDN")+10,'\"');
	String postThumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("poster",page.toString().indexOf("thumbCDN")+10)+8,'\"');
        String thumbLink = preThumb + postThumb;
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        
        
        Elements divs = getPage(url,false).select("section.thumb-list.videos").select("div.item.rollable");
        Random randomNum = new Random(); int count = 0; boolean got = divs.isEmpty(); video v = null;
        while(!got) {
            if (count > divs.size()) break;
            byte i = (byte)randomNum.nextInt(divs.size()); count++;
            String link = addHost(divs.get(i).select("div.thumb").select("a").attr("href"),"www.porn.com");
            String title = divs.get(i).select("div.thumb").select("a").attr("title");
            String thumb = divs.get(i).select("div.thumb").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
            File thumbFile = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
            try {v = new video(link,title,thumbFile,getSize(link), getDuration(link).toString()); } catch(GenericDownloaderException | IOException e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.porn.com/videos/search?q="+str.replaceAll(" ", "+");

        Elements divs = getPage(searchUrl,false).select("section.thumb-list.videos").select("div.item.rollable"); 
        int count = divs.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            Element div = divs.get(rand.nextInt(divs.size()));
            String link = addHost(div.select("div.thumb").select("a").attr("href"),"www.porn.com");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumb = div.select("div.thumb").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
            File thumbFile = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
            try { v = new video(link,div.select("div.thumb").select("a").attr("title"),thumbFile,getSize(link), getDuration(link).toString()); } catch (GenericDownloaderException | IOException e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    public long getSize(String url) throws IOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        Map<String, MediaQuality> qualities = getQualities(page.toString().substring(page.toString().indexOf("streams:[")+8,page.toString().indexOf("}]",page.toString().indexOf("streams:["))+2));
        MediaDefinition media = new MediaDefinition();
        media.addThread(qualities,videoName);
        
        return getSize(media);
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        long secs = getSeconds(getPage(link,false).select("span.length").text());
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        Vector<String> words = new Vector<>();
        getPage(url, false).select("div.meta-tags").select("p").get(1).select("a").forEach(a -> words.add(a.text()));;
        return words;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?porn[.]com/videos/([\\S]+[-])+(?<id>[\\d]+)";
        //https://www.porn.com/videos/chavon-pov-blowjob-and-tit-fuck-89478
    }
}
