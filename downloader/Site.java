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
    public static String[] QueryType = {"Spankbang", "Pornhub", "Xhamster", "Xvideos", "Youporn", "Redtube", "Thumbzilla", "Shesfreaky", "Tube8", "Spankwire", "Bigbootytube", "Ruleporn"};
    public enum Page {none, instagram};
    
    public static Page getPageSite(String page) {
        if (page.contains("<link rel=\"canonical\" href=\"https://www.instagram.com/"))
            return Page.instagram;
        else return Page.none;
    }
}