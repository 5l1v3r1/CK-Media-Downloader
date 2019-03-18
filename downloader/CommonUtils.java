/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import ChrisPackage.GameTime;
import ChrisPackage.stopWatch;
import downloaderProject.DataIO;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.jsoup.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Scanner;
import java.util.Vector;
import javax.imageio.ImageIO;
import org.jsoup.Jsoup;

/**
 *
 * @author christopher
 */
public class CommonUtils {
    public static final String PCCLIENT = "Mozilla/5.0 (X11; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0", 
    MOBILECLIENT = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.57 Mobile Safari/537.36";
    static final int BUFFSIZE = 1024 * 600; //1 x 600kb
    
    public static Vector<File> splitImage(File origin, int row, int col, int yOffset, int widthOffset) {
        Vector<File> splits = new Vector<>();
        
        try {
            BufferedImage originImage = ImageIO.read(origin);
            int width = originImage.getWidth(), height = originImage.getHeight(), y, x = 0;
            int eWidth = width / col, eHeight = height / row;
            
            for(int i = 0; i < row; i++) {
                y = 0;
                for(int j = 0; j < col; j++) {
                    BufferedImage subImg = originImage.getSubimage(y+yOffset, x, eWidth-widthOffset, eHeight);
                    File save = new File(MainApp.imageCache.getAbsolutePath()+File.separator+origin.getName()+String.valueOf(i)+String.valueOf(j)+".jpg");
                    ImageIO.write(subImg,"jpg",save); 
                    splits.add(save);
                    y+=eWidth;
                }
                x+=eHeight;
            }
        } catch (IOException e) {
            System.out.println("Error splitting");
        }
        return splits;
    }
    
    public static String getShortName(String s) {
        StringBuilder pure = new StringBuilder();
        
        for(int i = 0; i < s.length()/2; i++)
            pure.append(s.charAt(i));
        return pure.toString();
    }
    
    public static String clean(String s) {
        StringBuilder pure = new StringBuilder();
        for(int i = 0; i < s.length(); i++) {
            if (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == ' ' || s.charAt(i) == '.' || s.charAt(i) == '_' || s.charAt(i) == '-' || s.charAt(i) == '(' || s.charAt(i) == ')')
                pure.append(s.charAt(i));
        }
        return pure.toString();
    }
    
    public static String getPureDigit(String s) {
        StringBuilder pure = new StringBuilder();
        for(int i = 0; i < s.length(); i++)
            if (Character.isDigit(s.charAt(i)))
                pure.append(s.charAt(i));
        return pure.toString();
    }
    
    public static boolean hasExtension(String name, String exe) {
        return name.endsWith(exe);
    }
    
    public static boolean isImage(String name) {
        return (hasExtension(name,"gif") || hasExtension(name,"png") || hasExtension(name,"jpg"));
    }
    
    public static String getPicName(String link) {
        return getPicName(link,0);
    }
    
    public static String getPicName(String link, int skip) {
        StringBuilder name = new StringBuilder();
        
        for(int i = link.length()-1; i > 0; i--) {
            if (link.charAt(i) == ')') {
                if (skip-- <= 0)
                    break;
                else continue;
            } else if (link.charAt(i) == '/') {
                if (skip-- <= 0)
                    break;
                else continue;
            }
            name.append(link.charAt(i));
        }
        return name.reverse().toString();
    }
    
    public static String getThumbName(String link, int skip) {
        StringBuilder name = new StringBuilder();
        
        for(int i = link.length()-1; i > 0; i--) {
            if (link.charAt(i) == '/') {
                if (skip-- == 0)
                    break;
                else continue;
            }
            name.append(link.charAt(i));
        }
        return name.reverse().toString();
    }
    
    public static String getThumbName(String link) {
	String[] tokens = link.split("/");
	return tokens[tokens.length-1];
    }
    
    public static String parseName(String link, String filetype) {
	StringBuilder picName = new StringBuilder();
		
	for(int i = link.indexOf(filetype) + 3; i > -1; i--) {
		if (link.charAt(i) == '/') break;
        	picName.append(link.charAt(i));
	}
	picName.reverse();
	return picName.toString();
    }
    
