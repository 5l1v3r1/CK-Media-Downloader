/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;
import downloader.Exceptions.GenericDownloaderException;
import downloader.Extractors.*;
import downloader.Site.Type;
import java.io.File;
import java.net.MalformedURLException;
import org.jsoup.UncheckedIOException;
import org.jsoup.nodes.Document;

/**
 *
 * @author christopher
 */

public class ExtractorList {
    public static GenericExtractor getExtractor(Type type, String url) throws UncheckedIOException, GenericDownloaderException, Exception {
        switch (type) {
            case spankbang: return new Spankbang(url);
            case pornhub: return new Pornhub(url);
            case xhamster: return new Xhamster(url); 
            case xvideos: return new Xvideos(url);
            case xnxx: return new Xvideos(url); //xnxx shares the same setup so they use the extractor
            case youporn: return new Youporn(url);
            case redtube: return new Redtube(url);
            case thumbzilla: return new Thumbzilla(url);
            case shesfreaky: return new Shesfreaky(url);
            case instagram: return new Instagram(url);
            case yourporn: return new Yourporn(url);
            case bigtits: return new Bigtits(url);
            case pornhd: return new Pornhd(url);
            case vporn: return new Vporn(url);
            case ghettotube: return new Ghettotube(url);
            case tube8: return new Tube8(url);
            case watchenga: return new Watchenga(url);
            case youjizz: return new Youjizz(url);
            case xtube: return new Xtube(url);
            case spankwire: return new Spankwire(url);
            case justporno: return new Justporno(url);
            case bigbootytube:return new Bigbootytube(url);
            case befuck: return new Befuck(url);
            case dailymotion: return new Dailymotion(url);
            case vimeo: return new Vimeo(url);
            case cumlouder:return new Cumlouder(url);
            case ruleporn: return new Ruleporn(url);
            case imgur: return new Imgur(url);
            case pornpics: return new Pornpics(url);
            case bigboobsalert: return new Bigboobsalert(url);
            case eporner: return new Eporner(url);
            case pornheed: return new Pornheed(url);
            case homemoviestube: return new Homemoviestube(url);
            case anysex: return new Anysex(url);
            case porn: return new Porn(url);
            case gotporn: return new Gotporn(url);
            case drtuber: return new Drtuber(url);
            case myfreeblack: return new Myfreeblack(url);
            case vidoza: return new Vidoza(url);
            case hoodamateurs: return new Hoodamateurs(url);
            default:
                return null;
        }
    }
    
