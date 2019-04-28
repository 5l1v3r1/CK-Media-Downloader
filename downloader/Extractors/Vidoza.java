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
import java.util.Map;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */
public class Vidoza extends GenericExtractor {
    private static final int SKIP = 3;
    
    public Vidoza() { //this contructor is used for when you jus want to search
        
    }

    public Vidoza(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
    }
    
    public Vidoza(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Vidoza(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private Map<String, String> getQualities(String src) throws PageParseException {
        Map<String, String> links = new HashMap<>();
        
        try {
            CommonUtils.log(src.substring(13,src.indexOf("}],")+2),this);
            String s = src.substring(13,src.indexOf("}],")+2).replace(" src", "\"src\"").replace("type", "\"type\"").replace("label", "\"label\"").replace("res", "\"res\"");
            JSONArray qualities = (JSONArray)new JSONParser().parse(s);
            for(int i = 0; i < qualities.size(); i++)
                links.put((String)((JSONObject)qualities.get(i)).get("res"), (String)((JSONObject)qualities.get(i)).get("src"));
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        return links;
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        
        MediaDefinition media = new MediaDefinition();
        media.addThread(getQualities(page.toString().substring(page.toString().indexOf("sourcesCode: "))) ,videoName);
            
        return media;
    }
    
    private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException,Exception{
        //return getH1Title(getPage(url,false));
        return getPage(url,false).select("title").text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page = getPage(url,false);
        String thumbLink = CommonUtils.getLink(page.toString().substring(page.toString().indexOf("poster:")), 9, '"');
         
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumbLink,CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumbLink,SKIP));
    }

    @Override public video similar() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override public video search(String str) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www.)?vidoza.net/(?<id>[\\S]+).html"; 
    }
}
