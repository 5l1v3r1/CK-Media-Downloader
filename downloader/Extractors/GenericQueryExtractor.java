/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Extractors;

import downloader.DataStructures.GenericQuery;
import downloader.Exceptions.GenericDownloaderException;
import java.io.File;
import java.io.IOException;
import org.jsoup.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.util.Vector;

/**
 *
 * @author christopher
 */
public abstract class GenericQueryExtractor extends GenericExtractor{
    
    //This probably should have been an interface
    GenericQueryExtractor(String url, File thumb, String videoName){
        super(url,thumb,videoName);
    }
    
    GenericQueryExtractor() {
        
    }
    
    public abstract GenericQuery query(String search) throws IOException, SocketTimeoutException, UncheckedIOException, Exception;
    
    protected abstract Vector<File> parse(String url) throws IOException, SocketTimeoutException, UncheckedIOException, GenericDownloaderException;
}
