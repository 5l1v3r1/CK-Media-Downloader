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
}
