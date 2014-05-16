/*
Name: Visaahan Anandarajah
Date: March 24, 2014
Purpose: To implement the Boyer Moore algorithm for faster string searching purposes (used for highlighting text)
*/

import java.util.HashMap;

public class BoyerMoore {
public static void main(String[] args) {
        String test = "tag";       
        StringBuilder haystack = new StringBuilder (3000000);
        for (int i = 0; i < 10; i++)
        {
                haystack.append("tag");
        }
        String temp = BoyerMoore.BoyerMooreAlgo(haystack.toString(), test, "yo");
		System.out.println (temp);
    }
	/*This function runs the actual algorithm. It searches for a needle within a haystack and once it finds it
	  replaces it with a certain word (replacer) and then returns the final result. 
	  This algorithm uses character mismatches and reverse searching
	  to effectively search through large pieces of text
	  
	  Terminology:
		haystack: the text you are searching for a pattern in
		needle: the pattern you are searching for
		
	  The Boyer Moore Algorithm works as follows:
	  
		1. Generate the Last Occurence Array (LOA) and Suffix Skip Array (SSA) (descriptions of what they are found in appropriate sections)
		2. Suppose you have a haystack of length n and a needle of length m. Create a haystack pointer that points the a m-th character
			in the haystack and a needle pointer that points to the last character of the needle
		3. Compare the 2 characters
			i. If they are not the same move the haystack pointer by the minimum # produced between the LOA and the SSA
			ii. If they do match, move the haystack pointer and the needle pointer by 1.
		4. Repeat Step 3 till pattern is found or reached end of haystacks
		
	  This function has the added functionality of replacing a pattern with the word by keeping track of the previous needle found
	*/
	
