/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader;

import ChrisPackage.GameTime;
import ChrisPackage.stopWatch;
import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.jsoup.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import org.jsoup.Jsoup;

/**
 *
 * @author christopher
 */
public class CommonUtils {
    public static final String PCCLIENT = "Mozilla/5.0 (X11; Linux x86_64; rv:59.0) Gecko/20100101 Firefox/59.0", 
    MOBILECLIENT = "Mozilla/5.0 (Linux; Android 4.4.4; Nexus 5 Build/KTU84P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.57 Mobile Safari/537.36";
    private static final int BYTE = isWindows() ? 1024 : 1000, BUFFSIZE = BYTE * 600; //1 x 600kb
    
    private static boolean isWindows() {
        String Os = System.getProperty("os.name");
        return (Os.contains("win") || Os.contains("Win"));
    }
    
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
            log("Error splitting","CommonUtils");
        }
        return splits;
    }
    
    public static ImageView getIcon(String path, int height, int width) {
        Image image = new Image(System.class.getResourceAsStream(path));
        ImageView icon = new ImageView();
        icon.setImage(image);
        icon.setFitHeight(height);
        icon.setFitWidth(width);
        return icon;
    }
    
    public static void log(String msg, String context) {
        System.out.println("["+context+"] "+msg);
    }
    
    public static void log(String msg, Object context) {
        if (context == null) 
            System.out.println(msg);
        else System.out.println("["+context.getClass().getSimpleName()+"] "+msg);
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
        else if(format.equals("high"))
            return 2;
        else if(format.equals("low") || format.equals("single"))
            return 1;
        else  {
            boolean started = false;
            for(int i = 0; i < format.length(); i++) 
                if (Character.isDigit(format.charAt(i))) {
                    s.append(format.charAt(i)); started = true;
                } else if (started)
                    break;
        }
        return s.toString().isEmpty() ? 1 : Integer.parseInt(s.toString());
    }
    
    public static List<String> getSortedFormats(Set<String> list) {
        Iterator<String> i = list.iterator(); List<String> q = new ArrayList<>();
        while(i.hasNext())
            q.add(i.next());
        for(int j = 0; j < q.size() -1; j++) { //selection sort
            int min = j;
            for(int k = j+1; k < q.size(); k++)
                if(CommonUtils.getFormatWeight(q.get(k)) > CommonUtils.getFormatWeight(q.get(min)))
                    min = k;
            if(min != j) { //swap
                String temp = q.get(min);
                q.set(min,q.get(j)); 
                q.set(j,temp);
            }
        }
        return q;
    }
    
    public static String addAttr(String attr, String value) {
        return attr == null || value == null ? "" : "\""+attr.replaceAll("\"", "\\\"")+"\":\""+value.replaceAll("\"", "\\\"")+"\"";
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
    
    public static String addId(String name, String id) {
        StringBuilder pure = new StringBuilder();
        pure.append(name.substring(0,name.lastIndexOf(".")));
        if(id.length() > 0) pure.append("-"+id);
        pure.append(name.substring(name.lastIndexOf(".")));
        return pure.toString();
    }
    
    public static boolean checkImageCache(String name) {
        File[] files = MainApp.imageCache.listFiles();
        if (files != null)  {//if cache is not empty
            Arrays.parallelSort(files);
            if (Arrays.binarySearch(files, new File(MainApp.imageCache+File.separator+name)) > -1) {
                log("Found in cache: "+name, "CommonUtils");
                return true;
            }
        }
        log("not found in cache: "+name,"CommonUtils");
        return false;
    }
    
    public static boolean checkPageCache(String name) {
        File[] files = MainApp.pageCache.listFiles();
        if (files != null)  {//if cache is not empty
            Arrays.parallelSort(files);
            if (Arrays.binarySearch(files, new File(MainApp.pageCache+File.separator+name)) > -1) {
                log("Found in cache: "+name, "CommonUtils");
                return true;
            }
        }
        log("not found in cache: "+name,"CommonUtils");
        return false;
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
        return getContentSize(url,false,10);
    }
    
    public static long getContentSize(String url, boolean forbidden, int t) {
        if (url == null) return -5;
        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", PCCLIENT);
            int response = ((HttpURLConnection)connection).getResponseCode();
            //if redirect
            if ((response == HttpURLConnection.HTTP_SEE_OTHER) || (response == HttpURLConnection.HTTP_MOVED_TEMP) || (response == HttpURLConnection.HTTP_MOVED_PERM) || (response == 403 && forbidden)) {
                String location = connection.getHeaderField("Location");
                if (location != null) {
                    if (location.startsWith("/")) 
                        location = "https://"+location;
                    return getContentSize(location, true, t-1);
                    //https://www.pornhd.com/videos/154232/latina-ella-knox-massive1-boobs-almost-suffocate-a-bloke-hd-porn-video
                }
            }
            return connection.getContentLengthLong();
        } catch (SocketException e){
            e.printStackTrace();
            log(e.getMessage(),"CommonUtils");
            return -2;
        } catch (IOException e) {
            e.printStackTrace();
            log(e.getMessage(),"CommonUtils");
            return -3;
        } catch (Exception e) {
            e.printStackTrace();
            log(e.getMessage(),"CommonUtils");
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
            log("Failed to save page: "+ex.getMessage(),"CommonUtils");
        }
        MainApp.settings.cacheUpdate();
    }
    
    public static void erasePage(String name) {
        log(String.valueOf(new File(MainApp.pageCache.getAbsolutePath()+File.separator+name).delete()),"CommonUtils");
    }
    
    public static String sendPost(String url, String params, boolean mobile, String referer, String acceptType) throws MalformedURLException, ProtocolException, IOException {
        return sendPost(url,params,mobile,referer,acceptType,null);
    }
    
    public static String sendPost(String url, String params, boolean mobile, String referer, String acceptType, String host) throws MalformedURLException, ProtocolException, IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("POST");
        if (host != null)
            connection.setRequestProperty("Host",host);
        connection.setRequestProperty("User-Agent",mobile ? CommonUtils.MOBILECLIENT : CommonUtils.PCCLIENT);
        connection.setRequestProperty("Referer",referer);
        connection.setRequestProperty("Accept", acceptType);
        connection.setRequestProperty("Content-Length", Integer.toString(params.length()));
            
        connection.setDoInput(true);
        connection.setDoOutput(true);
            
        try (DataOutputStream w = new DataOutputStream(connection.getOutputStream())) {
            w.writeBytes(params);
            w.flush();
        }
            
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder re = new StringBuilder();
            
        while((line = in.readLine()) != null)
            re.append(line);
        in.close();
        
        return re.toString();
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
        return saveFile(link, saveName, path, s, false);
    }
    
    public static long saveFile(String link, String saveName, File path, OperationStream s, boolean forbid) throws MalformedURLException{
        return saveFile(link, saveName, path.getAbsolutePath(), s, forbid);
    }
    
    public static long saveFile(String link, String saveName, String path, OperationStream s, boolean forbid) throws MalformedURLException{
	BufferedInputStream in;
	FileOutputStream out;
        File dir = new File(path);
        long how = 0, fileSize = 0;
        if (!dir.exists()) 
            dir.mkdirs();
	
	try {
            URLConnection connection = new URL(link).openConnection();
            File save = new File(path+File.separator+saveName);
            how = save.exists() ? save.length() : 0;
            connection.setRequestProperty("User-Agent", PCCLIENT);
            connection.setRequestProperty("Range","bytes="+how+"-");
            int response = ((HttpURLConnection)connection).getResponseCode();
            //if redirect
            if ((response == HttpURLConnection.HTTP_SEE_OTHER) || (response == HttpURLConnection.HTTP_MOVED_TEMP) || (response == HttpURLConnection.HTTP_MOVED_PERM) || (response == 403 && forbid)) {
                String location = connection.getHeaderField("Location");
                if (location != null) {
                    location = location.startsWith("//") ? "http:"+location : location;
                    connection = new URL(location).openConnection();
                    //String cookies = connection.getHeaderField("Set-Cookie");
                    //connection.setRequestProperty("Cookie", cookies);
                    connection.setRequestProperty("User-Agent", PCCLIENT);
                    connection.connect();
                }
            }
            if (s != null) s.addProgress("Connected to Page for");
            fileSize = connection.getContentLengthLong();
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
                    if (s != null) s.addProgress(String.format("%.0f",(float)how/fileSize*100)+"% Complete");
                    timer.stop();
                    double secs = timer.getTime().convertToSecs();
                    double speed = (how / secs) / (double)BYTE;
                    long remain = (fileSize - how) / (long)BYTE;
                    GameTime g = new GameTime(); g.addSec((long)(remain / speed));
                    int use = g.getLength() < 3 ? 3 : g.getLength();
                    if (speed == Double.POSITIVE_INFINITY) {
                        if (s != null) s.addProgress(String.format("%s%d","^^",0));
                    } else {
                        if(s != null) s.addProgress(String.format("%s%f","^^",speed));
                    } if(s != null) s.addProgress(String.format("%s","**"+g.getTimeFormat(use)));
                }
                in.close();
                out.flush();
                out.close();
            }
        } catch (UncheckedIOException | SocketException e) {
            e.printStackTrace();
            log("link \""+link+"\" "+e.getMessage(),"CommonUtils");
            if (s != null) s.addProgress("Lost Connection: "+e.getMessage());
            return how; //return kb stopped at
        } catch(IOException e){
             if (e.getMessage().contains("Server returned HTTP response code: 416")) {
                log("Bad range ("+how+"-"+fileSize+")","CommonUtils");
                return -2;
            } else {
                e.printStackTrace();
                log("link \""+link+"\" "+e.getMessage(),"CommonUtils");
                if (s != null) s.addProgress("An IO error occurred: "+e.getMessage());
                return how; //return kb stopped at
            }
        } catch (Exception e) {
            e.printStackTrace();
            log("link \""+link+"\" "+e.getMessage(),"CommonUtils");
            if (s != null) s.addProgress("An error occurred: "+e.getMessage());
            return how;
        }
        MainApp.settings.cacheUpdate();
        if (s != null) s.addProgress("Finished downloading");
        return -2;//if sucessful return -2
    }
}