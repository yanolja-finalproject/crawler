package com.yanolja_final.crawler;

import com.yanolja_final.crawler.application.ExcelExporter;
import com.yanolja_final.crawler.application.PackageDataParser;
import com.yanolja_final.crawler.application.SqlConverter;
import com.yanolja_final.crawler.application.dto.PackageData;
import com.yanolja_final.crawler.reader.PackageCodeReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class Main implements ApplicationRunner {

    @Autowired
    PackageDataParser parser;

    @Autowired
    SqlConverter converter;

    @Override
    public void run(ApplicationArguments args) {
        List<PackageData> packageDatas = parser.parse(PackageCodeReader.read());
        String sql = converter.convert(packageDatas);
        sql = sql.replace("'null'", "NULL");

        saveFile("data.sql", sql);
    }

    public void saveFile(String fileName, String content) {
        String filePath = Paths.get(System.getProperty("user.dir"), "/", fileName).toString();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void beep() {
        int hz = 500;
        int msecs = 500;
        int times = 5;

        try {
            // 오디오 포맷 설정
            AudioFormat af = new AudioFormat(8000F, 8, 1, true, false);
            SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
            sdl.open(af);
            sdl.start();

            for (int i = 0; i < times; i++) {
                byte[] buf = new byte[1];
                int step = (int) (8000 / (hz * 4));
                for (int j = 0; j < 8000 / step * msecs / 1000; j++) {
                    buf[0] = (byte) ((j % step > step / 2) ? 120 : -120);
                    sdl.write(buf, 0, 1);
                }
                sdl.drain();
                Thread.sleep(msecs); // 비프음 사이의 간격
            }
            sdl.stop();
            sdl.close();
        } catch (LineUnavailableException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