    public static String getLink(String src, int where, char terminate) {
	StringBuilder pure = new StringBuilder();
		
	for(int i = where; src.charAt(i) != terminate && i < src.length(); i++)
            pure.append(src.charAt(i));
	return pure.toString();
    }
    
    public static int getThumbs(String src, int where, char terminate) {
	StringBuilder pure = new StringBuilder();
		
	for(int i = where; src.charAt(i) != terminate; i++)
            if (Character.isDigit(src.charAt(i)))
		pure.append(src.charAt(i));
	return Integer.parseInt(pure.toString());
    }
	
    public static String replaceIndex(String src, int index, String in) {
	StringBuilder pure = new StringBuilder();
		
	for(int i = 0; i < src.length(); i++) {
            if (src.charAt(i) == '{') {
		pure.append(index);
		i+=in.length()+1;
            } else pure.append(src.charAt(i));
	}
	return pure.toString();
    }
    
    public static String getUrl(String src, int where) {
        StringBuilder pure = new StringBuilder();
        
        where = src.indexOf("videoUrl",where) + 11;
        for(int i = where; i < src.length(); i++) {
            if (src.charAt(i) == '\"')
                break;
            pure.append(src.charAt(i));
        }
        return pure.toString();
    }
    
    public static String getBracket(String src, int from) {
        return getBracket(src, from, 0);
    }
	
    public static String getBracket(String src, int from, int skip) {
	boolean start = false;
	StringBuilder pure = new StringBuilder();
		
	for(int i = from; i < src.length(); i++) {
            if (start) {
		pure.append(src.charAt(i));
            if ((src.charAt(i) == '}') && (skip-- == 0))
		break;
            } else if (src.charAt(i) == '{') {
                pure.append(src.charAt(i)); start = true;
            }
	}
	return pure.toString();
    }
    
    public static String getSBracket(String src, int from) {
        boolean start = false;
        StringBuilder pure = new StringBuilder();
        
        for(int i = from; i < src.length(); i++) {
            if(start) {
                pure.append(src.charAt(i));
                if(src.charAt(i) == ']')
                    break;
            } else if (src.charAt(i) == '[') {
                pure.append(src.charAt(i)); start = true;
            }
        }
        return pure.toString();
    }
    
    public static String eraseChar(String src, char erase) {
	StringBuilder pure = new StringBuilder();
		
	for(int i = 0; i < src.length(); i++)
            if (src.charAt(i) != erase)
		pure.append(src.charAt(i));
        return pure.toString();
    }
    
    public static String changeIndex(String link, int index) {
        StringBuilder pure = new StringBuilder();
        
        int lastBrac = link.lastIndexOf(')');
        for(int i = 0; i < lastBrac + 1; i++)
            pure.append(link.charAt(i));
        pure.append(index);
        pure.append(".jpg");
        return pure.toString();
    }
    
    public static int getFormatWeight(String format) {
        StringBuilder s = new StringBuilder();
        if(format.equalsIgnoreCase("4k"))
            return 2160;
        else 
            for(int i = 0; i < format.length(); i++) 
                if (Character.isDigit(format.charAt(i)))
                    s.append(format.charAt(i));
                else break;
        return Integer.parseInt(s.toString());
    }
    
    public static String addAttr(String attr, String value) {
        return "\""+attr+"\":\""+value+"\"";
    }
    
    public static String addAttr(String attr, int value) {
        return addAttr(attr,String.valueOf(value));
    }
    
    public static String addAttr(String attr, long value) {
        return addAttr(attr,String.valueOf(value));
    }
    
    public static String addAttr(String attr, Object value) {
        return addAttr(attr,value instanceof String ? (String)value : value.toString());
    }
    
    public static boolean checkImageCache(String name) {
        File[] files = MainApp.imageCache.listFiles();
        if (files != null)  {//if cache is not empty
            Arrays.parallelSort(files);
            if (Arrays.binarySearch(files, new File(MainApp.imageCache+File.separator+name)) > -1) {
                System.out.println("Found in cache: "+name);
                return true;
            }
        }
        System.out.println("not found in cache: "+name);
        return false;
    }
    
