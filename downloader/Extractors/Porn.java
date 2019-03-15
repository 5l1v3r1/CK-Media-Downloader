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
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
public class Porn extends GenericExtractor{
    private static final int SKIP = 6;
    
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
    
    private static Map<String,String> getQualities(String src) throws PageParseException{
        Map<String, String> links = new HashMap<>();
        
        try {
            src = src.replaceAll("id:\"","\"id\":\"").replaceAll("url","\"url\"").replaceAll("active","\"active\"").replaceAll("false","\"false\"").replaceAll("true","\"true\"");
            System.out.println(src);
            JSONArray json = (JSONArray)new JSONParser().parse(src);
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject q = i.next();
                links.put((String)q.get("id"), (String)q.get("url"));
            }
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return links;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("streams:[")+8,page.toString().indexOf("}]",page.toString().indexOf("streams:["))+2));

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
        String thumbLink = null; 
	String preThumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("thumbCDN")+10,'\"');
	String postThumb = CommonUtils.getLink(page.toString(),page.toString().indexOf("poster",page.toString().indexOf("thumbCDN")+10)+8,'\"');
        thumbLink = preThumb + postThumb;
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }
    
    @Override protected void setExtractorName() {
        extractorName = "Anysex";
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
        if (url == null) return null;
        
        video v = null;
        Document page = getPage(url,false);
        Elements divs = page.select("section.thumb-list.videos").select("div.item.rollable");
        Random randomNum = new Random(); int count = 0; boolean got = false; if (divs.isEmpty()) got = true;
        while(!got) {
            if (count > divs.size()) break;
            int i = randomNum.nextInt(divs.size()); count++;
            String link = "https://www.porn.com" + divs.get(i).select("div.thumb").select("a").attr("href");
            String title = divs.get(i).select("div.thumb").select("a").attr("title");
            String thumb = divs.get(i).select("div.thumb").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
            File thumbFile = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
            try {v = new video(link,title,thumbFile,getSize(link)); } catch(GenericDownloaderException | IOException e) {continue;}
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://www.porn.com/videos/search?q="+str.replaceAll(" ", "+");
	Document page = getPage(searchUrl,false);

        Elements divs = page.select("section.thumb-list.videos").select("div.item.rollable"); video v = null;
	for(Element div: divs) {
            String link = "https://www.porn.com" + div.select("div.thumb").select("a").attr("href");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            String thumb = div.select("div.thumb").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
            File thumbFile = new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
            try { v = new video(link,div.select("div.thumb").select("a").attr("title"),thumbFile,getSize(link)); } catch (GenericDownloaderException | IOException e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    public long getSize(String url) throws IOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
     
        Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("streams:[")+8,page.toString().indexOf("}]",page.toString().indexOf("streams:["))+2));
        return CommonUtils.getContentSize(qualities.get(qualities.keySet().iterator().next()));
    }

    @Override public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
    
    @Override public String getId(String link) {
        Pattern p = Pattern.compile("https://(www.)?pornpics.com/galleries/([\\S]+)/");
        Matcher m = p.matcher(link);
        return m.find() ? m.group(2) : "";
    }

    @Override public String getId() {
        return getId(url);
    }
}
