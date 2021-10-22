package hw2;

import java.io.FileReader;
import java.io.BufferedReader;
import java.lang.StringBuilder;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileHandler {
    final static String template = "template.html";

    public FileHandler() {
    }

    public String getAbsolutePath() {
        Path path = Paths.get(template);
        Path absolutePath = path.toAbsolutePath();
        return absolutePath.toString();
    }

    public String readFile() {
        String s;
        StringBuilder content=new StringBuilder(1024);
        try {
            FileReader fr=new FileReader(getAbsolutePath());
            BufferedReader br= new BufferedReader(fr);
            while((s=br.readLine())!=null) {
                content.append(s + "\r\n");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }

    public void updateFile() {
    }
}






