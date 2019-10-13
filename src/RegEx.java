import java.util.Scanner;
import java.util.Map.Entry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Exception;

public class RegEx
{
	//MACROS
	static final int CONCAT = 0xC04CA7;
  	static final int ETOILE = 0xE7011E;
  	static final int ALTERN = 0xA17E54;
  	static final int PROTECTION = 0xBADDAD;
  
  	public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
  	public static final String ANSI_RESET = "\u001B[0m";

  	static final int PARENTHESEOUVRANT = 0x16641664;
  	static final int PARENTHESEFERMANT = 0x51515151;
  	static final int DOT = 0xD07;
  
  	//REGEX
  	private static String regEx;
  
  	//CONSTRUCTOR
  	public RegEx(){}

  	//MAIN
  	public static void main(String arg[]) throws Exception
  	{
  		//runTests();
  		runTests_radix_reading_file();
  		System.out.println("Welcome to Bogota, Mr. Thomas Anderson.");
    	if (arg.length!=0)
    	{
    		regEx = arg[0];
    	}
    	else
    	{
    		Scanner scanner = new Scanner(System.in);
      		System.out.print("  >> Please enter a regEx: ");
      		regEx = scanner.nextLine();
    	}
    	System.out.println("  >> Parsing regEx \""+regEx+"\".");
    	System.out.println("  >> ...");
    
    	if (regEx.length()<1)
    	{
    		System.err.println("  >> ERROR: empty regEx.");
    	}
    	else
    	{
	    	/*System.out.print("  >> ASCII codes: ["+(int)regEx.charAt(0));
	      	for (int i=1;i<regEx.length();i++) System.out.print(","+(int)regEx.charAt(i));
	      	System.out.println("].");*/
	    	try {
	    	  	/* RegExTree ret = parse();
	        	System.out.println("  >> Tree result: "+ret.toString()+".");
	        	Automaton a = ret.toAutomaton();
	        	System.out.println("  >> Epsilon Automaton result: \n"+a.toString()+".");
	        	System.out.println("  >> Determinist Automaton result: \n"+a.toDFA().toString()+".");
	        	System.out.println("  >> Pattern match result: \n"+a.toDFA().patternIndexList("bonjour".toCharArray(), 0));*/
	    	  	printResult(search(regEx, "src/test_file.txt"),"src/test_file.txt", regEx);
	      	} catch (Exception e) {
	    	  //System.err.println("  >> ERROR: syntax error for regEx \""+regEx+"\".");
	    	  	e.printStackTrace();
	      	}
    	}

    	System.out.println("  >> ...");
    	System.out.println("  >> Parsing completed.");
    	System.out.println("Goodbye Mr. Anderson.");
  	}
  
