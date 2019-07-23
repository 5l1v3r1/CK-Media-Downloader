/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
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
import java.util.Random;
import java.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Xhamster extends GenericQueryExtractor implements Searchable{
    private static final byte SKIP = 5;
    
    public Xhamster() { //this contructor is used for when you jus want to query
        
    }
    
    public Xhamster(String url)throws IOException, SocketTimeoutException, UncheckedIOException , Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, boolean mobile) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(convertUrl(url),downloadThumb(convertUrl(configureUrl(url))),downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        this(convertUrl(url),thumb,downloadVideoName(convertUrl(configureUrl(url))));
    }
    
    public Xhamster(String url, File thumb, String videoName){
        super(convertUrl(url),thumb,videoName);
    }

    private static Map<String,String> getQualities(String s) throws PageParseException {
        Map<String,String> q = new HashMap<>();
        try {
            JSONArray json = (JSONArray)new JSONParser().parse(s.substring(0,s.indexOf("}]")+2));
            Iterator<JSONObject> i = json.iterator();
            while(i.hasNext()) {
                JSONObject quality = i.next();
                if (quality.get("quality").equals("auto")) continue;
                q.put((String)quality.get("quality"), CommonUtils.eraseChar((String)quality.get("fallback"),'\\'));
            }
            return q;
        } catch (ParseException e) {
            throw new PageParseException(e.getMessage());
        }
    }
    
    @Override public MediaDefinition getVideo() throws IOException,SocketTimeoutException, UncheckedIOException, GenericDownloaderException {
        Document page = getPage(url,false,true);
        MediaDefinition media = new MediaDefinition();
        
        if (!isAlbum(url)) {
            Map<String,String> qualities = getQualities(page.toString().substring(page.toString().indexOf("mp4\":[")+5));

            //String video = page.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
            media.addThread(qualities,videoName);
        } else {
            //https://xhamster.com/photos/gallery/julie-anderson-977633 https://xhamster.com/photos/gallery/12272844/297194743
            final int max = Integer.parseInt(page.select("span.page-title__count").text());
            Elements a = page.getElementById("photo-slider").select("a"); int next = 2;
            while(a.size() < max) //pagination
                a.addAll(getPage(url+"/"+next++,false,true).getElementById("photo-slider").select("a"));
            for(int i = 0; i < a.size(); i++) {
                Map<String,String> pic = new HashMap<>();
                String link = a.get(i).attr("href");
                pic.put("single",link);
                media.addThread(pic, videoName+"-"+link.split("/")[link.split("/").length -1]);
            }
            media.setAlbumName(videoName);
        }
        return media;
    }

    @Override public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        String searchUrl = "https://xhamster.com/search?q="+search.trim().replaceAll(" ", "+");
        GenericQuery thequery = new GenericQuery();
        
	Elements searchResults = getPage(searchUrl,false).select("div.thumb-list__item.video-thumb");
	for(int i = 0; i < searchResults.size(); i++)  {
            String link = searchResults.get(i).select("a").attr("href");
            if (!CommonUtils.testPage(link)) continue; //test to avoid error 404
            thequery.addLink(link);
            String thumbLink = searchResults.get(i).select("a").select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(Jsoup.parse(searchResults.get(i).select("div.video-thumb-info").select("a").toString()).body().text());
            Document linkPage = getPage(searchResults.get(i).select("a").attr("href"),false);
            String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
            thequery.addSize(CommonUtils.getContentSize(video));
            thequery.addDuration(getDuration(link).toString());
	}
        return thequery;
    }
    
     //get preview thumbnails
    @Override protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, GenericDownloaderException {
        Vector<File> thumbs = new Vector<>();
        Elements previewImg = getPage(url,true).select("div.item").select("img"); //div containing imgs tags
			
        Iterator<Element> img = previewImg.iterator();
        while(img.hasNext()) { //loop through all img tags
            String thumb = img.next().attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
                CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
            thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP)));
        }
        return thumbs;
    }
    
    private static String downloadVideoName(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        return getPage(url,false).select("h1").text();
        //return isAlbum(url) ? page.select("h1").text() : Jsoup.parse(page.select("h1.entity-info-container__title").toString()).body().text();
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        //page.select("header").select("img").attr("src")
	String thumb = getMetaImage(getPage(url,true)); //!isAlbum(url) ? getMetaImage(page) : page.select("img.thumb").get(0).attr("src");
        
        if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache);
        return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,SKIP));
    }
    
    private static String convertUrl(String url) {
        url = url.replace("https://m", "https://www");
        if (url.matches("[\\S]*.m.xhamster.com/videos/[\\S]*"))
            return url.replaceFirst(".m", "");
        else return url;
    }
    
    private static boolean isAlbum(String link) {
        return link.matches("https?://(?:[\\S]+?[.])?xhamster[.](?:com|one)/photos/gallery/[^/]*-[\\d]+\\S*");
    }

    @Override public video similar() throws IOException, GenericDownloaderException {
    	if (url == null) return null;
        if (isAlbum(url)) return null;
        
        video v = null;
        Elements li =  getPage(url,false).select("div.thumb-list__item.video-thumb");
        Random randomNum = new Random(); int count = 0; boolean got = li.isEmpty();
        while(!got) {
            if (count > li.size()) break;
            int i = randomNum.nextInt(li.size()); count++;
            String link = li.get(i).select("a").get(0).attr("href");
            String thumb = li.get(i).select("img").attr("src");
            String title = li.get(i).select("a.video-thumb-info__name").text();
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,SKIP))) //if file not already in cache download it
            	if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,SKIP),MainApp.imageCache) != -2)
                    continue;//throw new IOException("Failed to completely download page");
            Document linkPage = getPage(link,false);
 
            String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href");
            v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,SKIP)),CommonUtils.getContentSize(video), getDuration(link).toString());
            break;
        }
        return v;
    }

    @Override public video search(String str) throws IOException, GenericDownloaderException {
        String searchUrl = "https://xhamster.com/search?q="+str.trim().replaceAll(" ", "+");
        
        
	Elements searchResults = getPage(searchUrl,false).select("div.thumb-list__item.video-thumb");
        int count = searchResults.size(); Random rand = new Random(); video v = null;
        
	while(count-- > 0) {
            int i = rand.nextInt(searchResults.size());
            if (!CommonUtils.testPage(searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            String thumbLink = searchResults.get(i).select("a").select("img").attr("src");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,SKIP))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,SKIP),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            Document linkPage = getPage(searchResults.get(i).select("a").attr("href"),false);
 
            String video = linkPage.select("a.player-container__no-player.xplayer.xplayer-fallback-image.xh-helper-hidden").attr("href"),
                    link = searchResults.get(i).select("a").attr("href");
            v = new video(link,Jsoup.parse(searchResults.get(i).select("div.video-thumb-info").select("a").toString()).body().text(),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,SKIP)),CommonUtils.getContentSize(video), getDuration(link).toString());
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }
    
    private GameTime getDuration(String link) throws IOException, GenericDownloaderException {
        if (isAlbum(link))
            return new GameTime();
        else {
            long secs = Integer.parseInt(getId(getPage(link,false).toString(),".*videoId\":"+getId(link, getValidRegex())+",\"duration\":(?<id>\\d+).*"));
            GameTime g = new GameTime();
            g.addSec(secs);
            return g;
        }
    }
    
    @Override public GameTime getDuration() throws IOException, GenericDownloaderException {
        if (isAlbum(url))
            return new GameTime();
        else return getDuration(url);
    }
    
    @Override public Vector<String> getKeywords() throws IOException, GenericDownloaderException {
        if (url == null || isAlbum(url)) return null;
        Vector<String> words = new Vector<>();
        Elements a = getPage(url, false).select("a.categories-container__item");
        for(int i = 1; i < a.size(); i++)
            words.add(a.get(i).text());
        return words;
    }

    @Override public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }

    @Override protected String getValidRegex() {
        works = true;
        return "https?://(?:[\\S]+?[.])?xhamster[.](?:com|one)/(?:movies/(?<id>[\\d]+)/(?<displayid>[^/]*)[.]html([?]\\S*)?|videos/(?<displayid2>[^/]*)-(?<id2>[\\d]+)|photos/gallery/(?<displayid3>[^/]*)-(?<id3>[\\d]+))"; 
    }
}
