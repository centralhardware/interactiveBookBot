package me.centralhardware.telegram.interactiveBookBot;

import me.centralhardware.telegram.interactiveBookBot.engine.Model.Next;
import me.centralhardware.telegram.interactiveBookBot.engine.Model.Part;
import me.centralhardware.telegram.interactiveBookBot.engine.Model.Variant;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {
        String book = Resources.toString(Resources.getResource(args[0]),
                Charset.forName(args[1]));

        List<Part> parts = new ArrayList<>();

        List<String> paragraps = new ArrayList<>();
        List<Variant> variants = new ArrayList<>();
        Integer number = null;
        List<String> split = List.of(book.split("\n"));
        for (int i = 0; i < split.size(); i++) {
            String line = split.get(i);
            line = line.strip();
            line = line.replace("\r", "");
            if (NumberUtils.isDigits(line)) {
                number = Integer.valueOf(line);
                continue;
            }

            if (line.startsWith("next")) {
                Next next = new Next(Integer.parseInt(line.replace("next: ", "")),
                        null);
                parts.add(new Part(number, new ArrayList<>(paragraps), next));
                paragraps.clear();
                variants.clear();
                continue;
            }

            if (line.startsWith("variant: ")) {
                String str = line.replace("variant: ", "");
                String text = str.split("-")[0];
                Integer processTo = Integer.valueOf(str.split("-")[1]);
                variants.add(new Variant(text, processTo, null));

                if (split.size() == i + 1){
                    parts.add(new Part(number, new ArrayList<>(paragraps), new Next(null, new ArrayList<>(variants))));
                    continue;
                }

                if (!split.get(i + 1).startsWith("variant")){
                    parts.add(new Part(number, new ArrayList<>(paragraps), new Next(null, new ArrayList<>(variants))));
                    paragraps.clear();
                    variants.clear();
                    continue;
                }

                continue;
            }

            if (line.equalsIgnoreCase("the end")){
                parts.add(new Part(number,
                        new ArrayList<>(paragraps),
                        new Next(-1, null)));
                paragraps.clear();
                variants.clear();
                continue;
            }

            paragraps.add(line);
        }

        File file = new File("./res.txt");
        Files.writeString(file.toPath(),
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(parts));
    }
}
