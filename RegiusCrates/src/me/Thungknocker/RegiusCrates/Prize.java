package me.Thungknocker.RegiusCrates;

import java.util.List;

/**
 * Created by Jains on 5/7/2014.
 */
public class Prize {
    public String message;
    public List<String> commands;

    public Prize(String m, List<String> cmd) {
        this.message = m;
        this.commands = cmd;
    }
}
