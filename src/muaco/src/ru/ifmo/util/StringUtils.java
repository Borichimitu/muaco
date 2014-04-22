package ru.ifmo.util;

/**
 * 
 * @author Daniil Chivilikhin
 *
 */
public class StringUtils {
	public static int levenshteinDistance(String S1, String S2) {
		int m = S1.length(), n = S2.length();
		int[] D1 = new int[n + 1];
		int[] D2 = new int[n + 1];
	 
		for(int i = 0; i <= n; i ++)
			D2[i] = i;
	 
		for(int i = 1; i <= m; i ++) {
			D1 = D2;
			D2 = new int[n + 1];
			for(int j = 0; j <= n; j ++) {
				if(j == 0) D2[j] = i;
				else {
					int cost = (S1.charAt(i - 1) != S2.charAt(j - 1)) ? 1 : 0;
					if(D2[j - 1] < D1[j] && D2[j - 1] < D1[j - 1] + cost)
						D2[j] = D2[j - 1] + 1;
					else if(D1[j] < D1[j - 1] + cost)
						D2[j] = D1[j] + 1;
					else
						D2[j] = D1[j - 1] + cost;
				}
			}
		}
		return D2[n];
	}
	
	public static double levenshteinDistance(String[] first, String[] second) {
		int n = first.length;
        int m = second.length;
        double[][] d = new double[n + 1][m + 1];
        
        for (int i = 0; i <= n; i++) {
                d[i][0] = i;
        }
        for (int j = 0; j <= m; j++) {
                d[0][j] = j;
        }
        
        for (int i = 0; i < n; i++) {
                for (int j = 0; j < m; j++) {
                        int cost;
                        if (first[i].equals(second[j])) {
                                cost = 0;
                        } else {
                                cost = 1;
                        }
                        d[i + 1][j + 1] = Math.min(Math.min(
                                                                d[i][j + 1] + 1, 
                                                                d[i + 1][j] + 1), 
                                                                d[i][j] + cost);
                }
        }
        
        return d[n][m];
	}
	
	public static int hammingDistance(String s1, String s2) {
		int result = 0;
		for (int i = 0; i < Math.min(s1.length(), s2.length()); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				result++;
			}
		}
		result += Math.abs(s1.length() - s2.length());
		return result;
	}
	
	public static int numberOfSameBits(String S1, String S2) {
		int result = 0;
		for (int i = 0; i < Math.min(S1.length(), S2.length()); i++) {
			if (S1.charAt(i) == S2.charAt(i)) {
				result++;
			}
		}
		return result;
	}
	
	public static int count(String s, char character) {
		int result = 0;
		for (char c : s.toCharArray()) {
			if (c == character) {
				result++;
			}
		}
		return result;
	}
}
