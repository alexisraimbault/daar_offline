import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;




public class RadixTree {
	
	public RadixTree next;
	public RadixTree child;
	public String prefix;
	public boolean terminal;
	ArrayList<Indexing.Match> matches;
	
	public RadixTree(String prefix, ArrayList<Indexing.Match> matches)
	{
		this(null, prefix, true, matches);
	}
	
	public RadixTree(RadixTree child, String prefix, boolean terminal, ArrayList<Indexing.Match> matches)
	{
		this.child = child;
		this.prefix = prefix;
		this.terminal = terminal;
		this.matches = matches;
	}
	
	public static RadixTree makeFromFile(String path) throws FileNotFoundException, IOException
	{
		Indexing indexing = new Indexing();
		HashMap<String, ArrayList<Indexing.Match>> matches = indexing.makeMatches(path);
		RadixTree result = null;
		for(Entry<String, ArrayList<Indexing.Match>> pair : matches.entrySet())
		{
			if(result == null)
				result = new RadixTree(pair.getKey(), pair.getValue());
			else
				result.addWord(pair.getKey(), pair.getValue());
		}
		
		return result;
	}
	
	public void addWord(String word, ArrayList<Indexing.Match> matches)
	{
		addWord(word, 0, matches);
	}
	
	private void addWord(String word, int word_idx, ArrayList<Indexing.Match> matches)
	{
		char c = word.charAt(word_idx);
		char pc = prefix.charAt(0);
		if(c != pc)
		{
			if(next == null)
				next = new RadixTree(word.substring(word_idx), matches);
			else
				next.addWord(word, word_idx, matches);
		}
		else
		{
			for(int idx = 1; idx < prefix.length(); ++idx, ++word_idx)
			{
				if(word_idx == word.length())
				{
					String suffix = prefix.substring(idx);
					prefix = prefix.substring(0, idx);
					child = new RadixTree(child, suffix, this.terminal, this.matches);
					terminal = true;
				}
				c = word.charAt(word_idx);
				pc = prefix.charAt(idx);

				if(c != pc)
				{
					String suffix = prefix.substring(idx);
					prefix = prefix.substring(0, idx);//todo choose ranking
					child = new RadixTree(child, suffix, this.terminal, this.matches);
					child.next = new RadixTree(word.substring(word_idx), matches);
					terminal = false;
				}
			}
			if(word_idx == word.length())
				this.terminal = true;
		}
	}
	
	public String toString() 
	{
		return "(";//TODO
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		System.out.println(makeFromFile("src/test_file_2.txt"));
	}
}
