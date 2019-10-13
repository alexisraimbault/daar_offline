import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Automaton
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
		
		ArrayList<int[]> states = new ArrayList<int[]>();//etats de l'automate déterministe en groupes d'etats de l'automate non deterministe
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
	
	public ArrayList<Match> patternIndexList(char[] text, int line)
	{
		ArrayList<Match> res = new ArrayList<Match>();
		int init = 0;
		while(!this.init[init])
			init++;
		int state = init;
		for(int i = 0; i < text.length; i++)
		{
			state = init;
			
			int j = i;
			while( j<text.length && text[j] < 256 && this.trans[state][(int)text[j]] != -1 )
			{
				state = this.trans[state][(int)text[j]];
				j++;
				
				if(this.accept[state])
				{
					res.add(new Match(line, i));
					break;
				}
			}
		}
		return res;
	}
	
	public List<Match> makeMatches(String path) throws FileNotFoundException, IOException
	{
		List<Match> result = new ArrayList<Match>();
		try (BufferedReader br = new BufferedReader(new FileReader(path)))
		{
			int line_idx = 0;
		    for(String line; (line = br.readLine()) != null; ++line_idx) 
		    {
		    	result.addAll(patternIndexList(line.toCharArray(), line_idx));
		    }
		    br.close();
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