  	//SEARCH
  	public static List<Match> search(String motif, String path) throws Exception
  	{
	  	if(!motif.contains(".") && !motif.contains("*") && !motif.contains("|"))//que lettres, - et '
	  	{
		  	System.out.println(motif);
		  	if(motif.matches("[a-zA-Z]+"))
		  	{
			  	System.out.println("using radix...");
			  	HashMap<String, ArrayList<Match>> matches = RadixTree.loadIndexingFromFile("indexing.txt");// en partant du principe que le cache du fichier à lire existe
			  	RadixTree radix = RadixTree.makeFromIndexing(matches);
			  	RadixTree radix_reverse = RadixTree.makeFromIndexingReverse(matches);
			  	List<Match> result = radix.patternIndexList(motif, 0);
			  	List<Match> result_reverse = radix_reverse.patternIndexList(RadixTree.reverse(motif), 0);
			  	for(Match m : result_reverse)
					result.add(new Match(m.line, m.index - motif.length()));
			  	return result;
		  	}
		  	else
		  	{
			  	System.out.println("using KMP...");
			  	return Kmp.makeMatches(path, motif);
		  	}
	  	}
	  	else
	  	{
		  	System.out.println("using automaton...");
		  	RegExTree ret = parse();
		  	Automaton a = ret.toAutomaton();
		  	System.out.println("Determinist Automaton result: \n"+a.toDFA().toString()+".");
		  	return a.toDFA().makeMatches(path);
	  	}
		
  	}
  	
  	
    //TESTS
    public static void runTests_radix_reading_file() throws Exception
  	{
    	HashMap<Integer, ArrayList<Long>> radix_motif_perf = new HashMap<Integer, ArrayList<Long>>();
  		HashMap<Integer, Double> radix_motif_perf_avg = new HashMap<Integer, Double>();
  		HashMap<String, ArrayList<Match>> matches = RadixTree.loadIndexingFromFile("indexing.txt");// en partant du principe que le cache du fichier à lire existe
  		Thread.sleep(4000);
  		for(Entry<String, ArrayList<Match>> pair : matches.entrySet())
  		{
  			if(!radix_motif_perf.containsKey(pair.getKey().length()) || radix_motif_perf.get(pair.getKey().length()).size()<10)
  			{
  				
  				long startTime = System.currentTimeMillis();
  	  			HashMap<String, ArrayList<Match>> matchesRed = RadixTree.loadIndexingFromFile("indexing.txt");// en partant du principe que le cache du fichier à lire existe
  	  			RadixTree radix = RadixTree.makeFromIndexing(matchesRed);
  	  			RadixTree radix_reverse = RadixTree.makeFromIndexingReverse(matchesRed);
  	  			
  	  			List<Match> result = radix.patternIndexList(pair.getKey(), 0);
  	  			List<Match> result_reverse = radix_reverse.patternIndexList(RadixTree.reverse(pair.getKey()), 0);
  	  			
  	  			for(Match m : result_reverse)
  	  				result.add(new Match(m.line, m.index - pair.getKey().length()));
  	  			
  	  			long stopTime = System.currentTimeMillis();
  	  			long duration = stopTime - startTime;
  	  			
  	  			if(radix_motif_perf.containsKey(pair.getKey().length()))
  	  				radix_motif_perf.get(pair.getKey().length()).add(duration);
  	  			else
  	  			{
  	  				ArrayList<Long> tmp = new ArrayList<Long>();
  	  				tmp.add(duration);
  	  				radix_motif_perf.put(pair.getKey().length(), tmp);
  	  			}
  			}
  		}
  		for(Entry<Integer, ArrayList<Long>> pair : radix_motif_perf.entrySet())
  		{
  			Collections.sort(pair.getValue());
  			for(Long n : pair.getValue())
  				System.out.print(n + " ");
  			System.out.print('\n');
  		}
  	}
  	
