import java.util.Scanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Exception;

public class RegEx {
  //MACROS
  static final int CONCAT = 0xC04CA7;
  static final int ETOILE = 0xE7011E;
  static final int ALTERN = 0xA17E54;
  static final int PROTECTION = 0xBADDAD;

  static final int PARENTHESEOUVRANT = 0x16641664;
  static final int PARENTHESEFERMANT = 0x51515151;
  static final int DOT = 0xD07;
  
  //REGEX
  private static String regEx;
  
  //CONSTRUCTOR
  public RegEx(){}

  //MAIN
  public static void main(String arg[]) {
    System.out.println("Welcome to Bogota, Mr. Thomas Anderson.");
    if (arg.length!=0) {
      regEx = arg[0];
    } else {
      Scanner scanner = new Scanner(System.in);
      System.out.print("  >> Please enter a regEx: ");
      regEx = scanner.next();
    }
    System.out.println("  >> Parsing regEx \""+regEx+"\".");
    System.out.println("  >> ...");
    
    if (regEx.length()<1) {
      System.err.println("  >> ERROR: empty regEx.");
    } else {
      System.out.print("  >> ASCII codes: ["+(int)regEx.charAt(0));
      for (int i=1;i<regEx.length();i++) System.out.print(","+(int)regEx.charAt(i));
      System.out.println("].");
      try {
        RegExTree ret = parse();
        System.out.println("  >> Tree result: "+ret.toString()+".");
        Automaton a = ret.toAutomaton();
        System.out.println("  >> Epsilon Automaton result: \n"+a.toString()+".");
        System.out.println("  >> Determinist Automaton result: \n"+a.toDFA().toString()+".");
        System.out.println("  >> Pattern match result: \n"+a.toDFA().patternIndexList("bonjour".toCharArray(), 0));
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
  public ArrayList<Indexing.Match> search(String motif, String path) throws Exception
  {
	  if(!motif.contains(".") && !motif.contains("*") && !motif.contains("|"))//que lettres, - et '
	  {
		  if(!motif.contains(" "))
		  {
			  RadixTree radix = RadixTree.loadFromFile("radix.ser");// en partant du principe que le cache du fichier ‡ lire existe
			  ArrayList<Indexing.Match> result = radix.patternIndexList(motif, 0);
			  RadixTree radix_reverse = RadixTree.loadFromFile("radix_reverse.ser");// en partant du principe que le cache du fichier ‡ lire existe
			  ArrayList<Indexing.Match> result_reverse = radix_reverse.patternIndexList(RadixTree.reverse(motif), 0);
			  Indexing indexing = new Indexing();
			  for(Indexing.Match m : result_reverse)
					result.add(indexing .new Match(m.line, m.index - motif.length()));
			  return result;
		  }
		  else
		  {
			  return Kmp.makeMatches(path, motif);
		  }
	  }
	  else
	  {
		  RegExTree ret = parse();
		  Automaton a = ret.toAutomaton();
		  return a.toDFA().makeMatches(path);
	  }
		
  }
  
  
  

  //FROM REGEX TO SYNTAX TREE
  private static RegExTree parse() throws Exception {
    //BEGIN DEBUG: set conditionnal to true for debug example
    if (false) throw new Exception();
    RegExTree example = exampleAhoUllman();
    if (false) return example;
    //END DEBUG

    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    for (int i=0;i<regEx.length();i++) result.add(new RegExTree(charToRoot(regEx.charAt(i)),new ArrayList<RegExTree>()));
    
    return parse(result);
  }
  private static int charToRoot(char c) {
    if (c=='.') return DOT;
    if (c=='*') return ETOILE;
    if (c=='|') return ALTERN;
    if (c=='(') return PARENTHESEOUVRANT;
    if (c==')') return PARENTHESEFERMANT;
    return (int)c;
  }
  private static RegExTree parse(ArrayList<RegExTree> result) throws Exception {
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
    for (RegExTree t: trees) {
      if (!found && t.root==PARENTHESEFERMANT) {
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
      } else {
        result.add(t);
      }
    }
    if (!found) throw new Exception();
    return result;
  }
  private static boolean containEtoile(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==ETOILE && t.subTrees.isEmpty()) return true;
    return false;
  }
  private static ArrayList<RegExTree> processEtoile(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ETOILE && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(last);
        result.add(new RegExTree(ETOILE, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }
  private static boolean containConcat(ArrayList<RegExTree> trees) {
    boolean firstFound = false;
    for (RegExTree t: trees) {
      if (!firstFound && t.root!=ALTERN) { firstFound = true; continue; }
      if (firstFound) if (t.root!=ALTERN) return true; else firstFound = false;
    }
    return false;
  }
  private static ArrayList<RegExTree> processConcat(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    boolean firstFound = false;
    for (RegExTree t: trees) {
      if (!found && !firstFound && t.root!=ALTERN) {
        firstFound = true;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root==ALTERN) {
        firstFound = false;
        result.add(t);
        continue;
      }
      if (!found && firstFound && t.root!=ALTERN) {
        found = true;
        RegExTree last = result.remove(result.size()-1);
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(last);
        subTrees.add(t);
        result.add(new RegExTree(CONCAT, subTrees));
      } else {
        result.add(t);
      }
    }
    return result;
  }
  private static boolean containAltern(ArrayList<RegExTree> trees) {
    for (RegExTree t: trees) if (t.root==ALTERN && t.subTrees.isEmpty()) return true;
    return false;
  }
  private static ArrayList<RegExTree> processAltern(ArrayList<RegExTree> trees) throws Exception {
    ArrayList<RegExTree> result = new ArrayList<RegExTree>();
    boolean found = false;
    RegExTree gauche = null;
    boolean done = false;
    for (RegExTree t: trees) {
      if (!found && t.root==ALTERN && t.subTrees.isEmpty()) {
        if (result.isEmpty()) throw new Exception();
        found = true;
        gauche = result.remove(result.size()-1);
        continue;
      }
      if (found && !done) {
        if (gauche==null) throw new Exception();
        done=true;
        ArrayList<RegExTree> subTrees = new ArrayList<RegExTree>();
        subTrees.add(gauche);
        subTrees.add(t);
        result.add(new RegExTree(ALTERN, subTrees));
      } else {
        result.add(t);
      }
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
  private static RegExTree exampleAhoUllman() {
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
class RegExTree {
  protected int root;
  protected ArrayList<RegExTree> subTrees;
  public RegExTree(int root, ArrayList<RegExTree> subTrees) {
    this.root = root;
    this.subTrees = subTrees;
  }
  //FROM TREE TO PARENTHESIS
  public String toString() {
    if (subTrees.isEmpty()) return rootToString();
    String result = rootToString()+"("+subTrees.get(0).toString();
    for (int i=1;i<subTrees.size();i++) result+=","+subTrees.get(i).toString();
    return result+")";
  }
  private String rootToString() {
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

class Automaton
{
	public Automaton(int size)
	{
		this.init = new boolean[size];
		this.accept = new boolean[size];
		this.etrans = new boolean[size][size];
		this.trans = new int[size][256];
		
		for(int[] line : trans)
		{
			Arrays.fill(line, -1);
		}
		
		for(boolean[] line :etrans)
		{
			Arrays.fill(line, false);
		}

		Arrays.fill(init, false);
		Arrays.fill(accept, false);
	}
	
	public boolean[] init;
	public boolean[] accept;
	public boolean[][] etrans;
	public int[][] trans;
	
	public int size()
	{
		return init.length;
	}
	
	public static Automaton letter(int letter)
	{
		Automaton a = new Automaton(2);
		a.init[0] = true;
		a.accept[1] = true;
		a.trans[0][letter] = 1;
		return a;
	}
	
	public static Automaton dot()
	{
		Automaton a = new Automaton(2);
		a.init[0] = true;
		a.accept[1] = true;
		Arrays.fill(a.trans[0], 1);
		return a;
	}
	
	public static Automaton star(Automaton a)
	{
		Automaton res = new Automaton(a.size() + 2);
		
		res.init[0] = true;
		res.accept[res.size()-1] = true;
		
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(a.trans[cpt][l] != -1)
					res.trans[cpt + 1][l] = a.trans[cpt][l] + 1;
			}
		}
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int cpt2 = 0; cpt2 < a.size(); cpt2++)
			{
				if(a.etrans[cpt][cpt2])
					res.etrans[cpt + 1][cpt2 + 1] = true;
			}
		}
		
		res.etrans[0][1] = true;
		res.etrans[0][res.size()-1] = true;
		res.etrans[res.size()-2][res.size()-1] = true;
		res.etrans[res.size()-2][1] = true;
		return res;
	}
	
	public static Automaton altern(Automaton a, Automaton b)
	{
		Automaton res = new Automaton(a.size() + b.size() + 2);
		
		res.init[0] = true;
		res.accept[res.size()-1] = true;
		
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(a.trans[cpt][l] != -1)
					res.trans[cpt + 1][l] = a.trans[cpt][l] + 1;
			}
		}
		for(int cpt = 0; cpt < b.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(b.trans[cpt][l] != -1)
					res.trans[cpt + a.size() + 1][l] = b.trans[cpt][l] + a.size() + 1;
			}
		}
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int cpt2 = 0; cpt2 < a.size(); cpt2++)
			{
				if(a.etrans[cpt][cpt2])
					res.etrans[cpt + 1][cpt2 + 1] = true;
			}
		}
		for(int cpt = 0; cpt < b.size(); cpt++)
		{
			for(int cpt2 = 0; cpt2 < b.size(); cpt2++)
			{
				if(b.etrans[cpt][cpt2])
					res.etrans[cpt + a.size() + 1][cpt2 + a.size() + 1] = true;
			}
		}
		
		res.etrans[0][1] = true;
		res.etrans[0][a.size() + 1] = true;
		res.etrans[res.size()-2][res.size()-1] = true;
		res.etrans[a.size()][res.size()-1] = true;
		return res;
	}
	
	public static Automaton concat(Automaton a, Automaton b)
	{
		Automaton res = new Automaton(a.size() + b.size());
		
		res.init[0] = true;
		res.accept[res.size()-1] = true;
		
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(a.trans[cpt][l] != -1)
					res.trans[cpt][l] = a.trans[cpt][l];
			}
		}
		for(int cpt = 0; cpt < b.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(b.trans[cpt][l] != -1)
					res.trans[cpt + a.size()][l] = b.trans[cpt][l] + a.size();
			}
		}
		for(int cpt = 0; cpt < a.size(); cpt++)
		{
			for(int cpt2 = 0; cpt2 < a.size(); cpt2++)
			{
				if(a.etrans[cpt][cpt2])
					res.etrans[cpt][cpt2] = true;
			}
		}
		for(int cpt = 0; cpt < b.size(); cpt++)
		{
			for(int cpt2 = 0; cpt2 < b.size(); cpt2++)
			{
				if(b.etrans[cpt][cpt2])
					res.etrans[cpt + a.size()][cpt2 + a.size()] = true;
			}
		}
		
		res.etrans[a.size() - 1][a.size()] = true;
		return res;
	}
	
	public ArrayList<Integer> getAcceptingStates()
	{
		ArrayList<Integer> acceptingStates = new ArrayList<Integer>();
		for(int s = 0; s < accept.length; s++)
		{
			if(accept[s])
				acceptingStates.add(s);
		}
		return acceptingStates;
	}
	
	public ArrayList<Integer> fermeture_epsilon(ArrayList<Integer> states)
	{
		ArrayList<Integer> fermeture = new ArrayList<Integer>();
		ArrayList<Integer> marked = new ArrayList<Integer>();
		marked.addAll(states);
		int traitement;
		while(!marked.isEmpty())
		{
			traitement = marked.remove(0);
			fermeture.add(traitement);
			for(int i = 0; i < this.etrans[traitement].length; i++)
			{
				if((this.etrans[traitement][i])&&(!fermeture.contains(i)&&(!marked.contains(i))))
					marked.add(i);
			}
			
		}
		return fermeture;
	}
	
	public boolean stateExists(ArrayList<int[]> states, int[] possible_state)
	{
		ArrayList<Integer> possible_state_list = new ArrayList<Integer>();
		for(int i : possible_state)
			possible_state_list.add(i);
		for(int[] state : states)
		{
			if(state.length == possible_state_list.size())
			{
				boolean same = true;
				for(int i : state)
				{
					if(!possible_state_list.contains(i))
						same = false;
				}
				if(same)
					return true;
			}
		}
		
		return false;
	}
	public int stateIndexIfExists(ArrayList<int[]> states, int[] possible_state)
	{
		ArrayList<Integer> possible_state_list = new ArrayList<Integer>();
		for(int i : possible_state)
			possible_state_list.add(i);
		int cpt = 0;
		for(int[] state : states)
		{
			if(state.length == possible_state_list.size())
			{
				boolean same = true;
				for(int i : state)
				{
					if(!possible_state_list.contains(i))
						same = false;
				}
				if(same)
					return cpt;
			}
			cpt++;
		}
		
		return -1;
	}
	
	public Automaton toDFA() //cf http://www.momirandum.com/automates-finis/Algorithmeavectransitionsepsilon.html
	{
		boolean[] newInit = new boolean[this.size()];
		boolean[] newAccept = new boolean[this.size()];
		int[][] newTrans = new int [this.size()][256];
		
		for(int[] line : newTrans)
		{
			Arrays.fill(line, -1);
		}

		Arrays.fill(newInit, false);
		Arrays.fill(newAccept, false);
		
		ArrayList<int[]> states = new ArrayList<int[]>();//etats de l'automate d√©terministe en groupes d'etats de l'automate non deterministe
		ArrayList<Integer> marked = new ArrayList<Integer>();
		
		ArrayList<Integer> init_states = new ArrayList<Integer>();//initial states
		for( int s = 0; s < this.size(); s++)
		{
			if(this.init[s])
				init_states.add(s);
		}
		ArrayList<Integer> init_states_epsilon = fermeture_epsilon(init_states);
		newInit[0] = true;
		marked.add(0);
		states.add(init_states_epsilon.stream().mapToInt(i -> i).toArray());//from ArrayList<Integer> to int[]
		
		while(!marked.isEmpty()) 
		{
			
			int traitement = marked.remove(0);
			int[] traitement_nda = states.get(traitement);
			
			for(int l = 0; l < 256; l++)
			{
				ArrayList<Integer> accessible_states = new ArrayList<Integer>();//accessible states for the state being treated with a transition using letter l
				for(int state : traitement_nda)
				{
					if(this.trans[state][l] != -1)
						accessible_states.add(this.trans[state][l]);
				}
				
				if(!accessible_states.isEmpty())
				{
					int[] possible_new_state = fermeture_epsilon(accessible_states).stream().mapToInt(i -> i).toArray();
					if(stateExists(states,possible_new_state))
					{
						newTrans[traitement][l] =stateIndexIfExists(states,possible_new_state);
					}else 
					{
						states.add(possible_new_state);
						marked.add(states.indexOf(possible_new_state));
						newTrans[traitement][l] = states.indexOf(possible_new_state);
					}
				}
			}
		}
		
		//define the accepting states in the new determinist automaton
		ArrayList<Integer> acceptingStates = this.getAcceptingStates();
		for(int i = 0; i < states.size(); i++)
		{
			for(int s : states.get(i))
			{
				if(acceptingStates.contains(s))
					newAccept[i] = true;
			}
		}
		
		//creating the new automaton and setting the different arrays
		int new_size = states.size();
		Automaton res = new Automaton(new_size);
		res.init = Arrays.copyOf(newInit, new_size);
		res.accept = Arrays.copyOf(newAccept, new_size);
		res.trans = Arrays.copyOf(newTrans, new_size);
		
		return res;
	}
	
	public ArrayList<Indexing.Match> patternIndexList(char[] text, int line)
	{
		Indexing indexing = new Indexing();
		ArrayList<Indexing.Match> res = new ArrayList<Indexing.Match>();
		int init = 0;
		while(!this.init[init])
			init++;
		int state = init;
		for(int i = 0; i < text.length; i++)
		{
			state = init;
			int j = i;
			while(this.trans[state][(int)text[j]] != -1)
			{
				state = this.trans[state][(int)text[j]];
				j++;
				if(this.accept[state])
				{
					res.add(indexing.new Match(line, i));
					break;
				}
			}
		}
		return res;
	}
	
	public ArrayList<Indexing.Match> makeMatches(String path) throws FileNotFoundException, IOException
	{
		ArrayList<Indexing.Match> result = new ArrayList<Indexing.Match>();
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			int line_idx = 0;
		    for(String line; (line = br.readLine()) != null; ++line_idx) 
		    {
		    	result.addAll(patternIndexList(line.toCharArray(), line_idx));
		    }
		}
		return result;
	}
	
	public String toString()
	{
		String s = "states : " + this.size() + "\n";
		s += "transitions : \n";
		for(int cpt = 0; cpt < this.size(); cpt++)
		{
			for(int l = 0; l < 256; l++)
			{
				if(this.trans[cpt][l] != -1)
					s += cpt + " -> " + this.trans[cpt][l] + " with " + (char)l + "\n";
			}
		}
		s += "e-transitions : \n";
		for(int cpt1 = 0; cpt1 < this.size(); cpt1++)
		{
			for(int cpt2 = 0; cpt2 < this.size(); cpt2++)
			{
				if(this.etrans[cpt1][cpt2])
					s += cpt1 + " -> " + cpt2 + "\n";
			}
		}
		s += "initial states : ";
		for(int cpt = 0; cpt < this.size(); cpt++)
		{
			if(this.init[cpt])
				s += cpt + " ,";
		}
		s += "\nfinal states : ";
		for(int cpt = 0; cpt < this.size(); cpt++)
		{
			if(this.accept[cpt])
				s += cpt + " ,";
		}
		
		return s;
	}
}