import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Kmp {
	
	public static boolean isPrefix(char[] suffix, char[] word)
	{
		for(int i = 0; i < suffix.length; i++)
		{
			if(word[i] != suffix[i])
				return false;
		}
		return true;
	}
	
	public static int biggestSuffixPrefix(char[] word)
	{
		
		int j = 1;
		char[] potential_suffix= Arrays.copyOfRange(word, j, word.length);
		while( (j < word.length) && (!isPrefix(potential_suffix,word)))
		{
			j++;
			potential_suffix= Arrays.copyOfRange(word, j, word.length);
		}
		return word.length - j;
	}
	
	public static int[] retenue(char[] factor)
	{
		int[] res = new int[factor.length + 1];
		res[0] = -1;
		res[factor.length] = 0;
		for(int i = 1; i < factor.length; i++)
		{
			if(factor[i] == factor[0])
				res[i] = -1;
			else 
			{
				res[i] = biggestSuffixPrefix(Arrays.copyOfRange(factor, 0, i));
				if(res[i] == 1 && res[i-1] == -1 && factor[i] == factor[1])//if is identical to factor[1]
					res[i] = 0;
			}
		}
		return res;
	}
	
	public static int patternIndex(char[] factor, int[] retenue, char[] text, int i)
	{
		int j = 0;
		while (i < text.length)
		{
			if(j == factor.length)
				return i - factor.length;
			if (text[i] == factor[j])
			{
				++i;
				++j;
			}
			else
			{
				if (retenue[j] == -1)
				{
					++i;
					j = 0;
				}
				else
				{
					j = retenue[j];
				}
			}
		}
		return -1;
	}
	
	public static int patternIndex(char[] factor, int[] retenue, char[] text)
	{
		int i = 0;
		int j = 0;
		while (i < text.length)
		{
			if(j == factor.length)
				return i - factor.length;
			if (text[i] == factor[j])
			{
				++i;
				++j;
			}
			else
			{
				if (retenue[j] == -1)
				{
					++i;
					j = 0;
				}
				else
				{
					j = retenue[j];
				}
			}
		}
		return -1;
	}
	
	public static ArrayList<Indexing.Match> makeMatches(String path, String motif) throws FileNotFoundException, IOException
	{
		char[] factor = motif.toCharArray();
		ArrayList<Indexing.Match> result = new ArrayList<Indexing.Match>();
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			int line_idx = 0;
		    for(String line; (line = br.readLine()) != null; ++line_idx) 
		    {
		    	result.addAll(patternIndexList(factor, retenue(factor), line.toCharArray(), line_idx));
		    }
		}
		return result;
	}
	
	public static ArrayList<Indexing.Match> patternIndexList(char[] factor, int[] retenue, char[] text, int line)
	{
		Indexing indexing = new Indexing();
		ArrayList<Indexing.Match> result = new ArrayList<Indexing.Match>();
		int i = 0;
		int j = 0;
		while (i < text.length)
		{
			if(j == factor.length)
			{
				result.add(indexing.new Match(line, i - factor.length));
				++i;
				j = 0;
			}
			else {
				if (text[i] == factor[j])
				{
					++i;
					++j;
				}
				else
				{
					if (retenue[j] == -1)
					{
						++i;
						j = 0;
					}
					else
					{
						j = retenue[j];
					}
				}
			}
		}
		if(j == factor.length)
			result.add(indexing.new Match(line, i - factor.length));
		return result;
	}
	
	public static char[] fileToCharArray(String path) throws FileNotFoundException, IOException
	{
		String s = "";
		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
		    for(String line; (line = br.readLine()) != null; ) {
		    	s+= line;
		    }
		}
		char[] tmpArray = s.toCharArray();
		return Arrays.copyOfRange(tmpArray, 3, tmpArray.length);
	}
	
	public static ArrayList<Indexing.Match> searchFile(String path ,char[] factor) throws FileNotFoundException, IOException
	{
		return patternIndexList(factor, retenue(factor), fileToCharArray(path), 0);
	}
	
	//MAIN
	  public static void main(String arg[]) throws FileNotFoundException, IOException {
		  char[] factor = "sa".toCharArray();
		  System.out.println(searchFile("src/test_file_2.txt", factor));
	  }
}
