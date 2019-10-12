import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

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
	
	public RadixTree() {
		// TODO Auto-generated constructor stub
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
					this.matches = matches;
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
						//System.out.println(prefix);//debug
						return matches;
					}
					else
						return new ArrayList<Indexing.Match>();
				else//child
				{
					if(child != null)
						return child.patternIndexList(word, word_idx);
					else
						return new ArrayList<Indexing.Match>();
				}	
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
	
	public static HashMap<String, ArrayList<Indexing.Match>> loadIndexingFromFile(String path) throws FileNotFoundException, IOException
	{
		HashMap<String, ArrayList<Indexing.Match>> result = new HashMap<String, ArrayList<Indexing.Match>>();
		Indexing indexing = new Indexing();
		File f = new File(path);
	    FileInputStream inputStream = new FileInputStream(f);
	    Scanner sc = new Scanner(inputStream, "UTF-8");
	    while (sc.hasNextLine()) 
	    {
	       String line = sc.nextLine();
	       String[] index = line.split(" ");
	       ArrayList<Indexing.Match> matches = new ArrayList<Indexing.Match>();
    	   for(int i = 1; i < index.length; i += 2)
    		   matches.add(indexing.new Match(Integer.parseInt(index[i]), Integer.parseInt(index[i+1])));
    	   result.put(index[0], matches);
    	}
		return result;
	}
	
	public static void writeIndexingInFile(String path, HashMap<String, ArrayList<Indexing.Match>> indexing) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
		for(Entry<String, ArrayList<Indexing.Match>> pair : indexing.entrySet())
		{
			writer.write(pair.getKey() + ' ');
			for(Indexing.Match match : pair.getValue())
				writer.write(match.line + " " + match.index + ' ');
			writer.write('\n');
		}
	    writer.close();
	}
	
	public String readMatches( String line )
	{
		Indexing indexing = new Indexing();
		ArrayList<Indexing.Match> tmpMatches = new ArrayList<Indexing.Match>();
		int idx = 0;
		String tmpLine, tmpChar;
		boolean readingLine, readingChar, justRedLine;
		tmpLine = "";
		tmpChar = "";
		justRedLine = false;
		readingLine = false;
		readingChar = false;
		if(line.charAt(idx) != '[')
		{
			System.out.println("can't read matches here !");
			return null;
		}
		while(line.charAt(idx) != ']')
		{
			if(!readingLine && !readingChar && !justRedLine)
			{
				if(line.charAt(idx) >= '0' && line.charAt(idx) <= '9')
				{
					readingLine = true;
					tmpLine += line.charAt(idx);
				}
			}else
			{
				if(readingLine)
				{
					if(line.charAt(idx) >= '0' && line.charAt(idx) <= '9')
						tmpLine += line.charAt(idx);
					else
					{
						readingLine = false;
						justRedLine = true;
					}
				}else
				{
					if(justRedLine)
					{
						if(line.charAt(idx) >= '0' && line.charAt(idx) <= '9')
						{
							justRedLine = false;
							readingChar = true;
							tmpChar += line.charAt(idx);
						}
					}else{
						if(readingChar)
						{
							if(line.charAt(idx) >= '0' && line.charAt(idx) <= '9')
								tmpChar += line.charAt(idx);
							else
							{
								readingChar = false;
								tmpMatches.add(indexing.new Match(Integer.parseInt(tmpLine), Integer.parseInt(tmpChar)));
								tmpLine = "";
								tmpChar = "";
							}
						}
					}
				}
			}
			idx++;
		}
		this.matches = tmpMatches;
		return line.substring(idx + 1);
	}
	
	public String readPrefix( String line )
	{
		int idx = 0;
		String tmpPrefix;
		tmpPrefix = "";
		while(line.charAt(idx) != '[')
		{
			char c = Character.toLowerCase(line.charAt(idx));
			if(c >= 'a' && c <= 'z')
				tmpPrefix += c;
			idx++;
		}
		this.prefix = tmpPrefix;
		return line.substring(idx);
	}
	
	public String readTree(String line, boolean isChild)
	{
		String tmpLine = this.readPrefix(line);
		tmpLine = this.readMatches(tmpLine);
		if(isChild)
		{
			while(tmpLine.length() > 0 && !(tmpLine.charAt(0) == ')'))
			{
				if(tmpLine.charAt(0) == '(')
				{
					this.child = new RadixTree();
					tmpLine = this.child.readTree(tmpLine, true);
				}else
				{
					if(tmpLine.charAt(0) == ',')
					{
						this.next = new RadixTree();
						tmpLine = this.next.readTree(tmpLine, false);
					}
				}
			}
			if(tmpLine.length() != 0 && tmpLine.charAt(0) == ')')
				return tmpLine.substring(1);
			else
				return tmpLine;
		}else
		{
			while(tmpLine.length() > 0 && !(tmpLine.charAt(0) == ')'))
			{
				if(tmpLine.charAt(0) == '(')
				{
					this.child = new RadixTree();
					tmpLine = this.child.readTree(tmpLine, true);
				}else
				{
					if(tmpLine.charAt(0) == ',')
					{
						this.next = new RadixTree();
						tmpLine = this.next.readTree(tmpLine, false);
					}
				}
			}
			
			return tmpLine;
		}
		
	}
	
	public static RadixTree readManually(String path) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(path));
		String line = br.readLine();
		br.close();
		RadixTree tree = new RadixTree();
		tree.readTree(line, false);
		return tree;
	}
	
	public static void writeManually(String path, RadixTree tree) throws IOException
	{
		BufferedWriter writer = new BufferedWriter(new FileWriter(path));
	    writer.write(tree.toString());
	    writer.close();
	}
	
	public String toString(){
		
		if(this.child == null && this.next == null)
			return this.prefix + this.matches ;
		else
		{
			if(this.next == null)
				return this.prefix +  this.matches + "(" + this.child.toString() + ")";
			else
			{
				if(this.child == null)
					return this.prefix +  this.matches  + "," + this.next.toString();
				else
					return this.prefix  + this.matches + "(" + this.child.toString() + ")" + "," + this.next.toString();
			}
		}
		
	}
	
	public static void main(String[] args) throws FileNotFoundException, IOException
	{
		
		{
			Indexing indexing = new Indexing();
			HashMap<String, ArrayList<Indexing.Match>> matches = indexing.makeMatches("src/test_file.txt");
			//System.out.println(matches.get("sargon"));
			RadixTree radix = makeFromIndexing(matches);
			//RadixTree radix_reverse = makeFromIndexingReverse(matches);
			//System.out.println(radix);
			//System.out.println(radix_reverse);
			//RadixTree.writeManually("indexing.txt.txt", radix);
			System.out.println("big file done");
			//RadixTree radixRed = readManually("radix.txt");
			//System.out.println(radixRed);
			//RadixTree.writeIndexingInFile("indexing.txt", matches);
			
			HashMap<String, ArrayList<Indexing.Match>> redMatches = RadixTree.loadIndexingFromFile("src/test_file_indexing.txt");
			RadixTree redRadix = makeFromIndexing(redMatches);
			System.out.println("radix from indexing done");
			//System.out.println(redRadix);
		}
		/*{
			RadixTree radix = RadixTree.loadFromFile("radix.ser");
			RadixTree radix_reverse = RadixTree.loadFromFile("radix_reverse.ser");
			System.out.println(radix.patternIndexList("sargon", 0));
			System.out.println(radix.patternIndexList("sargop", 0));
			String word = "rgopette";
			ArrayList<Indexing.Match> matches = radix_reverse.patternIndexList(reverse(word), 0);
			ArrayList<Indexing.Match> result = new ArrayList<Indexing.Match>();
			for(Indexing.Match m : matches)
				result.add(indexing.new Match(m.line, m.index - word.length()));
			System.out.println(result);
		}*/
		
	}
}
