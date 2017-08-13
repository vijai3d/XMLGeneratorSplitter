package main.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class FileNameCheck {

    public static boolean containsIllegals(String toExamine) {
        Pattern pattern = Pattern.compile("[\\w,\\s-]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }
}
