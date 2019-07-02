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
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Imgur extends GenericExtractor {
    private static final byte SKIP = 1;
    
    public Imgur() { //this contructor is used for when you jus want to search
        
    }
    
    public Imgur(String url)throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(chop(url),downloadThumb(chop(configureUrl(url))),downloadVideoName(chop(configureUrl(url))));
    }
    
    public Imgur(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(chop(url),thumb,downloadVideoName(chop(configureUrl(url))));
    }
    
    public Imgur(String url, File thumb, String videoName) {
        super(chop(url),thumb,videoName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException{
        if (url.matches("https?://imgur[.]com/gallery/[\\S]*"))
            return getImgurGallery();
	else return getImgurSingle();
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
    	Document page = getPage(url,false);
        String data = page.toString().substring(page.toString().indexOf("image               : {")+22,page.toString().indexOf("},\n",page.toString().indexOf("image               : {"))+1);
	JSONObject details = (JSONObject)new JSONParser().parse(data);
        return ((String)details.get("title")).length() < 1 ? url.substring(url.lastIndexOf("/")+1) : (String)details.get("title");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        Document page = getPage(url,false);
        
        String thumb = null;
        Elements metas = page.select("meta");
	for(Element meta : metas) {
            if(meta.attr("property").equals("twitter:image")) {
                thumb = meta.attr("content");
		break;	
            }
	}
	if ((thumb == null) || (thumb.length() < 1)) thumb = getMetaImage(page);

        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    private static String chop(String s) {
    	return s.charAt(s.length()-1) == '/' ? s.substring(0,s.length()-1) : s;    		
    }
    
    private MediaDefinition getImgurGallery() throws IOException, GenericDownloaderException {
        try {
            Document page = getPage(url,false,true);
            String data = page.toString().substring(page.toString().indexOf("image               : {")+22,page.toString().indexOf("},\n",page.toString().indexOf("image               : {"))+1);
            JSONObject details = (JSONObject)new JSONParser().parse(data);
            if (details.get("album_images") == null)
                return getSingle(details);
            else {
                MediaDefinition media = new MediaDefinition();
                int count = Integer.parseInt(String.valueOf(((JSONObject)details.get("album_images")).get("count")));
		JSONArray images = ((JSONArray)((JSONObject)details.get("album_images")).get("images"));
		String title = (String)details.get("title");
        	for(int i = 0; i < count; i++) {
                    String name = title.length() < 1 ? ((JSONObject)images.get(i)).get("hash")+" "+String.valueOf(i+1)+((JSONObject)images.get(i)).get("ext") : title + " " + String.valueOf(i+1);
                    String link = addHost(((JSONObject)images.get(i)).get("hash")+""+((JSONObject)images.get(i)).get("ext"),"i.imgur.com"); 
                    Map<String,String> qualities = new HashMap<>();
                    qualities.put("single",link); media.setAlbumName(videoName);
                    media.addThread(qualities,name);
                }
                return media;
            }
	} catch (ParseException e) {
            throw new PageParseException(e.getMessage());
	}
    }
	
    private MediaDefinition getSingle(JSONObject details) throws IOException {
        String title = (String)details.get("title");
               
        String name = title.length() < 1 ? details.get("hash")+""+details.get("ext") : title;
        String link = addHost(details.get("hash")+""+details.get("ext"),"i.imgur.com"); 
        Map<String,String> qualities = new HashMap<>(); MediaDefinition media = new MediaDefinition();
        qualities.put("single",link); media.addThread(qualities,name);
        return media;
    }
	
    private MediaDefinition getImgurSingle() throws IOException, GenericDownloaderException{
        try {
            Document page = getPage(url,false,true);
            String data = page.toString().substring(page.toString().indexOf("image               : {")+22,page.toString().indexOf("},\n",page.toString().indexOf("image               : {"))+1);
            JSONObject details = (JSONObject)new JSONParser().parse(data);
            return getSingle(details);
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?imgur[.]com/(?:gallery/)?(?<id>[\\S]+)"; 
    }
}
