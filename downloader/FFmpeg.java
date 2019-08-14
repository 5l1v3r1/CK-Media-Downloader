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

import downloaderProject.MainApp;
import downloaderProject.OperationStream;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FFmpeg {
    final private ProcessBuilder pBuilder;
    private String input, output;
    private BufferedReader status;
    private BufferedOutputStream comm;
    private static String ffmpegPath;

    public FFmpeg() {
        pBuilder = new ProcessBuilder();
        ffmpegPath = MainApp.OS == MainApp.OsType.Windows ? "ffmpeg.exe" : "ffmpeg";
    }

    public void setOutDir(String dir) {
        pBuilder.directory(new File(dir));
    }

    public void setOutDir(File dir) {
        pBuilder.directory(dir);
    }

    public void setInput(String in) {
        this.input = in;
    }

    public void setOutput(String out) {
        this.output = out;
    }
    
    public static void setFFmpegPath(String s) {
        ffmpegPath = s + (s.endsWith(File.separator) ? "" : File.separator) + ffmpegPath;
    }

    private void getProgress(OperationStream s) throws IOException { //for non live
        long total, current;
        String line = status.readLine();
        while(!line.matches(".*Duration: \\d{2}:\\d{2}:\\d{2}[.]\\d{2}.*"))
            line = status.readLine();

        Pattern pattern = Pattern.compile("Duration: (?<dur>\\d{2}:\\d{2}:\\d{2})[.]\\d+");
        Matcher m = pattern.matcher(line);
        m.find();
        total = CommonUtils.getSeconds(m.group("dur"));

        pattern = Pattern.compile("time=(?<time>\\d{2}:\\d{2}:\\d{2})[.]\\d+");
        while ((line = status.readLine()) != null) {
            m = pattern.matcher(line);
            if (m.find()) {
                current = CommonUtils.getSeconds(m.group("time"));
                s.addProgress(String.format("%.0f",(float)current/total * 100)+"% Complete");
            }
            //CommonUtils.log(line, this);
        }
    }

    public void stop() throws IOException {
        comm.write('q');
        comm.flush();
        status.close();
        comm.close();
    }

    public int run() throws IOException, InterruptedException{
        return run(null);
    }
    
    public int run(OperationStream backComm) throws IOException, InterruptedException {
        pBuilder.command(ffmpegPath, "-y", "-i", input, "-c", "copy", output);
        Process p = pBuilder.start();
        status = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        comm = new BufferedOutputStream(p.getOutputStream());
        if (backComm != null)
            getProgress(backComm);
        return p.waitFor();
    }
}