package Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class TopChannelSaver {
    public static void saveTopChannels() throws IOException {

        String url = "https://twitchtracker.com/channels/live/english";
        String path = "channels.txt";

        Document doc = Jsoup.connect(url).get();
        Elements element = doc.getElementsByClass("ri-name");

        String[] topChannelNames = element.text().split(" ");


        Files.deleteIfExists(Paths.get(path));
        for (String topChannelName : topChannelNames) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true))) {
                bw.write(topChannelName);
                bw.newLine();
                bw.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}

