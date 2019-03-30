/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.GenericExtractor;
import downloader.Extractors.Instagram;
import downloader.Site.Type;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */

public class ExtractorList {
    public static GenericExtractor getExtractor(Type type, String url) throws UncheckedIOException, GenericDownloaderException, Exception {
        try {
            Class<?> c = Class.forName("downloader.Extractors."+type.toString().substring(0,1).toUpperCase()+type.toString().substring(1));
            Constructor<?> cons = c.getConstructor(String.class);
            return (GenericExtractor)cons.newInstance(url);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            CommonUtils.log(e.getMessage(),"ExtractorList:getExtractor(type,string)");
            return null;
        }
    }
    
    public static GenericExtractor getExtractor(Type type, String url, File thumb, String name) throws UncheckedIOException, GenericDownloaderException, Exception {
        try {
            Class<?> c = Class.forName("downloader.Extractors."+type.toString().substring(0,1).toUpperCase()+type.toString().substring(1));
            Constructor<?> cons = c.getConstructor(String.class,File.class,String.class);
            return (GenericExtractor)cons.newInstance(url,thumb,name);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            CommonUtils.log(e.getMessage(),"ExtractorList:getExtractor(type,string,file,string)");
            return null;
        }
    }
    
    public static GenericExtractor getExtractor(Site.Page type, Document page) throws MalformedURLException {
        switch (type) {
            case instagram: return new Instagram(page);
            default:
                return null;
        }
    }
    
    public static boolean similar(String s, String s2) {
        if ((!s.startsWith("https://")) && (!s.startsWith("http://"))) s = "https://" + s;
        if (s.startsWith("http://")) s = s.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if ((!s2.startsWith("https://")) && (!s2.startsWith("http://"))) s2 = "https://" + s2;
        if (s2.startsWith("http://")) s2 = s2.replace("http://", "https://"); //so it doesnt matter if link is http / https
        if(Site.getUrlSite(s) == Site.getUrlSite(s2))
            try {
                Type t = Site.getUrlSite(s);
                Class<?> c = Class.forName("downloader.Extractors."+t.toString().substring(0,1).toUpperCase()+t.toString().substring(1));
                Constructor<?> cons = c.getConstructor();
                GenericExtractor x = (GenericExtractor)cons.newInstance();
                return x.getId(s).equals(x.getId(s2));
            } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                CommonUtils.log(e.getMessage(),"ExtractorList:similar");
                return false;
            }
        else return false;
    }
}
