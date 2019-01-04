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
    public enum Type {none, spankbang, pornhub, xhamster, xvideos, xnxx, youporn, redtube, thumbzilla, shesfreaky, instagram, yourporn, bigtits, pornhd, vporn, ghettotube, tube8, watchenga, youjizz, xtube, spankwire, vodlocker, justporno, bigbootytube, befuck, dailymotion, vimeo, cumlouder, ruleporn};
    public static String[] QueryType = {Type.spankbang.name(), Type.pornhub.name(), Type.xhamster.name(), Type.xvideos.name(), Type.youporn.name(), Type.redtube.name(), Type.thumbzilla.name(), Type.shesfreaky.name(), Type.tube8.name(), Type.spankwire.name(), Type.bigbootytube.name(), Type.ruleporn.name()};
	
    public static Type getUrlSite(String url) {
        if ((!url.startsWith("https://")) && (!url.startsWith("http://"))) url = "https://" + url;
        if (url.startsWith("http://")) url = url.replace("http://", "https://"); //so it doesnt matter if link is http / https
	if (url.matches("https://www.pornhub.com/view_video.php[?]viewkey=[\\S]*") || (url.matches("https://pornhub.com/view_video.php[?]viewkey=[\\S]*")) || (url.matches("https://www.pornhub.com/album/[\\S]*")) || (url.matches("https://pornhub.com/album/[\\S]*")) || (url.matches("https://www.pornhub.com/photo/[\\S]*")) || (url.matches("https://pornhub.com/photo/[\\S]*")) || (url.matches("https://www.pornhub.com/gif/[\\S]*")) || (url.matches("https://pornhub.com/gif/[\\S]*")))
            return Type.pornhub;
	else if (url.matches("https://www.instagram.com/[\\S]*/") || (url.matches("https://www.instagram.com/[\\S]*")))
            return Type.instagram;
	else if (url.matches("https://www.instagram.com/p/[\\S]*/[?]taken-by=[\\S]*") || url.matches("https://www.instagram.com/p/[\\S]*")) 
            return Type.instagram;
	else if ((url.matches("https://m.xhamster.com/videos/[\\S]*")) || (url.matches("https://xhamster.com/movies/[\\d]*/.html")) || (url.matches("https://www.xhamster.com/movies/[\\d]*/.html")) || (url.matches("https://[\\S]*.m.xhamster.com/videos/[\\S]*")) || (url.matches("https://[\\S]*.xhamster.com/videos/[\\S]*")))
            return Type.xhamster;
	else if (url.matches("https://www.xhamster.com/videos/[\\S]*") || (url.matches("https://xhamster.com/videos/[\\S]*")))
            return Type.xhamster;
	else if (url.matches("https://spankbang.com/[\\S]*/video/[\\S]*") || url.matches("https://www.spankbang.com/[\\S]*/video/[\\S]*") || url.matches("https://m.spankbang.com/[\\S]*/video/[\\S]*") || url.matches("https://t.spankbang.com/[\\S]*/video/[\\S]*"))
            return Type.spankbang;
	else if (url.matches("https://www.xvideos.com/video[\\S]*/[\\S]*") || (url.matches("https://xvideos.com/video[\\S]*/[\\S]*")))
            return Type.xvideos;
	else if (url.matches("https://www.redtube.com/[\\S]*") || (url.matches("https://redtube.com/[\\S]*")))
            return Type.redtube;
	else if (url.matches("https://www.thumbzilla.com/video/[\\S]*/[\\S]*") || ((url.matches("https://thumbzilla.com/video/[\\S]*/[\\S]*"))))
            return Type.thumbzilla;
	else if (url.matches("https://www.xnxx.com/video-[\\S]*/[\\S]*") || (url.matches("https://xnxx.com/video-[\\S]*/[\\S]*")))
            return Type.xnxx;//most likely parse the same as xvids
        else if (url.matches("https://www.youporn.com/watch/[\\d]*/[\\S]*/") || (url.matches("https://youporn.com/watch/[\\d]*/[\\S]*/")))
            return Type.youporn;
        else if ((url.matches("https://www.shesfreaky.com/video/[\\S]*.html")) || (url.matches("https://www.shesfreaky.com/video/[\\S]*.html")))
            return Type.shesfreaky;
        else if ((url.matches("https://www.yourporn.sexy/post/[\\S]*.html")) || (url.matches("https://www.yourporn.sexy/post/[\\S]*.html[\\S]*")) || (url.matches("https://yourporn.sexy/post/[\\S]*.html")) || (url.matches("https://yourporn.sexy/post/[\\S]*.html[\\S]*")))
            return Type.yourporn;
        else if ((url.matches("https://www.bigtits.com/videos/watch/[\\S]*/[\\d]*")) || (url.matches("https://bigtits.com/videos/watch/[\\S]*/[\\d]*")))
            return Type.bigtits;
        else if((url.matches("https://www.pornhd.com/videos/[\\d]*/[\\S]*")) || (url.matches("https://pornhd.com/videos/[\\d]*/[\\S]*")))
            return Type.pornhd;
        else if((url.matches("https://www.vporn.com/[\\S]*/[\\S]*/[\\d]*/")) || (url.matches("https://vporn.com/[\\S]*/[\\S]*/[\\d]*/")))
            return Type.vporn;
        else if ((url.matches("https://www.ghettotube.com/video/[\\S]*.html")) || (url.matches("https://ghettotube.com/video/[\\S]*.html")))
            return Type.ghettotube;
        else if ((url.matches("https://www.tube8.com/[\\S]*/[\\S]*/[\\d]*/")) || (url.matches("https://tube8.com/[\\S]*/[\\S]*/[\\d]*/")))
            return Type.tube8;
        else if ((url.matches("https://www.watchenga.tv/en/show/[\\S]*/")) || (url.matches("https://watchenga.tv/en/show/[\\S]*/")) || (url.matches("https://www.watchenga.tv/en/show/[\\S]*/season-[\\d]*/episode-[\\d]*/")) || (url.matches("https://www.watchenga.tv/en/show/[\\S]*/season-[\\d]*/episode-[\\d]*/")) || (url.matches("https://www.watcheng.tv/en/show/[\\S]*/")) || (url.matches("https://watcheng.tv/en/show/[\\S]*/")) || (url.matches("https://www.watcheng.tv/en/show/[\\S]*/season-[\\d]*/episode-[\\d]*/")) || (url.matches("https://www.watcheng.tv/en/show/[\\S]*/season-[\\d]*/episode-[\\d]*/")))
            return Type.watchenga;
        else if((url.matches("https://www.youjizz.com/videos/[\\S]*.html")) || (url.matches("https://youjizz.com/videos/[\\S]*.html")))
            return Type.youjizz;
        else if ((url.matches("https://www.xtube.com/video-watch/[\\S]*")) || (url.matches("https://xtube.com/video-watch/[\\S]*")))
            return Type.xtube;
        else if ((url.matches("https://www.spankwire.com/[\\S]*/video[\\d]*/[\\d]*")) || (url.matches("https://spankwire.com/[\\S]*/video[\\d]*/[\\d]*")))
            return Type.spankwire;
        else if(url.matches("https://vodlocker.nl/[\\S]*.html") || url.matches("https://www.vodlocker.nl/[\\S]*.html"))
            return Type.none;//return Type.vodlocker;
        else if (url.matches("https://xxx.justporno.tv/[\\S]*/[\\d]*/[\\S]*") || url.matches("https://justporno.tv/[\\S]*/[\\d]*/[\\S]*") || url.matches("https://xxx.justporno.es/[\\S]*/[\\d]*/[\\S]*") || url.matches("https://justporno.es/[\\S]*/[\\d]*/[\\S]*"))
            return Type.justporno;
        else if (url.matches("https://www.bigbootytube.xxx/videos/[\\d]*/[\\S]*/") || url.matches("https://bigbootytube.xxx/videos/[\\d]*/[\\S]*/"))
            return Type.bigbootytube;
        else if (url.matches("https://befuck.com/videos/[\\d]*/[\\S]*") || url.matches("https://www.befuck.com/videos/[\\d]*/[\\S]*"))
            return Type.befuck;
        else if (url.matches("https://dailymotion.com/video/[\\S]*") || url.matches("https://www.dailymotion.com/video/[\\S]*"))
            return Type.dailymotion;
        else if (url.matches("https://vimeo.com/[\\d]*") || url.matches("https://www.vimeo.com/[\\d]*") || url.matches("https://player.vimeo.com/video/[\\d]*"))
            return Type.vimeo;
        else if ((url.matches("https://cumlouder.com/porn-video/[\\S]*/") || url.matches("https://www.cumlouder.com/porn-video/[\\S]*/")) || (url.matches("https://cumlouder.com/porn-video/[\\S]*") || url.matches("https://www.cumlouder.com/porn-video/[\\S]*")))
            return Type.cumlouder;
        else if ((url.matches("https://ruleporn.com/[\\S]*/") || url.matches("https://www.ruleporn.com/[\\S]*/")))
            return Type.ruleporn;
	else return Type.none;
    }
}