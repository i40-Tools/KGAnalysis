package com.kganalysis.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a support class for string util methods
 * @class StringUtil
 * @Version 1.0
 * @Date 21.09.2016
 * @author Irlan Grangel
 *
 */
public class StringUtil {

	public static String ontExpressivity = "";
	
	/**
	 * Return the String with the first letter in lowercase
	 * @param str
	 * @return
	 */
	public static String lowerCaseFirstChar(String str){
		return str.substring(0, 1).toLowerCase() + str.substring(1); 
	}
	
	/**
	 * 
	 * @param str
	 * @param character
	 * @return
	 */
	public static String replaceChar(String str,String character){
		return str.replaceAll("\\",character);
	}
	
	/**
	 * Removes the underscore sign of an string
	 * @param str
	 * @return String without underscores
	 */
	public static String removeUnderScore(String str){
		String s = str.replaceAll("_", "");
		return s;
	}
	
	/**
	 * Count the number of words in a string
	 * @param String
	 * @return Number of words
	 */
	public static int countWords(String s){
	
		int wordCount = 0;

	    boolean word = false;
	    int endOfLine = s.length() - 1;

	    for (int i = 0; i < s.length(); i++) {
	        // if the char is a letter, word = true.
	        if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
	            word = true;
	            // if char isn't a letter and there have been letters before,
	            // counter goes up.
	        } else if (!Character.isLetter(s.charAt(i)) && word) {
	            wordCount++;
	            word = false;
	            // last word of String; if it doesn't end with a non letter, it
	            // wouldn't count without this.
	        } else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
	            wordCount++;
	        }
	    }
	    return wordCount;
	}
	
	/**
	 * Check whether and URL is correct
	 * @param toCheck
	 * @return
	 */
	public static boolean containsURL(String toCheck){
		final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

		Pattern p = Pattern.compile(URL_REGEX);
		Matcher m = p.matcher(toCheck);//replace with string to compare
		if(m.find()) {
		    return true;
		}
		return false;
	}
	
}
