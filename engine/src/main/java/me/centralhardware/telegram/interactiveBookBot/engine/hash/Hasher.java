package me.centralhardware.telegram.interactiveBookBot.engine.hash;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class Hasher {

    public String hash(String str){
        return Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
    }

}
