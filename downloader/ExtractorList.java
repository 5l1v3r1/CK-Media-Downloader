/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Exceptions.NotSupportedException;
import downloader.Extractors.Default;
import downloader.Extractors.GenericExtractor;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.UncheckedIOException;

/**
 *
 * @author christopher
 */

public class ExtractorList {
    static final String[] EXTRACTORS = {"Anysex", "Befuck", "Bigboobsalert", "Bigbootytube", "Bigtits", "Cumlouder", "Dailymotion",
        "Drtuber", "Eporner", "Ghettotube", "Gotporn", "Homemoviestube", "Hoodamateurs", "Imgur", "Instagram", "Justporno", "Myfreeblack",
        "Porn", "Pornhd", "Pornheed", "Pornhub", "Pornpics", "Redtube", "Ruleporn", "Shesfreaky", "Spankbang", "Spankwire",
        "Thumbzilla", "Tube8", "Vidoza", "Vimeo", "Vodlocker", "Vporn", "Watchenga", "Xhamster", "Xtube", "Xvideos", "Youjizz",
        "Youporn", "Yourporn"
    };
    
    private static List<GenericExtractor> getExtractors() throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<GenericExtractor> l = new ArrayList<>();
        for (String x : EXTRACTORS)
            l.add((GenericExtractor) Class.forName("downloader.Extractors." + x).getConstructor().newInstance());
        l.add(new Default()); //fall back on default 
        return l;
    }
    
    public static GenericExtractor getExtractor(String url) throws UncheckedIOException, GenericDownloaderException, Exception {
        try {
            List<GenericExtractor> list = getExtractors();
            for(GenericExtractor x :list)
                if (x.suitable(url)) {
                    int size = x.getClass().getName().split("[.]").length;
                    return (GenericExtractor) Class.forName("downloader.Extractors." +x.getClass().getName().split("[.]")[size-1]).getConstructor(String.class).newInstance(url);
                }
            throw new NotSupportedException("Url is not supported:",url);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            if (e.getCause() instanceof GenericDownloaderException) throw (GenericDownloaderException)e.getCause();
            CommonUtils.log(e.getMessage(),"ExtractorList:getExtractor(string)");
            return null;
        }
    }
    
    public static GenericExtractor getExtractor(String url, File thumb, String name) throws UncheckedIOException, GenericDownloaderException, Exception {
        try {
            List<GenericExtractor> list = getExtractors();
            for(GenericExtractor x :list)
                if (x.suitable(url)) {
                    int size = x.getClass().getName().split("[.]").length;
                    return (GenericExtractor) Class.forName("downloader.Extractors." +x.getClass().getName().split("[.]")[size-1]).getConstructor(String.class,File.class,String.class).newInstance(url,thumb,name);
                }
            throw new NotSupportedException("Url is not supported:",url);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            CommonUtils.log(e.getMessage(),"ExtractorList:getExtractor(string,file,string)");
            return null;
        }
    }
    
    public static boolean similar(String s, String s2) {
        GenericExtractor ex1 = null, ex2 = null;
        try {
            List<GenericExtractor> list = getExtractors();
            for(GenericExtractor x :list) {
                int size = x.getClass().getName().split("[.]").length;
                if (x.suitable(s)) {
                    ex1 = (GenericExtractor) Class.forName("downloader.Extractors." +x.getClass().getName().split("[.]")[size-1]).getConstructor().newInstance();
                    break;
                }
            }
            for(GenericExtractor x :list) {
                int size = x.getClass().getName().split("[.]").length;
                if (x.suitable(s2)) {
                    ex2 = (GenericExtractor) Class.forName("downloader.Extractors." +x.getClass().getName().split("[.]")[size-1]).getConstructor().newInstance();
                    break;
                }
            }
            if (ex1 == null || ex2 == null) {
                return false;
            } else if (ex1.getClass().getName().equals(ex2.getClass().getName())) {
                String id1 = ex1.getId(s).isEmpty() || ex1.getId(s) == null ? s : ex1.getId(s);
                String id2 = ex2.getId(s2).isEmpty() || ex2.getId(s2) == null ? s2 : ex2.getId(s2);
                return id1.equals(id2);
            } else {
                String obj1 = ex1.getClass().getName(), obj2 = ex2.getClass().getName();
                int size1 = obj1.split("[.]").length, size2 = obj2.split("[.]").length;
                String name1 = obj1.split("[.]")[size1-1], name2 = obj2.split("[.]")[size2-1];
                if ((name1.equals("Pornhub") && name2.equals("Thumbzilla")) || (name2.equals("Pornhub") && name1.equals("Thumbzilla"))) {
                    String id1 = ex1.getId(s).isEmpty() || ex1.getId(s) == null ? s : ex1.getId(s);
                    String id2 = ex2.getId(s2).isEmpty() || ex2.getId(s2) == null ? s2 : ex2.getId(s2);
                    return id1.equals(id2);
                } else return false;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            CommonUtils.log(e.getMessage(),"ExtractorList:similar");
            return false;
        }
    }
}
