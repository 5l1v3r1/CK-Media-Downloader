/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

/**
 *
 * @author christopher
 */

public class Site {
    public enum Type {none, spankbang, pornhub, xhamster, xvideos, xnxx, youporn, redtube, thumbzilla, shesfreaky, instagram, yourporn, bigtits, pornhd, vporn, ghettotube, tube8, watchenga, youjizz, xtube, spankwire, vodlocker, justporno, bigbootytube, befuck, dailymotion, vimeo, cumlouder, ruleporn, imgur, pornpics, bigboobsalert, eporner, pornheed, homemoviestube, anysex, porn, gotporn, drtuber, myfreeblack, vidoza, hoodamateurs};
    public static String[] QueryType = {"Spankbang", "Pornhub", "Xhamster", "Xvideos", "Youporn", "Redtube", "Thumbzilla", "Shesfreaky", "Tube8", "Spankwire", "Bigbootytube", "Ruleporn"};
    public enum Page {none, instagram};
    
    public static Page getPageSite(String page) {
        if (page.contains("<link rel=\"canonical\" href=\"https://www.instagram.com/"))
            return Page.instagram;
        else return Page.none;
    }
}