    public static void runTests() throws Exception
  	{
  	  	
  		//radix tests
  		HashMap<Integer, ArrayList<Long>> radix_motif_perf = new HashMap<Integer, ArrayList<Long>>();
  		HashMap<Integer, Double> radix_motif_perf_avg = new HashMap<Integer, Double>();
  		HashMap<String, ArrayList<Match>> matches = RadixTree.loadIndexingFromFile("indexing.txt");// en partant du principe que le cache du fichier à lire existe
  		Thread.sleep(4000);
  		for(Entry<String, ArrayList<Match>> pair : matches.entrySet())
  		{
  			long startTime = System.currentTimeMillis();
  			
  			RadixTree radix = RadixTree.makeFromIndexing(matches);
  			RadixTree radix_reverse = RadixTree.makeFromIndexingReverse(matches);
  			
  			List<Match> result = radix.patternIndexList(pair.getKey(), 0);
  			List<Match> result_reverse = radix_reverse.patternIndexList(RadixTree.reverse(pair.getKey()), 0);
  			
  			for(Match m : result_reverse)
  				result.add(new Match(m.line, m.index - pair.getKey().length()));
  			
  			long stopTime = System.currentTimeMillis();
  			long duration = stopTime - startTime;
  			
  			if(radix_motif_perf.containsKey(pair.getKey().length()))
  				radix_motif_perf.get(pair.getKey().length()).add(duration);
  			else
  			{
  				ArrayList<Long> tmp = new ArrayList<Long>();
  				tmp.add(duration);
  				radix_motif_perf.put(pair.getKey().length(), tmp);
  			}
  		}
  		//System.out.println(radix_motif_perf);
  		//average
  		for(Entry<Integer, ArrayList<Long>> pair : radix_motif_perf.entrySet())
  		{
  			long sum = 0;
  			int cpt = 0;
  			for(long exec_time : pair.getValue())
  			{
  				sum += (double)exec_time;
  				cpt++;
  			}
  			radix_motif_perf_avg.put(pair.getKey(), (double)sum/cpt);
  		}
  		System.out.println("radix : regEx size avg execution time for a same file");
  		for(Entry<Integer, Double> pair : radix_motif_perf_avg.entrySet())
  		{
  			System.out.println(pair.getKey() + " " + pair.getValue());
  		}

  		
  		
  		//KMP tests
  		
  		HashMap<Integer, ArrayList<Long>> kmp_motif_perf = new HashMap<Integer, ArrayList<Long>>();
  		HashMap<Integer, Double> kmp_motif_perf_avg = new HashMap<Integer, Double>();
  		for(Entry<String, ArrayList<Match>> pair : matches.entrySet())
  		{
  			long startTime = System.currentTimeMillis();
  			
  			Kmp.makeMatches("src/test_file.txt", pair.getKey());
  			
  			long stopTime = System.currentTimeMillis();
  			long duration = stopTime - startTime;
  			
  			if(kmp_motif_perf.containsKey(pair.getKey().length()))
  				kmp_motif_perf.get(pair.getKey().length()).add(duration);
  			else
  			{
  				ArrayList<Long> tmp = new ArrayList<Long>();
  				tmp.add(duration);
  				kmp_motif_perf.put(pair.getKey().length(), tmp);
  			}
  		}
  		//System.out.println(kmp_motif_perf);
  		//average
  		for(Entry<Integer, ArrayList<Long>> pair : kmp_motif_perf.entrySet())
  		{
  			long sum = 0;
  			int cpt = 0;
  			for(long exec_time : pair.getValue())
  			{
  				sum += (double)exec_time;
  				cpt++;
  			}
  			kmp_motif_perf_avg.put(pair.getKey(), (double)sum/cpt);
  		}
  		System.out.println("KMP : regEx size avg execution time for a same file");
  		for(Entry<Integer, Double> pair : kmp_motif_perf_avg.entrySet())
  		{
  			System.out.println(pair.getKey() + " " + pair.getValue());
  		}
  		
  		//automaton tests
  		
  		

  	}
  
