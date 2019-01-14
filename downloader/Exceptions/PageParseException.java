/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package downloader.Exceptions;

/**
 *
 * @author christopher
 */
public class PageParseException extends GenericDownloaderException{
    
    public PageParseException(String msg) {
        super(msg);
    }
    
    public PageParseException() {
        super("Error parsing the page");
    }
}
