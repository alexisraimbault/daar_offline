
public class Match
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