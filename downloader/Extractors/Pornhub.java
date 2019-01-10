/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import ChrisPackage.GameTime;
import downloader.CommonUtils;
import downloader.DataStructures.GenericQuery;
import downloader.DataStructures.downloadedMedia;
import downloader.DataStructures.video;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.PageNotFoundException;
import downloader.Exceptions.PrivateVideoException;
import downloader.Exceptions.VideoDeletedException;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import static java.lang.Thread.sleep;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public class Pornhub extends GenericQueryExtractor{
    private static final int skip = 4;
    
    public Pornhub() { //this contructor is used for when you jus want to query
        
    }
    
    public Pornhub(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,downloadThumb(configureUrl(url)),downloadVideoName(configureUrl(url)));
        
    }
    
    public Pornhub(String url, File thumb) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        this(url,thumb,downloadVideoName(configureUrl(url)));
    }
    
    public Pornhub(String url, File thumb, String videoName) {
        super(url,thumb,videoName);
    }
    
    private static String toogle(String s) {
        //was used to redirect this extractors link to thumbzilla
        if(!isAlbum(s) || !isPhoto(s))
            return "https://www.thumbzilla.com/video/" + s.split("=")[1] + "/s";
        else return s;
    }
    
    private void downloadPic(String link, OperationStream s, String folder) throws MalformedURLException {
        long stop = 0; String name = CommonUtils.getPicName(link);
        do {
            if (s != null) s.addProgress("Trying "+CommonUtils.clean(name));
            if (folder == null)
                stop = CommonUtils.saveFile(link,CommonUtils.clean(name),MainApp.settings.preferences.getPictureFolder().getAbsolutePath(),s);
            else stop = CommonUtils.saveFile(link,CommonUtils.clean(name),MainApp.settings.preferences.getPictureFolder().getAbsolutePath()+File.separator+folder,s);
            try {
                sleep(4000);
            } catch (InterruptedException ex) {
                System.out.println("Failed to pause");
            }
        }while(stop != -2); //retry download if failed
    }
    
    private void getPic(String link, OperationStream s, String folder) throws MalformedURLException, IOException {
        Document page = Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.PCCLIENT).get().html());
        while(page.toString().contains("RNKEY"))
            page = Jsoup.parse(Jsoup.connect(link).cookie("RNKEY",getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
        String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
        downloadPic(img,s,folder);
    }
    
    private static Map<String,String> getQualities(String src) {
        int from = 0, occur = 0;
        
        Map<String,String> qualities = new HashMap<>();
        while((occur = src.indexOf("quality",from)) != -1) {
            qualities.put(CommonUtils.getLink(src, occur+10, '\"'),CommonUtils.eraseChar(CommonUtils.getUrl(src,occur), '\\'));
            if (qualities.get(CommonUtils.getLink(src, occur+10, '\"')).length() == 0)
                qualities.remove(qualities.get(CommonUtils.getLink(src, occur+10, '\"')));
            from = occur + 1;
        }
        
        return qualities;
    }
    
    @Override
    public void getVideo(OperationStream s) throws IOException,SocketTimeoutException,UncheckedIOException, GenericDownloaderException, Exception{
        if (s != null) s.startTiming();
        Document page;
        
        if (isPhoto(url)) {
            getPic(url,s,null);
            GameTime took = s.endOperation();
            if (s != null) s.addProgress("Took "+took.getTime()+" to download");
            MainApp.createNotification("Download Success","Finished Downloading "+getVideoName());
            File saved = new File(MainApp.settings.preferences.getPictureFolder() + File.separator + CommonUtils.clean(getVideoName()));
            MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
        } else if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            Elements items = page.select("li.photoAlbumListContainer");
            for(int i = 0; i < items.size(); i++) {
                String subLink = "https://www.pornhub.com" + items.get(i).select("a").attr("href");
                try {getPic(subLink,s,this.videoName);} catch(UncheckedIOException | NullPointerException e) {i--; sleep(4000);/*retry link*/}
            }
            GameTime took = s.endOperation();
            if (s != null) s.addProgress("Took "+took.getTime()+" to download");
            MainApp.createNotification("Download Success","Finished Downloading Album"+getVideoName());
            File saved = new File(MainApp.settings.preferences.getPictureFolder() + File.separator + this.videoName);
            MainApp.downloadHistoryList.add(new downloadedMedia(videoName,videoThumb,saved,name()));
        } else { //must be a video
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY",getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            verify(page);
            //Element video = page.getElementById("videoShow"); video.attr("data-default");
            Elements titleSpan = page.select("span.inlineFree");
            String title = Jsoup.parse(titleSpan.toString()).body().text(); //pull out text from span
            String rawQualities = CommonUtils.getLink(page.toString(), page.toString().indexOf(":",page.toString().indexOf("mediaDefinitions")) + 4, ']');
            Map<String,String> quality = getQualities(rawQualities);
            String video = null;
            if(quality.containsKey("720"))
                video = quality.get("720");
            else if (quality.containsKey("480"))
                video = quality.get("480");
            else if (quality.containsKey("360"))
                video = quality.get("360");
            else video = quality.get("240");
                
            super.downloadVideo(video,title,s);
        }
    }
    
    private static void verify(Document page) throws GenericDownloaderException {
        if(Jsoup.parse(page.select("title").toString()).text().equals("Page Not Found"))
            throw new PageNotFoundException(page.location());
        if (!page.select("div.privateContainer").isEmpty())
                throw new PrivateVideoException();
        if (!page.select("div.removed").isEmpty()) {
        	Elements span = page.select("div.removed").select("div.notice.video-notice").select("span");
        	if (!span.isEmpty())
        		throw new VideoDeletedException(span.text());
        	else throw new VideoDeletedException("Video was removed");
        }
        Element e; 
       if ((e = page.getElementById("messageWrapper")) != null) {
    	   if (Jsoup.parse(e.select("p").toString()).body().text().equals("This video was deleted by uploader."))
    		   throw new VideoDeletedException();
    	   else if (e.select("p").text().toLowerCase().contains("video has been removed"))
    		   throw new VideoDeletedException(e.select("p").text());
       }
       e = page.getElementById("userPremium");
       if (e != null) 
    	   throw new PrivateVideoException("Premium Video");
    }

    @Override
    public GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception{
        search = search.trim(); 
        search = search.replaceAll(" ", "+");
        String searchUrl = "https://pornhub.com/video/search?search="+search;
        
        GenericQuery thequery = new GenericQuery();
        Document page = getPage(searchUrl,false);
        while(page.toString().contains("RNKEY"))
            page = Jsoup.parse(Jsoup.connect(searchUrl).referrer(searchUrl).cookie("RNKEY",getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
                
	Elements searchResults = page.select("ul.videos.search-video-thumbs").select("li");
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://pornhub.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            try {
                Document linkPage = getPage("https://pornhub.com"+searchResults.get(i).select("a").attr("href"),false);
                while(linkPage.toString().contains("RNKEY"))
                    linkPage = Jsoup.parse(Jsoup.connect("https://pornhub.com"+searchResults.get(i).select("a").attr("href")).cookie("RNKEY", getRNKEY(linkPage.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
                verify(linkPage);
            } catch (GenericDownloaderException e) {continue;}
            thequery.addLink("https://pornhub.com"+searchResults.get(i).select("a").attr("href"));
            String thumbLink = searchResults.get(i).select("a").select("img").attr("data-mediumthumb");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            thequery.addThumbnail(new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)));
            thequery.addPreview(parse(thequery.getLink(i)));
            thequery.addName(searchResults.get(i).select("a").attr("title"));
	}
        return thequery;
    }
    
    //get preview thumbnails
    @Override
    protected Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException{ 
        Vector<File> thumbs = new Vector<>();
        
        try {
            String html;
            if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,true))) //check to see if page was downloaded previous 
                html = CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true));
            else {
                html = Jsoup.connect(url).followRedirects(true).cookie("trial-step1-modal-shown", "null").userAgent(CommonUtils.MOBILECLIENT).get().html(); //not found so download it
                while(html.contains("RNKEY")) //.cookie("trial-step1-modal-shown", "null")
                    html = Jsoup.connect(url).cookie("RNKEY",getRNKEY(html)).cookie("atatusScript","hide").cookie("ua","5b8fd1d60da9f748d773c2f3fc6ec89e").cookie("bs","bm8unqfp3axtovr7u7xpil9rlrabd02g").cookie("ss","472939886686767906").cookie("RNLBSERVERID","ded6856").userAgent(CommonUtils.MOBILECLIENT).referrer("https://www.google.com").get().html();
                CommonUtils.savePage(html, url, true);
            }
            Document page = Jsoup.parse(html);
            Element preview = page.getElementById("thumbDisplay");
            Elements previewImg = preview.select("img");

            Iterator<Element> img = previewImg.iterator();
            while(img.hasNext()) {
                String thumb = img.next().attr("data-src");
                if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip+1))) //if file not already in cache download it
                    CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip+1),MainApp.imageCache);
                thumbs.add(new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip+1)));
            }
        } catch (NullPointerException e) {
            CommonUtils.erasePage(CommonUtils.getCacheName(url,true));
            return thumbs; //return parse(url); //possible it went to interstitial? instead
        }
        return thumbs;
    }
    
    private static String getRNKEY(String page) {
        page = page.replace("document.cookie=", "return");
        page = page.substring(page.indexOf("<!--")+4,page.indexOf("//-->"));
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        try {
            engine.eval(page);
            Invocable inv = (Invocable)engine;
            return ((String)inv.invokeFunction("go")).split(("="))[1];
        } catch (ScriptException | NoSuchMethodException e) {e.printStackTrace();System.out.println(e.getMessage());return " ";}
    }
    
    private static boolean isAlbum(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if (url.matches("https://www.pornhub.com/album/[\\S]*") || url.matches("https://pornhub.com/album/[\\S]*"))
            return true;
        else return false;
    }
    
    private static boolean isPhoto(String url) {
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if (url.matches("https://www.pornhub.com/photo/[\\S]*") || url.matches("https://pornhub.com/photo/[\\S]*"))
            return true;
        if (url.matches("https://www.pornhub.com/gif/[\\S]*") || url.matches("https://pornhub.com/gif/[\\S]*"))
            return true;
        else return false;
    }
    
    private static boolean isGif(String url) {
        if (url.matches("https://www.pornhub.com/gif/[\\S]*") || url.matches("https://pornhub.com/gif/[\\S]*"))
            return true;
        else return false;
    }
    
     private static String downloadVideoName(String url) throws IOException , SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception{
        Document page;
        if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            //String subUrl = "https://www.pornhub.com" + page.select("li.photoAlbumListContainer").get(0).select("a").attr("href");
            //Document subpage = Jsoup.parse(Jsoup.connect(subUrl).userAgent(CommonUtils.PCCLIENT).get().html());
            //String img = subpage.getElementById("photoImageSection").select("div.centerImage").select("a").select("img").attr("src");
            if (page.select("h1.photoAlbumTitleV2").text().contains("<"))
                return page.select("h1.photoAlbumTitleV2").text().substring(0,page.select("h1.photoAlbumTitleV2").text().indexOf("<"));
            else return page.select("h1.photoAlbumTitleV2").text();
        } else if (isPhoto(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
            return CommonUtils.getPicName(img);
        } else {
            if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,true))) //check to see if page was downloaded previous
               page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true)));
            else {
                String html = Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).get().html();
                while(html.contains("RNKEY"))
                    html = Jsoup.connect(url).cookie("RNKEY", getRNKEY(html)).userAgent(CommonUtils.MOBILECLIENT).get().html();
                page = Jsoup.parse(html);
               CommonUtils.savePage(html, url, true);
            }
            verify(page);
           Elements titleSpan = page.select("span.inlineFree");
           String title = Jsoup.parse(titleSpan.toString()).body().text(); //pull out text in span
           return title;
        }
    } 
	
    //getVideo thumbnail
    private static File downloadThumb(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException, Exception {
        Document page;
        if (isAlbum(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            String subUrl = "https://www.pornhub.com" + page.select("li.photoAlbumListContainer").get(0).select("a").attr("href");
            Document subpage = Jsoup.parse(Jsoup.connect(subUrl).userAgent(CommonUtils.PCCLIENT).get().html());
            String img = subpage.getElementById("photoImageSection").select("div.centerImage").select("a").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(img))) //if file not already in cache download it
                CommonUtils.saveFile(img,CommonUtils.getThumbName(img),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(img));
        } else if (isPhoto(url)) {
            page = Jsoup.parse(Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html());
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            String img = "";
            Element div = page.getElementById("photoImageSection");
            if (div == null) { div = page.getElementById("gifImageSection"); img = div.select("div.centerImage").attr("data-gif"); }
            else img = div.select("div.centerImage").select("a").select("img").attr("src");
            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(img))) //if file not already in cache download it
                CommonUtils.saveFile(img,CommonUtils.getThumbName(img),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(img));
        } else {
            if (CommonUtils.checkImageCache(CommonUtils.getCacheName(url,true))) {//check to see if page was downloaded previous
                page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,true)));
            } else {
                 String html = Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).get().html();
                 while(html.contains("RNKEY"))
                    html = Jsoup.connect(url).cookie("RNKEY", getRNKEY(html)).userAgent(CommonUtils.MOBILECLIENT).get().html();
                page = Jsoup.parse(html);
                CommonUtils.savePage(html, url, true);
             }
            verify(page);
            Elements metas = page.select("meta");
            String thumb = "";
            
            Iterator<Element> i = metas.iterator();
            while(i.hasNext()) {
                Element temp = i.next();
                if (temp.attr("property").equals("og:image")) {
                    thumb = temp.attr("content"); break;
                }
            }

            if(!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip+1))) //if file not already in cache download it
                CommonUtils.saveFile(thumb,CommonUtils.getThumbName(thumb,skip+1),MainApp.imageCache);
            return new File(MainApp.imageCache.getAbsolutePath()+File.separator+CommonUtils.getThumbName(thumb,skip+1));
        }
    }
    
    @Override
    protected void setExtractorName() {
        extractorName = "Pornhub";
    }

    @Override
    public video similar() throws IOException{
        if (url == null) return null;
        if(isAlbum(url) || isPhoto(url)) {
            return null;
        } else {
            Random rand = new Random();
            if (rand.nextBoolean())
                return getRelated();
            else return getRecommended();
        }
    }
    
    private video getRelated() throws IOException {
        return getRelated(0);
    }
    
    private video getRelated(int tries) throws IOException {
        System.out.println("chose related");
        if (url == null) return null;
        
        video v = null;
        try {
            Document page = getPage(url,false);
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            Elements li = page.getElementById("relatedVideosCenter").select("li");
            for(int i = 0; i < li.size(); i++) {
                String link = "http://pornhub.com" + li.get(i).select("div.phimage").select("a.img").attr("href"); try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
                //if (!link.startsWith("http://pornhub.com") || !link.startsWith("https://pornhub.com")) link = "http://pornhub.com" + link;
                String thumb = li.get(i).select("div.phimage").select("a.img").select("img").attr("src");
                if (thumb.length() < 1) li.get(i).select("div.phimage").select("a.img").select("img").attr("data-thumb_url");
                String title = li.get(i).select("div.phimage").select("a.img").attr("data-title");
                if (title.length() < 1) title = li.get(i).select("div.phimage").select("a.img").attr("data-title");
                try {if (title.length() < 1) title = downloadVideoName(link);}catch(Exception e) {continue;}
                if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
                try {v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)),getSize(link)); }catch (GenericDownloaderException | IOException e) {}
                break;
            }
        } catch (NullPointerException e) {
            if (tries < 1) return getRecommended(1);
        }
        return v;
    }
    
    private video getRecommended() throws IOException {
        return getRecommended(0);
    }
    
    private video getRecommended(int tries) throws IOException {
        System.out.println("chose recommeded");
        if (url == null) return null;
        
        video v = null;
        try {
            Document page = getPage(url,false);
            while(page.toString().contains("RNKEY"))
                page = Jsoup.parse(Jsoup.connect(url).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
            Elements li = page.getElementById("relateRecommendedItems").select("li");
            for(int i = 0; i < li.size(); i++) {
                String link = "https://www.pornhub.com" + li.select("div.phimage").select("a.img").attr("href"); try {verify(getPage(link,false));} catch (GenericDownloaderException e) {continue;}
                //if (!link.startsWith("http://pornhub.com") || !link.startsWith("https://pornhub.com")) link = "http://pornhub.com" + link;
                String thumb = li.select("div.phimage").select("a.img").select("img").attr("src");
                if (thumb.length() < 1) li.select("div.phimage").select("a.img").select("img").attr("data-thumb_url");
                String title = li.select("div.phimage").select("a.img").attr("data-title");
                if (title.length() < 1) title = li.select("div.phimage").select("a.img").attr("data-title");
                try {if (title.length() < 1) title = downloadVideoName(link);}catch(Exception e) {continue;}
                if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumb,skip))) //if file not already in cache download it
                    if (CommonUtils.saveFile(thumb, CommonUtils.getThumbName(thumb,skip),MainApp.imageCache) != -2)
                        throw new IOException("Failed to completely download page");
                try {v = new video(link,title,new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumb,skip)),getSize(link)); }catch (GenericDownloaderException | IOException e) {}
            }
        } catch (NullPointerException e) {
            if (tries < 1) return getRelated(1);
        }
        return v;
    }

    @Override public video search(String str) throws IOException {
        str = str.trim(); str = str.replaceAll(" ", "+");
        String searchUrl = "https://pornhub.com/video/search?search="+str;
        
        Document page = getPage(searchUrl,false);
        while(page.toString().contains("RNKEY"))
            page = Jsoup.parse(Jsoup.connect(searchUrl).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
        video v = null;
        
	Elements searchResults = page.select("ul.videos.search-video-thumbs").select("li");
        //get first valid video
	for(int i = 0; i < searchResults.size(); i++)  {
            if (!CommonUtils.testPage("https://pornhub.com"+searchResults.get(i).select("a").attr("href"))) continue; //test to avoid error 404
            try {
                Document pageLink = Jsoup.parse(Jsoup.connect("https://pornhub.com"+searchResults.get(i).select("a").attr("href")).userAgent(CommonUtils.PCCLIENT).get().html());
                while(pageLink.toString().contains("RNKEY"))
                    pageLink = Jsoup.parse(Jsoup.connect("https://pornhub.com"+searchResults.get(i).select("a").attr("href")).cookie("RNKEY", getRNKEY(pageLink.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
                verify(pageLink);
                
            } catch (GenericDownloaderException e) {continue;}
            String thumbLink = searchResults.get(i).select("a").select("img").attr("data-mediumthumb");
            if (!CommonUtils.checkImageCache(CommonUtils.getThumbName(thumbLink,skip))) //if file not already in cache download it
                if (CommonUtils.saveFile(thumbLink, CommonUtils.getThumbName(thumbLink,skip),MainApp.imageCache) != -2)
                    throw new IOException("Failed to completely download page");
            String link = "https://pornhub.com"+searchResults.get(i).select("a").attr("href");
            try { v = new video(link,searchResults.get(i).select("a").attr("title"),new File(MainApp.imageCache+File.separator+CommonUtils.getThumbName(thumbLink,skip)),getSize(link)); } catch (GenericDownloaderException | IOException e) {continue;}
            break; //if u made it this far u already have a vaild video
	}
        return v;
    }

    @Override
    public long getSize() throws IOException, GenericDownloaderException {
        return getSize(url);
    }
    
    private static long getSize(String link) throws IOException, GenericDownloaderException{
        Document page = Jsoup.parse(Jsoup.connect(link).userAgent(CommonUtils.PCCLIENT).get().html());
        while(page.toString().contains("RNKEY"))
            page = Jsoup.parse(Jsoup.connect(link).cookie("RNKEY", getRNKEY(page.toString())).userAgent(CommonUtils.PCCLIENT).get().html());
        verify(page);
        //Element video = page.getElementById("videoShow"); video.attr("data-default");
        String rawQualities = CommonUtils.getLink(page.toString(), page.toString().indexOf(":",page.toString().indexOf("mediaDefinitions")) + 4, ']');
        Map<String,String> quality = getQualities(rawQualities);
        String video = null;
        if(quality.containsKey("720"))
            video = quality.get("720");
        else if (quality.containsKey("480"))
            video = quality.get("480");
        else if (quality.containsKey("360"))
            video = quality.get("360");
        else video = quality.get("240");
            return CommonUtils.getContentSize(video);
    }
}
