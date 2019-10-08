import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class RadixTree implements java.io.Serializable {
	
	public RadixTree next;
	public RadixTree child;
	public String prefix;
	public boolean terminal;
	ArrayList<Indexing.Match> matches;
	public static Indexing indexing = new Indexing();
	
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
	
	public static RadixTree makeFromIndexing(HashMap<String, ArrayList<Indexing.Match>> matches) throws FileNotFoundException, IOException
	{
		
		RadixTree result = null;
		for(Entry<String, ArrayList<Indexing.Match>> pair : matches.entrySet())
		{
			if(result == null)
				result = new RadixTree(pair.getKey(), pair.getValue());
			else
				result.addWord2(pair.getKey(), pair.getValue());
		}
		return result;
	}
	public static RadixTree makeFromIndexingReverse(HashMap<String, ArrayList<Indexing.Match>> matches) throws FileNotFoundException, IOException
	{
		
		RadixTree result = null;
		for(Entry<String, ArrayList<Indexing.Match>> pair : matches.entrySet())
		{
			String key = reverse_rmlast(pair.getKey());
			ArrayList<Indexing.Match> value = new ArrayList<Indexing.Match>();
			for(Indexing.Match m : pair.getValue())
				value.add(indexing.new Match(m.line, m.index + key.length() + 1));
			if(result == null)
				result = new RadixTree(key, value);
			else
				result.addWord2(key, value);
		}
		return result;
	}
	
	public static String reverse_rmlast(String word)
	{
		StringBuilder sb = new StringBuilder(); 
		sb.append(word);
		String full_reverse = sb.reverse().toString();
		return full_reverse.substring(0, full_reverse.length() - 1);
	}
	public static String reverse(String word)
	{
		StringBuilder sb = new StringBuilder(); 
		sb.append(word);
		return sb.reverse().toString();
	}
	
	public void addWord2(String word, ArrayList<Indexing.Match> matches)
	{
		if(this.prefix.equals(word))
			this.terminal = true;
		else
		{
			if(this.prefix.length() < word.length() && word.startsWith(this.prefix))
			{
				if(this.child != null)
					this.child.addWord2(word.substring(this.prefix.length()), matches);
				else
					this.child = new RadixTree(null, word.substring(this.prefix.length()), true, matches);
			}
				
			else
			{
				if(word.length() < this.prefix.length() && this.prefix.startsWith(word))
				{
					RadixTree tmpChild = new RadixTree(this.child,this.prefix.substring(word.length()), this.terminal, this.matches);
					this.child = tmpChild;
					this.prefix = this.prefix.substring(0, word.length());
					this.terminal = true;
				}
				else{
					int cpt_identiques = 0;
					while(cpt_identiques < word.length() && cpt_identiques < this.prefix.length() && word.charAt(cpt_identiques) == this.prefix.charAt(cpt_identiques))
						cpt_identiques ++;
					if(cpt_identiques > 0)
					{
						RadixTree tmpChild = new RadixTree(null,word.substring(cpt_identiques), this.terminal, matches);
						RadixTree tmpChild2 = new RadixTree(this.child,this.prefix.substring(cpt_identiques), this.terminal, this.matches);
						tmpChild.next = tmpChild2;
						this.child = tmpChild;
						this.prefix = this.prefix.substring(0, cpt_identiques);
						this.terminal = true;	
					}else
					{
						if(this.next != null)
							this.next.addWord2(word, matches);
						else{
							this.next = new RadixTree(null, word, true, matches);
						}
					}
				}
			}
		}
	}
	
	public ArrayList<Indexing.Match> patternIndexList(String word, int word_idx)
	{
		int save_word_idx = word_idx;
		int idx = 0;
		if(word.length() - word_idx < prefix.length())
		{
			if(prefix.charAt(0)==word.charAt(word_idx) && prefix.startsWith(word.substring(word_idx, word.length())))//word is a prefix of a word in the text
				return matches;
			if(next != null)
				return next.patternIndexList(word, word_idx);
			else
				return new ArrayList<Indexing.Match>();
		}
			
		for(idx = 0; idx < prefix.length(); ++idx, ++word_idx)
		{
			if (word.charAt(word_idx) != prefix.charAt(idx))
				break;
		}
		
		if(idx == 0){//no letter in common -> search next
			if(next != null)
				return next.patternIndexList(word, save_word_idx);
			else
				return new ArrayList<Indexing.Match>();
		}
		else{//some letters in common
			if(idx == prefix.length())//all prefix letters in common -> child or found word
			{
				if(word_idx == word.length())// found word
					if (terminal)
					{
						return matches;
					}
					else
						return new ArrayList<Indexing.Match>();
				else//child
					return child.patternIndexList(word, word_idx);
			}
			else//not all prefix letters in common -> word not in tree
			{
				return new ArrayList<Indexing.Match>();
			}
		}
	}
	
	public static RadixTree loadFromFile(String path)
	{
		ObjectInputStream ois = null;

	    try {
	      final FileInputStream fichier = new FileInputStream(path);
	      ois = new ObjectInputStream(fichier);
	      final RadixTree tree = (RadixTree) ois.readObject();
	      return tree;
	    } catch (final java.io.IOException e) {
	      e.printStackTrace();
	    } catch (final ClassNotFoundException e) {
	      e.printStackTrace();
	    } finally {
	      try {
	        if (ois != null) {
	          ois.close();
	        }
	      } catch (final IOException ex) {
	        ex.printStackTrace();
	      }
	    }
		return null;
	}
	
	public static void writeInFile(String path, RadixTree tree)
	{
		ObjectOutputStream oos = null;

	    try {
	      final FileOutputStream fichier = new FileOutputStream(path);
	      oos = new ObjectOutputStream(fichier);
	      oos.writeObject(tree);
	      oos.flush();
	    } catch (final java.io.IOException e) {
	      e.printStackTrace();
	    } finally {
	      try {
	        if (oos != null) {
	          oos.flush();
	          oos.close();
	        }
	      } catch (final IOException ex) {
	        ex.printStackTrace();
	      }
	    }
	}
	
	public String toString(){
		
		if(this.child == null && this.next == null)
			return this.prefix;
		else
		{
			if(this.next == null)
				return this.prefix + " (" + this.child.toString() + ") ";
			else
			{
				if(this.child == null)
					return this.prefix + ", " + this.next.toString();
				else
					return this.prefix + " (" + this.child.toString() + ") " + ", " + this.next.toString();
			}
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		
		{
			Indexing indexing = new Indexing();
			HashMap<String, ArrayList<Indexing.Match>> matches = indexing.makeMatches("src/test_file_3.txt");
			RadixTree radix = makeFromIndexing(matches);
			RadixTree radix_reverse = makeFromIndexingReverse(matches);
			System.out.println(radix);
			RadixTree.writeInFile("radix.ser", radix);
			RadixTree.writeInFile("radix_reverse.ser", radix_reverse);
		}
		{
			RadixTree radix = RadixTree.loadFromFile("radix.ser");
			RadixTree radix_reverse = RadixTree.loadFromFile("radix_reverse.ser");
			System.out.println(radix.patternIndexList("sargopette", 0));
			System.out.println(radix.patternIndexList("sargop", 0));
			String word = "rgopette";
			ArrayList<Indexing.Match> matches = radix_reverse.patternIndexList(reverse(word), 0);
			ArrayList<Indexing.Match> result = new ArrayList<Indexing.Match>();
			for(Indexing.Match m : matches)
				result.add(indexing.new Match(m.line, m.index - word.length()));
			System.out.println(result);
		}
		
	}
}
