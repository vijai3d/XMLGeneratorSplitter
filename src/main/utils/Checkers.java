package main.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

 public class Checkers {

    public  boolean checkFilename(String toExamine) {
        Pattern pattern = Pattern.compile("[\\w,\\s-]");
        Matcher matcher = pattern.matcher(toExamine);
        return matcher.find();
    }
     public  boolean checkNumber(String toExamine) {
         Pattern pattern = Pattern.compile("^[1-9]\\d*$");
         Matcher matcher = pattern.matcher(toExamine);
         return matcher.find();
     }
}
