package me.centralhardware.telegram.interactiveBookBot.engine.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Next (
        Integer processTo,
        List<Variant> variants
) {

    public static final Integer END_MARKER = -1;

    @JsonIgnore
    public boolean isEnding(){
        return Objects.equals(processTo, END_MARKER);
    }

    @JsonIgnore
    public NextType getType(){
        if (CollectionUtils.isNotEmpty(variants)){
            return NextType.CHOOSE;
        } else if (isEnding()){
            return NextType.END;
        } else {
            return NextType.DIRECT;
        }
    }

}
