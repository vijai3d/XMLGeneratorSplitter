package main.utils;

import java.util.Random;

public class RandomString {

     public String getRandomString() {
         String alphabet= "abcdefghijklmnopqrstuvwxyz";
         String string = "";
         Random random = new Random();
         for (int i = 0; i < random.nextInt(200); i++) {
             char c = alphabet.charAt(random.nextInt(26));
             string+=c;
         }
         return string;
    }
}