    public static GenericExtractor getExtractor(Type type, String url, File thumb, String name) throws UncheckedIOException, GenericDownloaderException, Exception {
        switch (type) {
            case spankbang: return new Spankbang(url,thumb,name);
            case pornhub: return new Pornhub(url,thumb,name);
            case xhamster: return new Xhamster(url,thumb,name);
            case xvideos: return new Xvideos(url,thumb,name);
            case xnxx: return new Xvideos(url,thumb,name);
            case youporn: return new Youporn(url,thumb,name);
            case redtube:  return new Redtube(url,thumb,name);
            case thumbzilla: return new Thumbzilla(url,thumb,name);
            case shesfreaky: return new Shesfreaky(url,thumb,name);
            case instagram: return new Instagram(url,thumb,name);
            case yourporn: return new Yourporn(url,thumb,name);
            case bigtits: return new Bigtits(url,thumb,name);
            case pornhd: return new Pornhd(url,thumb,name);
            case vporn: return new Vporn(url,thumb,name);
            case ghettotube: return new Ghettotube(url,thumb,name);
            case tube8: return new Tube8(url,thumb,name);
            case watchenga: return new Watchenga(url,thumb,name);
            case youjizz: return new Youjizz(url,thumb,name);
            case xtube: return new Xtube(url,thumb,name);
            case spankwire: return new Spankwire(url,thumb,name);
            case justporno: return new Justporno(url,thumb,name);
            case bigbootytube: return new Bigbootytube(url,thumb,name);
            case befuck: return new Befuck(url,thumb,name);
            case dailymotion: return new Dailymotion(url,thumb,name);
            case vimeo: return new Vimeo(url,thumb,name);
            case cumlouder: return new Cumlouder(url,thumb,name);
            case ruleporn: return new Ruleporn(url,thumb,name);
            case imgur: return new Imgur(url,thumb,name);
            case pornpics: return new Pornpics(url,thumb,name);
            case bigboobsalert: return new Bigboobsalert(url,thumb,name);
            case eporner: return new Eporner(url,thumb,name);
            case pornheed: return new Pornheed(url,thumb,name);
            case homemoviestube: return new Homemoviestube(url,thumb,name);
            case anysex: return new Anysex(url,thumb,name);
            case porn: return new Porn(url,thumb,name);
            case gotporn: return new Gotporn(url,thumb,name);
            case drtuber: return new Drtuber(url,thumb,name);
            case myfreeblack: return new Myfreeblack(url,thumb,name);
            case vidoza: return new Vidoza(url,thumb,name);
            case hoodamateurs: return new Hoodamateurs(url,thumb,name);
            default:
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
            switch(Site.getUrlSite(s)) {
                case spankbang: return new Spankbang().getId(s).equals(new Spankbang().getId(s2));
                case pornhub: return new Pornhub().getId(s).equals(new Pornhub().getId(s2));
                case xhamster: return new Xhamster().getId(s).equals(new Xhamster().getId(s2));
                case xvideos: return new Xvideos().getId(s).equals(new Xvideos().getId(s2));
                case xnxx: return new Xvideos().getId(s).equals(new Xvideos().getId(s2));
                case youporn: return new Youporn().getId(s).equals(new Youporn().getId(s2));
                case redtube: return new Redtube().getId(s).equals(new Redtube().getId(s2));
                case thumbzilla: return new Thumbzilla().getId(s).equals(new Thumbzilla().getId(s2));
                case shesfreaky: return new Shesfreaky().getId(s).equals(new Shesfreaky().getId(s2));
                case instagram: return new Instagram().getId(s).equals(new Instagram().getId(s2));
                case yourporn: return new Yourporn().getId(s).equals(new Yourporn().getId(s2));
                case bigtits: return new Bigtits().getId(s).equals(new Bigtits().getId(s2));
                case pornhd: return new Pornhd().getId(s).equals(new Pornhd().getId(s2));
                case vporn: return new Vporn().getId(s).equals(new Vporn().getId(s2));
                case ghettotube: return new Ghettotube().getId(s).equals(new Ghettotube().getId(s2));
                case tube8: return new Tube8().getId(s).equals(new Tube8().getId(s2));
                case watchenga: return new Watchenga().getId(s).equals(new Watchenga().getId(s2));
                case youjizz: return new Youjizz().getId(s).equals(new Youjizz().getId(s2));
                case xtube: return new Xtube().getId(s).equals(new Xtube().getId(s2));
                case spankwire: return new Spankwire().getId(s).equals(new Spankwire().getId(s2));
                case justporno: return new Justporno().getId(s).equals(new Justporno().getId(s2));
                case bigbootytube: return new Bigbootytube().getId(s).equals(new Bigbootytube().getId(s2));
                case befuck: return new Befuck().getId(s).equals(new Befuck().getId(s2));
                case dailymotion: return new Dailymotion().getId(s).equals(new Dailymotion().getId(s2));
                case vimeo: return new Vimeo().getId(s).equals(new Vimeo().getId(s2));
                case cumlouder: return new Cumlouder().getId(s).equals(new Cumlouder().getId(s2));
                case ruleporn: return new Ruleporn().getId(s).equals(new Ruleporn().getId(s2));
                case imgur: return new Imgur().getId(s).equals(new Imgur().getId(s2));
                case pornpics: return new Pornpics().getId(s).equals(new Pornpics().getId(s2));
                case bigboobsalert: return new Bigboobsalert().getId(s).equals(new Bigboobsalert().getId(s2));
                case eporner: return new Eporner().getId(s).equals(new Eporner().getId(s2));
                case pornheed: return new Pornheed().getId(s).equals(new Pornheed().getId(s2));
                case homemoviestube: return new Homemoviestube().getId(s).equals(new Homemoviestube().getId(s2));
                case anysex: return new Anysex().getId(s).equals(new Anysex().getId(s2));
                case porn: return new Porn().getId(s).equals(new Porn().getId(s2));
                case gotporn: return new Gotporn().getId(s).equals(new Gotporn().getId(s2));
                case drtuber: return new Drtuber().getId(s).equals(new Drtuber().getId(s2));
                case myfreeblack: return new Myfreeblack().getId(s).equals(new Myfreeblack().getId(s2));
                case vidoza: return new Vidoza().getId(s).equals(new Vidoza().getId(s2));
                case hoodamateurs: return new Hoodamateurs().getId(s).equals(new Hoodamateurs().getId(s2));
                default: return false;
            }
        else return false;
    }
}
