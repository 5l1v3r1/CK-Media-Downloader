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
import downloader.Exceptions.PageNotFoundException;
import downloaderProject.MainApp;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
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
    
    protected static Document getPage(String url, boolean mobile) throws IOException, GenericDownloaderException {
        return getPage(url,mobile,false, false);
    }
    
    protected static Document getPage(String url, boolean mobile, boolean force) throws IOException, GenericDownloaderException {
        return getPage(url,mobile,force, false);
    }
    
    protected static Document getPage(String url, boolean mobile, boolean force, boolean ignore) throws FileNotFoundException, IOException, GenericDownloaderException {
        Document page;
        if (!force && CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile))) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else { String html;
            try {
               html = mobile ? Jsoup.connect(url).userAgent(CommonUtils.MOBILECLIENT).ignoreHttpErrors(ignore).get().html() : Jsoup.connect(url).userAgent(CommonUtils.PCCLIENT).ignoreHttpErrors(ignore).get().html();
               page = Jsoup.parse(html);
               CommonUtils.savePage(html, url, mobile);
            } catch (HttpStatusException e) {
                throw new PageNotFoundException(e.getMessage());
            }
        } //if not found in cache download it
        return page;
    }
    
    protected Document getPageCookie(String url, boolean mobile) throws IOException, FileNotFoundException, GenericDownloaderException {
        return getPage(url,mobile,false, false);
    }
    
    protected Document getPageCookie(String url, boolean mobile, boolean force) throws IOException, FileNotFoundException, GenericDownloaderException {
        return getPage(url,mobile,force, false);
    }
    
    protected Document getPageCookie(String url, boolean mobile, boolean force, boolean ignore) throws FileNotFoundException, IOException, GenericDownloaderException {
        Document page;
        if (CommonUtils.checkPageCache(CommonUtils.getCacheName(url,mobile)) && !force) //check to see if page was downloaded previous 
            page = Jsoup.parse(CommonUtils.loadPage(MainApp.pageCache.getAbsolutePath()+File.separator+CommonUtils.getCacheName(url,mobile))); //load if not force redownload
        else {
            try {
               Response r = mobile ? addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.MOBILECLIENT).ignoreHttpErrors(ignore).execute() : addCookies(Jsoup.connect(url)).followRedirects(true).userAgent(CommonUtils.PCCLIENT).ignoreHttpErrors(ignore).execute();
               cookieJar.putAll(r.cookies());
               page = r.parse();
               CommonUtils.savePage(page.toString(), url, mobile);
            } catch (HttpStatusException e) {
                throw new PageNotFoundException(e.getMessage());
            }
        } //if not found in cache download it
        return page;
    }
    
    protected static Document getPageCookie(String url, boolean mobile, Map<String, String> cookies) throws IOException, PageNotFoundException {
        Document page; String html;
        try {
            html = mobile ? addCookies(Jsoup.connect(url), cookies).followRedirects(true).userAgent(CommonUtils.MOBILECLIENT).get().html() : addCookies(Jsoup.connect(url), cookies).followRedirects(true).userAgent(CommonUtils.PCCLIENT).get().html();
            page = Jsoup.parse(html);
        } catch (HttpStatusException e) {
            throw new PageNotFoundException(e.getMessage());
        }
        return page;
    }
    
    private static Connection addCookies(Connection c, Map<String, String> cookies) {
        Iterator<String> i = cookies.keySet().iterator();
        while(i.hasNext()) {
            String cookie = i.next();
            c = c.cookie(cookie, cookies.get(cookie));
        }
        return c;
    }
    
    private Connection addCookies(Connection c) {
        return addCookies(c, cookieJar);
    }    
    
    protected void addCookie(String cookieName, String cookie) {
        cookieJar.put(cookieName, cookie);
    }
    
    public String getCookies() {
        return CommonUtils.StringCookies(cookieJar);
    }
    
    final public boolean cookieEmpty() {
        return cookieJar.isEmpty();
    }
    
    final protected void clearCookies() {
        cookieJar.clear();
    }
    
    final protected static String getCanonicalLink(Document page) {
        for(Element link: page.select("link"))
            if(link.attr("rel").equals("canonical"))
                return link.attr("href");
        return null;
    }
    
    final protected static GameTime getMetaDuration(Document page) {
        Elements metas = page.select("meta");
        long secs = 0;
        for(Element meta :metas) {
            if (meta.attr("property").contains("video:duration") || meta.attr("property").contains("og:duration")) {
                secs = Integer.parseInt(meta.attr("content"));
                break;
            }
        }
        GameTime g = new GameTime();
        g.addSec(secs);
        return g;
    }
    
    final protected static String getMetaImage(Document page) {
        return getMetaImage(page,false);
    }
     
    final protected static String getMetaImage(Document page, boolean ignore) {
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
    
    final protected static String getTitle(Document page) {
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
    
    final protected static String getTitleTag(Document page) {
        return Jsoup.parse(page.select("title").toString()).text();
    }
    
    final protected static String getH1Title(Document page) {
        return Jsoup.parse(page.select("h1").toString()).body().text();
    }
    
    final protected Map<String, MediaQuality> getDefaultVideo(Document page) {
        Map<String, MediaQuality> q = new HashMap<>();
        if (page.select("video").isEmpty())
            return null;
        else if (page.select("video").select("source").isEmpty()) 
            q.put("single", new MediaQuality(configureUrl(page.select("video").attr("src"))));
        else {
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
                    String type = page.select("video").select("source").attr("type");
                    if (type == null || type.isEmpty()) 
                        q.put(format, new MediaQuality(configureUrl(src)));
                    else q.put(format, new MediaQuality(configureUrl(src), type));
                });
            } else {
                String type = page.select("video").select("source").attr("type");
                if (type == null || type.isEmpty()) 
                    q.put("single", new MediaQuality(configureUrl(page.select("video").select("source").attr("src"))));
                else q.put("single", new MediaQuality(configureUrl(page.select("video").select("source").attr("src")), type));
            }
        }
        return q;
    }
    
    final protected static String getVideoPoster(Document page) {
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
    
    //should probably implement a getVideo(url)
    public abstract MediaDefinition getVideo() throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException;
    protected abstract String getValidRegex();
    
    public boolean allowNoThumb() {
        return false;
    }
    
    public boolean isLive() {
        return false;
    }
    
    public video similar() throws IOException, GenericDownloaderException { //get a video from the related items list
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public GameTime getDuration() throws IOException, GenericDownloaderException {
        return new GameTime();
    }
    public Vector<String> getKeywords() throws IOException, GenericDownloaderException { //categories && tags
        return null;
    }
    public Vector<String> getStars() throws IOException, GenericDownloaderException {
        return null;
    }
    
    final static protected long getSize(MediaDefinition media, String cookieString) throws GenericDownloaderException, UncheckedIOException, IOException {
        long size = 0;
        if (media != null) {
            if (!media.isSingleThread()) { //if more than one thread
                Iterator<Map<String, MediaQuality>> i = media.iterator();
                while(i.hasNext()) { //get best quality from thread
                    Map<String, MediaQuality> temp = i.next();
                    String highestQuality = temp.keySet().size() == 1 ? temp.keySet().iterator().next() : CommonUtils.getSortedFormats(temp.keySet()).get(0);
                    if(temp.get(highestQuality) != null || !temp.get(highestQuality).getUrl().isEmpty()) {
                        long s = CommonUtils.getContentSize(temp.get(highestQuality).getUrl(), cookieString);
                        size += s < 0 ? 0 : s;
                    }
                }
            } else {
                Map<String, MediaQuality> m = media.iterator().next();
                String highestQuality = m.keySet().size() == 1 ? m.keySet().iterator().next() : CommonUtils.getSortedFormats(m.keySet()).get(0);
                if(m.get(highestQuality) != null || !m.get(highestQuality).getUrl().isEmpty()) {
                    long s = CommonUtils.getContentSize(m.get(highestQuality).getUrl(), cookieString);
                    size += s < 0 ? 0 : s;
                }
            }
        }
        return size;
    }
    
    final static protected long getSize(MediaDefinition media) throws GenericDownloaderException, UncheckedIOException, IOException {
        return getSize(media, null);
    }
    
    public long getSize() throws GenericDownloaderException, UncheckedIOException, IOException {
        return getSize(getVideo(), CommonUtils.StringCookies(cookieJar));
    }
    
    final static protected long getSeconds(String t) {
        Pattern p = Pattern.compile(CommonUtils.TIMEREGEX1);
        Matcher m = p.matcher(t);
        long secs = 0;
        if (!m.find()) {
            p = Pattern.compile(CommonUtils.TIMEREGEX2);
            m = p.matcher(t);
            if (!m.find()) {
                p = Pattern.compile(CommonUtils.TIMEREGEX3);
                m = p.matcher(t);
                if (!m.find())
                    return 0;
            }
        }
        secs += Integer.parseInt(m.group("secs"));
        if (m.group("mins") != null)
            secs += Integer.parseInt(m.group("mins")) * 60;
        if (m.group("hrs") != null)
            secs += Integer.parseInt(m.group("hrs")) * 60 * 60;
        if (m.group("days") != null)
            secs += Integer.parseInt(m.group("days")) * 24 * 60 * 60;
        return secs;
    }
    
    final static protected Vector<String> getMatches(String src, String regex, String group) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(src);
        Vector<String> matches = new Vector<>();
        while(m.find())
            matches.add(m.group(group));
        return matches;
    }
    
    final static protected String getId(String link, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(link);
        if (!m.find())
            return "";
        else if (m.group("id") != null && !m.group("id").isEmpty())
            return m.group("id");
        else if (m.group("id2") != null && !m.group("id2").isEmpty())
            return m.group("id2");
        else return m.group("id3");
    }
   
    final public String getId(String link) {
        return getId(link, getValidRegex());
    }
    
    final public String getId() {
        return getId(url, getValidRegex());
    }
    
    
    final public boolean suitable(String url) {
        return url == null ? false : url.matches(getValidRegex()) && working();
    }

    final protected static String addHost(String url, String host) {
        String real;
        if (url.startsWith("//"))
            real = "http:" + url;
        else if (url.startsWith("/"))
            real = "http://" + host + url;
        else if (!url.contains(host))
            real = "http://" + host + "/" + url;
        else real = url;
        return real;
    }
    
    final protected static String getDomain(String s) {
        Pattern p = Pattern.compile("https?://([^/]+)/[\\S]+");
        Matcher m = p.matcher(s);
        return !m.find() ? "" : m.group(1);
    }
    
    final protected static String configureUrl(String link) {
        if (link.startsWith("//"))
            return  "http:" + link;
        else if (!link.matches("https?://[\\S]+")) return "https://" + link;
        else
            return link;
    }
    
    final protected static String changeHttp(String link) {
        return link.replace("https", "http");
    }
}