  	//print
  	public static void printResult(List<Match> matches, String path, String word) throws FileNotFoundException, IOException
  	{
  		matches.sort((Match o1, Match o2)-> o1.line!=o2.line ? o1.line-o2.line : o2.index-o1.index);
  		System.out.println("printing the " + matches.size() + " matches...");
  		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			int line_idx = 0;
		    for(String line; (line = br.readLine()) != null; ++line_idx) 
		    {
		    	boolean toPrint = !matches.isEmpty() && line_idx == matches.get(0).line;
		    	while(!matches.isEmpty() && line_idx == matches.get(0).line){
		    		String tmpLineBefore = line.substring(0, matches.get(0).index);
		    		String tmpLineAfter = line.substring(matches.get(0).index);
		    		line = tmpLineBefore + "@" + tmpLineAfter;
		    		matches.remove(0);
		    	}
		    	if(toPrint)
		    		System.out.println(line);
		    }
		    br.close();
		}
  	}
  
  
  

  	//FROM REGEX TO SYNTAX TREE
  	private static RegExTree parse() throws Exception
  	{
	    //BEGIN DEBUG: set conditionnal to true for debug example
	    if (false) throw new Exception();
	    RegExTree example = exampleAhoUllman();
	    if (false) return example;
	    //END DEBUG
	
	    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
	    for (int i = 0 ; i < regEx.length(); i++)
	    	result.add(new RegExTree(charToRoot(regEx.charAt(i)), new ArrayList<RegExTree>()));
	    
	    return parse(result);
  	}
  	
  	private static int charToRoot(char c)
  	{
	    if (c=='.') return DOT;
	    if (c=='*') return ETOILE;
	    if (c=='|') return ALTERN;
	    if (c=='(') return PARENTHESEOUVRANT;
	    if (c==')') return PARENTHESEFERMANT;
	    return (int)c;
  	}
  	
  	private static RegExTree parse(ArrayList<RegExTree> result) throws Exception
  	{
	    while (containParenthese(result)) result=processParenthese(result);
	    while (containEtoile(result)) result=processEtoile(result);
	    while (containConcat(result)) result=processConcat(result);
	    while (containAltern(result)) result=processAltern(result);

	    if (result.size()>1) throw new Exception();
    		return removeProtection(result.get(0));
  	}
  	private static boolean containParenthese(ArrayList<RegExTree> trees) {
	  	for (RegExTree t: trees) if (t.root==PARENTHESEFERMANT || t.root==PARENTHESEOUVRANT) return true;
    	return false;
  	}
  	private static ArrayList<RegExTree> processParenthese(ArrayList<RegExTree> trees) throws Exception {
  		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
  		boolean found = false;
  		for (RegExTree t: trees)
  		{
  			if (!found && t.root==PARENTHESEFERMANT)
  			{
	    	  	boolean done = false;
	        	ArrayList<RegExTree> content = new ArrayList<RegExTree>();
	        	while (!done && !result.isEmpty())
	        		if (result.get(result.size()-1).root==PARENTHESEOUVRANT) { done = true; result.remove(result.size()-1); }
	          		else content.add(0,result.remove(result.size()-1));
	        	if (!done) throw new Exception();
	        	found = true;
	        	ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
	        	subTrees.add(parse(content));
	        	result.add(new RegExTree(PROTECTION, subTrees));
  			} else
    	  		result.add(t);
    	}
    	if (!found)
    		throw new Exception();
    	return result;
  	}
  	
  	private static boolean containEtoile(ArrayList<RegExTree> trees)
  	{
  		for (RegExTree t: trees) if (t.root==ETOILE && t.subTrees.isEmpty()) return true;
	  		return false;
  	}
  	
  	private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception
  	{
  		ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    	boolean found = false;
    	for (RegExTree t: trees)
    	{
    		if (!found && t.root==ETOILE && t.subTrees.isEmpty())
    		{
		        if (result.isEmpty()) throw new Exception();
		        found = true;
		        RegExTree last = result.remove(result.size()-1);
		        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
		        subTrees.add(last);
		        result.add(new RegExTree(ETOILE, subTrees));
      		}
      		else
    	  		result.add(t);
    	}
    	return result;
  	}
  	private static boolean containConcat(ArrayList<RegExTree> trees) {
  		boolean firstFound = false;
  		for (RegExTree t: trees)
  		{
  			if (!firstFound && t.root!=ALTERN) { firstFound = true; continue; }
  			if (firstFound) if (t.root!=ALTERN) return true; else firstFound = false;
  		}
  		return false;
  	}
  	
  	private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception
  	{
	    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
	    boolean found = false;
	    boolean firstFound = false;
    	for (RegExTree t: trees)
    	{
    		if (!found && !firstFound && t.root!=ALTERN)
    		{
		        firstFound = true;
		        result.add(t);
		        continue;
      		}
      		if (!found && firstFound && t.root==ALTERN)
      		{
		        firstFound = false;
		        result.add(t);
        		continue;
      		}
      		if (!found && firstFound && t.root!=ALTERN)
      		{
		        found = true;
		        RegExTree last = result.remove(result.size()-1);
		        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
		        subTrees.add(last);
		        subTrees.add(t);
		        result.add(new RegExTree(CONCAT, subTrees));
      		}
      		else
    	  		result.add(t);
    	}
    	return result;
  	}
  	private static boolean containAltern(ArrayList<RegExTree> trees)
  	{
  		for (RegExTree t: trees) if (t.root==ALTERN && t.subTrees.isEmpty())
  			return true;
  		return false;
  	}
  	private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception
  	{
	    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
	    boolean found = false;
	    RegExTree gauche = null;
	    boolean done = false;
    	for (RegExTree t: trees)
    	{
    		if (!found && t.root==ALTERN && t.subTrees.isEmpty())
    		{
    	  		if (result.isEmpty())
    	  			throw new Exception();
        		found = true;
        		gauche = result.remove(result.size()-1);
        		continue;
      		}
      		if (found && !done)
      		{
		        if (gauche==null) throw new Exception();
		        done=true;
		        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
		        subTrees.add(gauche);
		        subTrees.add(t);
		        result.add(new RegExTree(ALTERN, subTrees));
      		}
      		else
      			result.add(t);
    	}
    	return result;
  	}
  	private static RegExTree removeProtection(RegExTree tree) throws Exception {
	    if (tree.root==PROTECTION && tree.subTrees.size()!=1) throw new Exception();
	    if (tree.subTrees.isEmpty()) return tree;
	    if (tree.root==PROTECTION) return removeProtection(tree.subTrees.get(0));
	
	    ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
	    for (RegExTree t: tree.subTrees) subTrees.add(removeProtection(t));
	    return new RegExTree(tree.root, subTrees);
  	}
  
  	//EXAMPLE
  	// --> RegEx from Aho-Ullman book Chap.10 Example 10.25
  	private static RegExTree exampleAhoUllman()
  	{
	    RegExTree a = new RegExTree((int)'a', new ArrayList<RegExTree>());
	    RegExTree b = new RegExTree((int)'b', new ArrayList<RegExTree>());
	    RegExTree c = new RegExTree((int)'c', new ArrayList<RegExTree>());
	    ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
	    subTrees.add(c);
	    RegExTree cEtoile = new RegExTree(ETOILE, subTrees);
	    subTrees = new ArrayList<RegExTree>();
	    subTrees.add(b);
	    subTrees.add(cEtoile);
	    RegExTree dotBCEtoile = new RegExTree(CONCAT, subTrees);
	    subTrees = new ArrayList<RegExTree>();
	    subTrees.add(a);
	    subTrees.add(dotBCEtoile);
	    return new RegExTree(ALTERN, subTrees);
  	}
}