    public static boolean checkPageCache(String name) {
        File[] files = MainApp.pageCache.listFiles();
        if (files != null)  {//if cache is not empty
            Arrays.parallelSort(files);
            if (Arrays.binarySearch(files, new File(MainApp.pageCache+File.separator+name)) > -1) {
                System.out.println("Found in cache: "+name);
                return true;
            }
        }
        System.out.println("not found in cache: "+name);
        return false;
    }
    
    private static long checkProgress(String url) {
        File progressFile = new File(MainApp.progressCache.getAbsolutePath()+File.separator+getShortName(clean(url)));
        if (!progressFile.exists()) return 0;
        else {
            try {
                return DataIO.readProgress(progressFile);
            }catch (FileNotFoundException ex) {
                System.out.println("Failed to load progress");
                return 0;
            } catch(IOException e) {
                System.out.println("Failed to load progress");
                return 0;
            } catch (ClassNotFoundException ex) {
                System.out.println("Build error");
                return 0;
            }
        }
    }
    
    private static void saveProgress(String url, long bytes) {
        File progressFile = new File(MainApp.progressCache.getAbsolutePath()+File.separator+getShortName(clean(url)));
        try {
            DataIO.writeProgress(progressFile, url, bytes);
        } catch (IOException ex) {
            System.out.println("Failed to save progress");
        }
    }
    
    //get cache name from url
    public static String getCacheName(String url, boolean mobile) {
        url = url.replace("/", ".");
        url = url.replace(":", ".");
        if (mobile)
            return url+".mobile";
        else return url+".pc";
    }
    
    public static long getContentSize(String url) {
        if (url == null) return -5;
        try {
            URLConnection connection = new URL(url).openConnection();
            /*String temp = url.split("/")[url.split("/").length-1];
            String pure = temp.substring(temp.indexOf(".")).replace(".","");
            System.out.println("url "+pure);
            connection.addRequestProperty("Cookie", "amplitude_idpornhd.com:="+pure);
            connection.setRequestProperty("User-Agent", PCCLIENT);
            connection.connect();
            int response = ((HttpURLConnection)connection).getResponseCode();
            System.out.println("response "+response);*/
            return connection.getContentLengthLong();
        } catch (SocketException e){
            System.out.println(e.getMessage());
            return -2;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return -3;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return -4;
        }
    }
    
    public static boolean testPage(String url) throws IOException, SocketTimeoutException, UncheckedIOException { //test to avoid error 404
        try {
            Jsoup.connect(url).get().html();
        } catch (org.jsoup.HttpStatusException e) {
            return false;//HTTP error fetching URL
        }
        return true;
    }
    
    public static String loadPage(String name) throws FileNotFoundException{
        return loadPage(new File(name));
    }
    
    public static String loadPage(File pageFile) throws FileNotFoundException{
        Scanner reader = new Scanner(pageFile);
        StringBuilder page = new StringBuilder();
        
        while(reader.hasNextLine())
            page.append("\n"+reader.nextLine());
        reader.close();
        return page.toString();
    }
    
    public static void savePage(String page, String url, boolean mobile) {
        try {
            File save = new File(MainApp.pageCache.getAbsolutePath()+File.separator+getCacheName(url,mobile));
            
            Formatter f = new Formatter(save);
            f.format("%s", page);
            f.flush(); f.close();
        } catch (FileNotFoundException ex) {
            System.out.println("Failed to save page: "+ex.getMessage());
        }
        MainApp.settings.cacheUpdate();
    }
    
    public static void erasePage(String name) {
        System.out.println(new File(MainApp.pageCache.getAbsolutePath()+File.separator+name).delete());
    }
    
    public static long saveFile(String link, String saveName, File path) throws MalformedURLException{
        return saveFile(link,saveName,path.getAbsolutePath());
    }
    
    public static long saveFile(String link, String saveName, File path, OperationStream s) throws MalformedURLException{
        return saveFile(link, saveName, path.getAbsolutePath(), s);
    }
    