	public static String BoyerMooreAlgo (String haystack, String needle, String replacer){
      
		//this builds the new result
        int expectedMatches = 200;
        needle = needle.toLowerCase();
        StringBuilder temp_haystack = new StringBuilder (haystack.toLowerCase());
        StringBuilder result = new StringBuilder(temp_haystack.length()
                + expectedMatches * Math.abs ((replacer.length() - needle.length())));
        
		//this generate the last occurenence array (please read the function commenting for more information)
        HashMap lastOcc = lastOccurrence (needle);
		
		//this generates the suffix skip array (please read the function commets for more information)
        int[] suffixSkip = suffixSkip (needle);
        
		//this are the pointers used to keep track of where you are in the haystack and where you are in the needle
        int needle_length = needle.length() - 1;
        int haystack_ptr = needle_length;
        int needle_ptr = needle_length;
        int lastNeedleIndex = 0;
       
	   //while we have not reached the end of the haystack
        while (haystack_ptr < temp_haystack.length()){
            //found word
			if (needle_ptr == -1){
				//if pattern found within haystack, at beginning of haystack or at end of haystack
                if (((haystack_ptr > 0) && (haystack_ptr +  needle.length() < haystack.length() - 1))
						|| (haystack_ptr == -1)
						|| (haystack_ptr + needle.length() + 1 == haystack.length()) {
                    
                    result.append (haystack.substring (lastNeedleIndex, haystack_ptr+1)).append (replacer);
                    lastNeedleIndex = haystack_ptr + needle.length() + 1;  
                }
				
				//jump double the length due to pattern already found and to cover next pattern being reverse searched
                haystack_ptr += 2 * needle.length();
                needle_ptr = needle_length; 
            }
            
            //in-progress (characters match)
            else if (temp_haystack.charAt (haystack_ptr) == needle.charAt (needle_ptr))
            {
                haystack_ptr--;
                needle_ptr--;
            }
            
            //mismatch (take the minimum)
            else {
                char key = temp_haystack.charAt (haystack_ptr);
                int badCharSkip = (lastOcc.containsKey (key)) ? (Integer)lastOcc.get(key) : -1;
                haystack_ptr += needle_length - Math.min(badCharSkip,suffixSkip[needle_ptr]);
                needle_ptr = needle_length;
            }
        }
        
        //append the tail
        result.append (haystack.substring (lastNeedleIndex, temp_haystack.length()));
        return result.toString();
    }
    
	//This generates the last occurence of each letter in the pattern
	/*
		The purpose of the last occurence array is to keep track of the last occurence
		of all characters in the pattern
		
		This is useful in the following situation:
			Let P be the pattern
			Let a be the character we are comparing in the haystack
			Let b be the character we are comparing in the needle (in P)
			
			Suppose we get the case where a != b but a is found in P. It is possible that we found the pattern in the haystack
			It's just the character that is seen in the haystack may be seen at a different location in P as opposed to where the needle pointer was.
			Therefore, we want to match the pattern such that we match the pattern with the letter in the alternate as well.
			So we shift the pattern to make sure it matches at the alternate location.
		
		This is also known as the "Bad Character Rule"
	*/
    private static HashMap lastOccurrence(String str) {
        HashMap lastOcc = new HashMap();
        str = str.toLowerCase();

        for (int i = 0; i < str.length(); i++) {
            char key = str.charAt(i);

            if (!(lastOcc.containsKey(key))) {
                int value = str.lastIndexOf(key);
                lastOcc.put(key, value);
            }
        }
        return lastOcc;
    }

    /*
		This function creates a suffix skip array
		Let P be the pattern
		A suffix skip array finds a pattern such that
			SSA(i) = j - 1 such that P[j..j+(n-i)] = P[i..n] but P[j-1] != P[i-1] & j != i for i from 0..n 
			(- indexes are used when j < 0 < j+(n-i)) We may decide what letters to put at the - indexes
			This is useful because in the case a word contains the same sequence of letters (ie. P[i..n])
			and we may find a mismatch at P[i-1]. If we shift the needle pointer to P[j-1]
			P[j..j+(n-i)] has already been implicitly matched
			
		This is also known as the good suffix rule
	*/
	
    private static int[] suffixSkip(String str) {
        str = str.toLowerCase();
        int[] temp = new int[str.length()];
        String longestPrefix = "";
        int length = str.length();

        //find largest prefix that is a suffix (used half of length since suffix size cannot be larger than prefix size)
		//want to find the largest prefix that is also a suffix to make using negative indexes easier
        for (int j = 0; j <= length / 2; j++) {
            String prefix = str.substring(0, j);
            String suffix = str.substring(length - j, length);
            if (prefix.equals(suffix)) {
                longestPrefix = prefix;
            }
        }

        //develop suffix skip array
        boolean found;
        int lastOccurrence;
        int suffixLength = longestPrefix.length();

        for (int i = length - 1; i >= 0; i--) {
            found = false;
            lastOccurrence = i;
			//currently finding a letter that does not match the last character (ie the case where i = n)
            if (i == length - 1) {
                for (int j = i - 1; j >= 0; j--) {
                    if (!(str.charAt(j) == (str.charAt(i)))) {
                        temp[i] = j;
                        break;
                    }
                }
            } 
			
			//not the last character (ie. trying to match a sequence of strings)
			else {
                String substring = str.substring(i + 1, length);
                while (!found) {
                    int index = str.lastIndexOf(substring, lastOccurrence);
                    //the substring not found in string, therefore not in string
                    if (index == -1) {
                        //in case of the possibility there is a prefix that is also a suffix which would need to be accounted for if word larger than suffix
						//(ie. this is where negative indexes are used)
                        temp[i] = (substring.length() > suffixLength) ? substring.length() * -1 + suffixLength - 1 : substring.length() *-1 - 1;
                        found = true;
                    } 
                    //ie found the longest prefix & substring is largest prefix
                    else if (index == 0 && (substring.length()) == suffixLength) {
                        temp[i] = -1;
                        found = true;
                    } 
                    //there is a matching substring
                    else {
                        //checks if characters beforehand after different
                        if ((index != 0) && (str.charAt(i) != str.charAt(index - 1))) {
                            temp[i] = index - 1;
                            found = true;
                        } 
                        //else looks for substring after index one found with both having same characters beforehand
                        else {
                            lastOccurrence = index - 1;
                        }
                    }
                }

            }
        }
        return temp;
    }
}