//UTILITARY CLASS
class RegExTree
{
	protected int root;
	protected ArrayList<RegExTree> subTrees;
	
	public RegExTree(int root, ArrayList<RegExTree> subTrees)
	{
		this.root = root;
		this.subTrees = subTrees;
	}
	//FROM TREE TO PARENTHESIS
	public String toString()
	{
		if (subTrees.isEmpty())
			return rootToString();
		String result = rootToString() + "(" + subTrees.get(0).toString();
		for (int i = 1; i < subTrees.size(); i++)
			result+=","+subTrees.get(i).toString();
		return result+")";
	}
	
	private String rootToString()
	{
		if (root==RegEx.CONCAT) return ".";
		if (root==RegEx.ETOILE) return "*";
		if (root==RegEx.ALTERN) return "|";
		if (root==RegEx.DOT) return ".";
		return Character.toString((char)root);
	}
  
	public Automaton toAutomaton()
	{
	    if (root==RegEx.CONCAT)
	    	return Automaton.concat(subTrees.get(0).toAutomaton(), subTrees.get(1).toAutomaton());
	    else if (root==RegEx.ETOILE)
	    	return Automaton.star(subTrees.get(0).toAutomaton());
	    else if (root==RegEx.ALTERN)
	    	return Automaton.altern(subTrees.get(0).toAutomaton(), subTrees.get(1).toAutomaton());
	    else if (root==RegEx.DOT)
	    	return Automaton.dot();
	    else
	    	return Automaton.letter(root);
	}
}