    public static long saveFile(String link, String saveName, String path) throws MalformedURLException{
        return saveFile(link, saveName, path, null);
    }
    
    public static long saveFile(String link, String saveName, String path, OperationStream s) throws MalformedURLException{
	BufferedInputStream in = null;
	FileOutputStream out = null;
        File dir = new File(path);
        long how = 0;
        if (!dir.exists()) 
            dir.mkdirs();
	
	try { //https://spankbang.com/2lidc/video/slutty+latin+girl+showing+nice+tits+in+cam+chat
            URLConnection connection = new URL(link).openConnection();
            File save = new File(path+File.separator+saveName);
            how = save.exists() ? save.length() : 0;
            connection.setRequestProperty("User-Agent", PCCLIENT);
            connection.setRequestProperty("Range","bytes="+how+"-");
            int response = ((HttpURLConnection)connection).getResponseCode();
            //if redirect
            if ((response == HttpURLConnection.HTTP_SEE_OTHER) || (response == HttpURLConnection.HTTP_MOVED_TEMP) || (response == HttpURLConnection.HTTP_MOVED_PERM)) {
                String location = connection.getHeaderField("Location");
                if (location.startsWith("/")) 
                    location = "https://"+location;
                connection = new URL(location).openConnection();
                String cookies = connection.getHeaderField("Set-Cookie");
                connection.setRequestProperty("Cookie", cookies);
                connection.setRequestProperty("User-Agent", PCCLIENT);
                connection.connect();
            }
            if (s != null) s.addProgress("Connected to Page for");
            long fileSize = connection.getContentLengthLong();
            if (how >= fileSize) {
                if (s != null) s.addProgress(String.format("%.0f",(float)how/fileSize*100)+"% Complete");
                return -2;
            } else {
                in = new BufferedInputStream(connection.getInputStream());
                out = how == 0 ? new FileOutputStream(save) : new FileOutputStream(save,true);
                byte[] buffer = new byte[BUFFSIZE];

                int count;
                if (s != null) s.addProgress("Downloading");
                stopWatch timer = new stopWatch(); timer.start();
                while((count = in.read(buffer,0,BUFFSIZE)) != -1) {
                    if(!MainApp.active)
                        return -2;//application was closed
                    out.write(buffer, 0, count);
                    how+=count;
                    //saveProgress(link,how);
                    if (s != null) s.addProgress(String.format("%.0f",(float)how/fileSize*100)+"% Complete");
                    timer.stop();
                    double secs = timer.getTime().convertToSecs();
                    double speed = (how / secs) / 1024D;
                    long remain = (fileSize - how) / 1024;
                    GameTime g = new GameTime(); g.addSec((long)(remain / speed));
                    if (speed == Double.POSITIVE_INFINITY) {
                        if (s != null) s.addProgress(String.format("%s%d","^^",0));
                    } else {
                        if(s != null) s.addProgress(String.format("%s%f","^^",speed));
                    } if(s != null) s.addProgress(String.format("%s","**"+g));
                }
                in.close();
                out.flush();
                out.close();
            }
        } catch (UncheckedIOException | SocketException e) {
            e.printStackTrace();
            System.out.println("link "+link);
            if (s != null) s.addProgress("Lost Connection: "+e.getMessage());
            return how; //return kb stopped at
        } catch(IOException e){
             if (e.getMessage().contains("Server returned HTTP response code: 416")) {
                System.out.println("Bad range");
                return -2;
            } else {
                e.printStackTrace();
                System.out.println("link "+link);
                if (s != null) s.addProgress("An IO error occurred: "+e.getMessage());
                return how; //return kb stopped at
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("link "+link);
            if (s != null) s.addProgress("An error occurred: "+e.getMessage());
            return how;
        }
        MainApp.settings.cacheUpdate();
        if (s != null) s.addProgress("Finished downloading");
        //File progressFile = new File(MainApp.progressCache.getAbsolutePath()+File.separator+getShortName(clean(link)));
        //progressFile.delete();
        return -2;//if sucessful this will -2
    }
}