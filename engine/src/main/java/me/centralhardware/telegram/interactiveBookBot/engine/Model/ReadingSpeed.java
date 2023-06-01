package me.centralhardware.telegram.interactiveBookBot.engine.Model;

public enum ReadingSpeed {

    HIGH(250, 150),
    MIDDLE(200, 100),
    SLOW(150, 50);

    private final Integer rus;
    private final Integer eng;

    ReadingSpeed(Integer rus, Integer eng) {
        this.rus = rus;
        this.eng = eng;
    }

    public Integer getReadingSpeeds(String lang){
        return switch (lang){
            case "rus" -> rus;
            case "eng" -> eng;
            default -> throw new IllegalStateException();
        };
    }

}
