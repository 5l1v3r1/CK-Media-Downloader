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
public class PageNotFoundException extends GenericDownloaderException{
    
    public PageNotFoundException() {
        super("Page not Found");
    }
    
    public PageNotFoundException(String page) {
        super(page+" not Found");
    }
}
