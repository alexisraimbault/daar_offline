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
				result.addWord2(pair.getKey(), pair.getValue());
		}
		
		return result;
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
						System.out.println(prefix);
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
	
	/*
	public void addWord(String word, ArrayList<Indexing.Match> matches)
	{
		addWord(word, 0, matches);
	}
	
	private void addWord(String word, int word_idx, ArrayList<Indexing.Match> matches)
	{
		char c = Character.toLowerCase(word.charAt(word_idx));
		char pc = Character.toLowerCase(prefix.charAt(0));
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
				c = Character.toLowerCase(word.charAt(word_idx));
				pc = Character.toLowerCase(prefix.charAt(idx));

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
	*/
	
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
		RadixTree radix = makeFromFile("src/test_file_3.txt");
		System.out.println(radix);
		RadixTree.writeInFile("radix.ser", radix);
		RadixTree radix_cpy = RadixTree.loadFromFile("radix.ser");
		System.out.println(radix_cpy);
		System.out.println(radix.patternIndexList("sargopette", 0));
		System.out.println(radix.patternIndexList("mauvaistest", 0));
		System.out.println(radix.patternIndexList("bobo", 0));
	}
}
