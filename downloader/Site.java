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
    public static String[] QueryType = {Type.spankbang.name(), Type.pornhub.name(), Type.xhamster.name(), Type.xvideos.name(), Type.youporn.name(), Type.redtube.name(), Type.thumbzilla.name(), Type.shesfreaky.name(), Type.tube8.name(), Type.spankwire.name(), Type.bigbootytube.name(), Type.ruleporn.name()};
    public enum Page {none, instagram};
	
    public static Type getUrlSite(String url) {
        if ((!url.startsWith("https://")) && (!url.startsWith("http://"))) url = "https://" + url;
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
	    if ((url.matches("https://(www.)?pornhub.com/view_video.php[?]viewkey=[\\S]*")) || (url.matches("https://(www.)?pornhub.com/(photo|album|gif|playlist)/[\\S]*")))    
	    	return Type.pornhub;
		else if (url.matches("https://(www.)?instagram.com/[\\S]+/") || url.matches("https://(www.)?instagram.com/p/[\\S]+(/[?]taken-by=[\\S]*)?"))
	            return Type.instagram;
		else if ((url.matches("https://(((www)|m).)?xhamster.com/movies/[\\d]+/.html")) || (url.matches("https://([\\S]+.)?m.xhamster.com/videos/[\\S]+")) || (url.matches("https://([\\S]+.)?xhamster.com/videos/[\\S]+")))
	            return Type.xhamster; //this one done retarded ik
		else if (url.matches("https://((www|m).)?xhamster.com/videos/[\\S]+"))
	            return Type.xhamster;
		else if (url.matches("https://(((www)|([mt])).)?spankbang.com/[\\S]+/(video|playlist)/[\\S]+"))
	            return Type.spankbang;
		else if (url.matches("https://(www.)?xvideos.com/video[\\S]+/[\\S]+"))
	            return Type.xvideos;
		else if (url.matches("https://(www.)?redtube.com/[\\S]+"))
	            return Type.redtube;
		else if (url.matches("https://(www.)?thumbzilla.com/video/[\\S]+/[\\S]+"))
	            return Type.thumbzilla;
		else if (url.matches("https://(www.)?xnxx.com/video-[\\S]+/[\\S]+"))
            return Type.xnxx;//most likely parse the same as xvids
        else if (url.matches("https://(www.)?youporn.com/watch/[\\d]+/[\\S]+/"))
            return Type.youporn;
        else if ((url.matches("https://(www.)?shesfreaky.com/video/[\\S]+.html")))
            return Type.shesfreaky;
        else if ((url.matches("https://(www.)?yourporn.sexy/post/[\\S]+.html([?][\\S]*)?")) || (url.matches("https://pics.vc/watch[?]g=[\\S]+")))
            return Type.yourporn;
        else if ((url.matches("https://(www.)?bigtits.com/videos/watch/[\\S]+/[\\d]+")))
            return Type.bigtits;
        else if((url.matches("https://(www.)?pornhd.com/videos/[\\d]+/[\\S]+")))
            return Type.pornhd;
        else if((url.matches("https://(www.)?vporn.com/[\\S]+/[\\S]+/[\\d]+/")))
            return Type.vporn;
        else if ((url.matches("https://(www.)?ghettotube.com/video/[\\S]+.html")))
            return Type.ghettotube;
        else if ((url.matches("https://(www.)?tube8.com/[\\S]+/[\\S]+/[\\d]+/")))
            return Type.tube8;
        else if ((url.matches("https://(www.)?watcheng[a]?.tv/en/show/[\\S]+/(season-[\\d]+/episode-[\\d]+/)?")))
            return Type.watchenga;
        else if((url.matches("https://(www.)?youjizz.com/videos/[\\S]+.html")))
            return Type.youjizz;
        else if ((url.matches("https://(www.)?xtube.com/video-watch/[\\S]+")))
            return Type.xtube;
        else if ((url.matches("https://(www.)?spankwire.com/[\\S]+/video[\\d]+/([\\d]+)?")))
            return Type.spankwire;
        else if(url.matches("https://(www.)?vodlocker.nl/[\\S]+.html"))
            return Type.none;//return Type.vodlocker;
        else if (url.matches("https://(xxx.)?justporno.(tv|es)?/[\\S]+/[\\d]+/[\\S]+"))
            return Type.justporno;
        else if (url.matches("https://(www.)?bigbootytube.xxx/videos/[\\d]+/[\\S]+/"))
            return Type.bigbootytube;
        else if (url.matches("https://(www.)?befuck.com/videos/[\\d]+/[\\S]+"))
            return Type.befuck;
        else if (url.matches("https://(www.)?dailymotion.com/video/[\\S]+"))
            return Type.dailymotion;
        else if (url.matches("https://(www.)?vimeo.com/[\\d]+") || url.matches("https://player.vimeo.com/video/[\\d]+"))
            return Type.vimeo;
        else if (url.matches("https://(www.)?cumlouder.com/porn-video/[\\S]+[/]?"))
            return Type.cumlouder;
        else if (url.matches("https://(www.)?ruleporn.com/[\\S]+/"))
            return Type.ruleporn;
        else if (url.matches("https://(www.)?imgur.com/gallery/[\\S]+") || url.matches("https://(www.)?imgur.com/[\\S]+"))
            return Type.imgur;
        else if (url.matches("https://(www.)?pornpics.com/galleries/[\\S]+/"))
            return Type.pornpics;
        else if(url.matches("https://(www.)?bigboobsalert.com/[\\S]+[.]php"))
            //return Type.bigboobsalert;
            return Type.none;
        else if(url.matches("https://(www.)?eporner.com/hd-porn/[\\S]+/[\\S]+/"))
                return Type.eporner;
        else if(url.matches("https://(www.)?pornheed.com/video/[\\d]+/[\\S]+"))
            return Type.pornheed;
        else if(url.matches("https://(www.)?homemoviestube.com/videos/[\\d]+/[\\S]+.html"))
            return Type.homemoviestube;
        else if(url.matches("https://(www.)?anysex.com/[\\d]+/"))
            return Type.anysex;
        else if(url.matches("https://(www.)?porn.com/videos/([\\S]+[-])+[\\d]+"))
            return Type.porn;
        else if(url.matches("https://(www.)?gotporn.com/[\\S]+/video-[\\d]+"))
            return Type.none; //return Type.gotporn;
        else if (url.matches("https://(?:(www|m).)?drtuber.com/video/([\\d]+)/[\\S]+"))
            return Type.drtuber;
        else if (url.matches("https://(www.)?myfreeblack.com/porn/([\\d]+)(/[\\S]+)?"))
            return Type.myfreeblack;
        else if (url.matches("https://(www.)?vidoza.net/[\\S]+.html"))
            return Type.vidoza;
        else if (url.matches("https://(www.)?hoodamateurs.com/([\\d]+)(/[\\S]+)?/?"))
            return Type.hoodamateurs;
        else return Type.none;
    }
    
    public static Page getPageSite(String page) {
        if (page.contains("<link rel=\"canonical\" href=\"https://www.instagram.com/"))
            return Page.instagram;
        else return Page.none;
    }
}