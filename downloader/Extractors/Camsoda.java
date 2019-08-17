/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.CommonUtils;
import downloader.DataStructures.MediaDefinition;
import downloader.DataStructures.MediaQuality;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.OfflineException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PageParseException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */
public class Camsoda extends GenericExtractor {
    
    public Camsoda() {
        
    }

    public Camsoda(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),getStarName(configureUrl(url)));
    }
    
    public Camsoda(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException,GenericDownloaderException, Exception{
        this(url,thumb,getStarName(configureUrl(url)));
    }
    
    public Camsoda(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    private static String getStarName(String url) {
        return getId(url, "https?://(?:www[.])?camsoda[.]com/(?<id>[^/?#]+)");
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception {
        String name = getStarName(url);
        String rawJson = Jsoup.connect("https://www.camsoda.com/api/v1/user/"+name).ignoreContentType(true).execute().body();
        verify(rawJson);
        String thumb = addHost(CommonUtils.eraseChar(CommonUtils.getLink(rawJson, rawJson.indexOf("\"profile_picture\":\"") + 19, '\"'), '\\'), "");
        
        String thumbName = CommonUtils.addId(CommonUtils.getThumbName(thumb), CommonUtils.getCurrentTimeStamp().replaceAll("[:/]", "-"));
        if (CommonUtils.checkImageCache(thumbName))
            new File(MainApp.imageCache.getAbsolutePath() + File.separator + thumbName).delete();
        CommonUtils.saveFile(thumb, thumbName, MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath() + File.separator + thumbName);
    }

    @Override public MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        String userJson = Jsoup.connect("https://www.camsoda.com/api/v1/user/"+getStarName(url)).ignoreContentType(true).execute().body();
        String serverJson = Jsoup.connect("https://www.camsoda.com/api/v1/video/vtoken/"+getStarName(url)+"?username=guest_"+String.valueOf(new Random().nextInt(98999) + 1000)).ignoreContentType(true).execute().body();
        
        String server = verify(userJson, serverJson);
        String app, streamName, token;
        try {
            JSONObject videoData = (JSONObject)new JSONParser().parse(serverJson);
            app = (String)videoData.get("app");
            streamName = (String)videoData.get("stream_name");
            token = (String)videoData.get("token");
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
        
        String m3u8Url = "https://"+server+"/"+app+"/mp4:"+streamName+"_aac/playlist.m3u8?token="+token;
        Map<String, String> q = CommonUtils.parseM3u8Formats(m3u8Url);
        MediaDefinition media = new MediaDefinition();
        Map<String, MediaQuality> qualities = new HashMap<>();
        q.keySet().iterator().forEachRemaining(a ->
                qualities.put(a, new MediaQuality(q.get(a), "mp4", true)));
        media.addThread(qualities, videoName);
        return media;
    }
    
    private static void verify(String user) throws GenericDownloaderException {
        if (user.matches(".*\"status\":false,.*"))
            throw new PageNotFoundException("User does not exist");
    }
    
    private static String verify(String user, String server) throws GenericDownloaderException {
        verify(user);
        try {
            String json = getId(server, "\"edge_servers\":(?<id>\\[.*?\\])");
            JSONArray servers = (JSONArray)new JSONParser().parse(json);
            if (servers.isEmpty())
                throw new OfflineException("No streams are avaliable", true);
            else return (String)servers.get(0);
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }
    
    @Override public boolean isLive() {
        return true;
    }
    
    @Override public long getSize() {
        return -1;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:www[.])?camsoda[.]com/(?<id>[^/?#]+)"; 
        //https://www.camsoda.com/valeryromero
    }
}
