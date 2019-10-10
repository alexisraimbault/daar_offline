import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Indexing implements Serializable{
	
	public class Match implements Serializable
	{
		public Match(int line, int index) {
			super();
			this.line = line;
			this.index = index;
		}
		
		public int line;
		public int index;
		
		public String toString()
		{
			return "(" + line + " , " + index + ")"; 
		}
	}
	
	public HashMap<String, ArrayList<Match>> makeMatches(String path) throws FileNotFoundException, IOException
	{
		HashMap<String, ArrayList<Match>> result = new HashMap<String, ArrayList<Match>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			int line_idx = 0;
		    for(String line; (line = br.readLine()) != null; ++line_idx) 
		    {
		    	int begin_word = 0;
		    	for(int idx = 0; idx < line.length(); ++idx)
		    	{
		    		char c = line.charAt(idx);
		    		if(c >= 'A' && c <= 'Z')
		    			c = Character.toLowerCase(c);
		    		if(!((c >= 'a' && c <= 'z')))
		    		{
		    			
		    			if(begin_word != idx)
		    			{
		    				String word = line.substring(begin_word, idx).toLowerCase();
		    				if(result.containsKey(word))
		    					result.get(word).add(new Match(line_idx, begin_word));
		    				else{
		    					ArrayList<Match> new_match = new ArrayList<Match>();
		    					new_match.add(new Match(line_idx, begin_word));
		    					result.put(word, new_match);
		    				}
		    			}
		    			
		    			begin_word = idx + 1;
		    		}
		    	}
		    	if(begin_word != line.length())
    			{
    				String word = line.substring(begin_word, line.length());
    				if(result.containsKey(word))
    					result.get(word).add(new Match(line_idx, begin_word));
    				else{
    					ArrayList<Match> new_match = new ArrayList<Match>();
    					new_match.add(new Match(line_idx, begin_word));
    					result.put(word, new_match);
    				}
    			}
		    }
		    br.close();
		}
		return result;
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		Indexing tmp = new Indexing();
		System.out.println(tmp.makeMatches("src/test_file.txt"));
	}
	
}
