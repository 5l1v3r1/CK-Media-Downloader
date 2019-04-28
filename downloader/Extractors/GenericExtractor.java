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
import downloader.Exceptions.PageNotFoundException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author christopher
 */
public abstract class GenericExtractor {
    protected File videoThumb;
    protected String videoName, url;
    protected String extractorName;
    protected Map<String,String> cookieJar;
    protected boolean works;
    
    GenericExtractor(String url, File thumb, String videoName) {
       this();
       this.videoThumb = thumb;
       this.videoName = videoName;
       this.url = configureUrl(url);
    }
    
    GenericExtractor() { //this contructor is used for when you jus want to query / search
        this.cookieJar = new HashMap<>();
    }
    
    protected static Response getPageResponse(String url, boolean mobile) throws IOException {
        return mobile ? Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).header("Cookie","Country=US").execute() : Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).header("Cookie","Country=US").execute();
    }
    
    protected static Document getPage(String url, boolean mobile) throws IOException, GenericDownloaderException {
        return getPage(url,mobile,false);
    }
    
    protected static Document getPage(String url, boolean mobile, boolean force) throws FileNotFoundException, IOException, GenericDownloaderException {
        Document page;
        if (!force && CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile))) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else { String html;
            try {
               html = mobile ? Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).get().html() : Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).get().html();
               page = Jsoup.parse(html);
               CommonUtils.savePage(html, url, mobile);
            } catch (HttpStatusException e) {
                throw new PageNotFoundException(e.getMessage());
            }
        } //if not found in cache download it
        return page;
    }
    
    protected Document getPageCookie(String url, boolean mobile) throws IOException, FileNotFoundException, GenericDownloaderException {
        return getPage(url,mobile,false);
    }
    
    protected Document getPageCookie(String url, boolean mobile, boolean force) throws FileNotFoundException, IOException, GenericDownloaderException {
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile)) && !force) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else { String html;
            try {
               html = mobile ? addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.MOBILECLIENT).get().html() : addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.PCCLIENT).get().html();
               page = Jsoup.parse(html);
               CommonUtils.savePage(html, url, mobile);
            } catch (HttpStatusException e) {
                throw new PageNotFoundException(e.getMessage());
            }
        } //if not found in cache download it
        return page;
    }
    
    private Connection addCookies(Connection c) {
        Iterator<String> i = cookieJar.keySet().iterator();
        while(i.hasNext()) {
            String cookie = i.next();
            c = c.cookie(cookie, cookieJar.get(cookie));
        }
        return c;
    } 
    
    protected static String getCanonicalLink(Document page) {
        for(Element link: page.select("link"))
            if(link.attr("rel").equals("canonical"))
                return link.attr("href");
        return null;
    }
    
    protected static String getMetaImage(Document page) {
        return getMetaImage(page,false);
    }
     
    protected static String getMetaImage(Document page, boolean ignore) {
        String thumbLink;
        thumbLink = pullMetaImage(page.select("meta"), ignore, "property");
        thumbLink = thumbLink == null ? pullMetaImage(page.select("meta"), ignore, "name") : thumbLink;
        thumbLink = thumbLink == null ? pullMetaImage(page.select("meta"), ignore, "itemprop") : thumbLink;
        thumbLink = thumbLink == null ? pullMetaImage(page.select("meta"), ignore, "content") : thumbLink;
        if (thumbLink == null)
            for(Element meta :page.select("link"))
                if (meta.attr("rel").equals("image_src"))
                    thumbLink = meta.attr("href");
        if (thumbLink == null)
            for(Element meta :page.select("link"))
                if (meta.attr("rel").equals("icon"))
                    thumbLink = meta.attr("href");
        return thumbLink;
    }
    
    private static String pullMetaImage(Elements metas, boolean ignore, String attr) {
        for(Element meta :metas) {
            if (meta.attr(attr).equals("og:image"))
                if (!meta.attr("content").contains("static"))
                    return meta.attr("content");
                else if(ignore) //if contains static but we are ignoring
                    return meta.attr("content");
        }
        return null;
    }
    
    protected static String getTitle(Document page) {
        String title = null;
        //title = page.select("title").text();
        //if (title.length() < 1)
        for(Element meta :page.select("meta"))
            if(meta.attr("property").equals("og:title"))
                title = meta.attr("content");
        if (title == null)
            for(Element meta :page.select("meta"))
                if(meta.attr("itemprop").equals("name"))
                    title = meta.attr("content");
        return title;
    }
    
    protected static String getTitleTag(Document page) {
        return Jsoup.parse(page.select("title").toString()).text();
    }
    
    protected static String getH1Title(Document page) {
        return Jsoup.parse(page.select("h1").toString()).body().text();
    }
    
    protected Map<String,String> getDefaultVideo(Document page) {
        Map<String,String> q = new HashMap<>();
        if (page.select("video").isEmpty())
            return null;
        else if (page.select("video").select("source").isEmpty())    
            q.put("single",page.select("video").attr("src"));
        else  {
            if (page.select("video").select("source").size() > 1) {
                page.select("video").select("source").forEach((source) -> {
                    String format; int i = 0;
                    format = source.attr("title");
                    if(format.length() == 0)
                        format = source.attr("id");
                    if(format.length() == 0)
                        format = source.attr("label");
                    if(format.length() == 0)
                        format = source.attr("res");
                    if(format.length() == 0)
                        format = String.valueOf(i++);
                    String src = source.attr("src");
                    src = (src == null || src.length() < 1) ? page.select("video").attr("id") : src;
                    if (src.startsWith("//"))
                        src = "http:" + src;
                    else if (!src.startsWith("https") && !src.startsWith("http"))
                        src = "http://" + src;
                    q.put(format,src);
                });
            } else
                q.put("single",page.select("video").select("source").attr("src"));
        }
        return q;
    }
    
    protected static String getVideoPoster(Document page) {
        try {
            return page.select("video").get(0).attr("poster") != null && !page.select("video").get(0).attr("poster").isEmpty() ? page.select("video").get(0).attr("poster") : null;
        } catch (NullPointerException e) {
            return null;
        }
    }
    
    public String getVideoName() {
        return this.videoName;
    }
    
    public File getThumb() {
        return this.videoThumb;
    }
    
    public String getUrl() {
        return this.url;
    }
    
    final public boolean working() {
        return works;
    }
    
    protected void addCookie(String cookieName, String cookie) {
        cookieJar.put(cookieName, cookie);
    }
    
    protected void clearCookies() {
        cookieJar.clear();
    }
    
    //should probably implement a getVideo(url)
    public abstract MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException;
    public abstract video similar() throws IOException, GenericDownloaderException; //get a video from the related items list
    public abstract video search(String str) throws IOException, GenericDownloaderException; //search (similar to query except no img preview and only 1 result) 
    protected abstract String getValidRegex();
    
    final protected long getSize(MediaDefinition media) throws GenericDownloaderException, UncheckedIOException, IOException {
        long size = 0;
        if (!media.isSingleThread()) { //if more than one thread
            Iterator<Map<String,String>> i = media.iterator(); int j = 0;
            while(i.hasNext()) { //get best quality from thread
                Map<String,String> temp = i.next();
                String highestQuality = temp.keySet().size() == 1 ? temp.keySet().iterator().next() : CommonUtils.getSortedFormats(temp.keySet()).get(0);
                if(temp.get(highestQuality) != null || !temp.get(highestQuality).isEmpty()) {
                    long s = CommonUtils.getContentSize(temp.get(highestQuality));
                    size += s < 0 ? 0 : s;
                }
            }
        } else {
            Map<String,String> m = media.iterator().next();
            String highestQuality = m.keySet().size() == 1 ? m.keySet().iterator().next() : CommonUtils.getSortedFormats(m.keySet()).get(0);
            if(m.get(highestQuality) != null || !m.get(highestQuality).isEmpty()) {
                long s = CommonUtils.getContentSize(m.get(highestQuality));
                size += s < 0 ? 0 : s;
            }
        }
        return size;
    }
    
    final public long getSize() throws GenericDownloaderException, UncheckedIOException, IOException {
        return getSize(getVideo());
    }
    
    final static protected String getId(String link, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(link);
        return !m.find() ? "" :  m.group("id") == null || m.group("id").isEmpty() ? m.group("id2"): m.group("id");
    }
   
    final public String getId(String link) {
        return getId(link, getValidRegex());
    }
    
    final public String getId() {
        return getId(url, getValidRegex());
    }
    
    
    final public boolean suitable(String url) {
        return url.matches(getValidRegex()) && working();
    }
    
    final protected static String getDomain(String s) {
        Pattern p = Pattern.compile("https?://([^/]+)/[\\S]+");
        Matcher m = p.matcher(s);
        return !m.find() ? "" : m.group(1);
    }
    
    protected static String configureUrl(String link) {
        if (!link.matches("https?://[\\S]+")) return "https://" + link;
        else
            return link;
    }
    
    final protected static String changeHttp(String link) {
        return link.replace("https", "http");
    }
}
