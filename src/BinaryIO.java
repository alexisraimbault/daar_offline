import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BinaryIO
{
	public static void writeString(DataOutputStream dos, String string) throws IOException
	{
		dos.writeUTF(string);
	}
	
	public static String readString(DataInputStream dis) throws IOException
	{
		return dis.readUTF();
	}
	
	public static void writeMatch(DataOutputStream dos, Match match) throws IOException
	{
		dos.writeInt(match.line);
		dos.writeInt(match.index);
	}
	
	public static Match readMatch(DataInputStream dis) throws IOException
	{
		int line_i = dis.readInt();
		int char_i = dis.readInt();
		return new Match(line_i, char_i);
	}
	
	public static void writeMatches(DataOutputStream dos, List<Match> matches) throws IOException
	{
		dos.writeInt(matches.size());
		for(Match match : matches)
		{
			writeMatch(dos, match);
		}
	}
	
	public static List<Match> readMatches(DataInputStream dis) throws IOException
	{
		List<Match> matches = new ArrayList<>();
		int size = dis.readInt();
		for(int i = 0; i < size; ++i)
		{
			matches.add(readMatch(dis));
		}
		return matches;
	}
	
	public static void writeWordMatches(DataOutputStream dos, String word, List<Match> matches) throws IOException
	{
		writeString(dos, word);
		writeMatches(dos, matches);
	}
	
	public static Pair<String, List<Match>> readWordMatches(DataInputStream dis) throws IOException
	{
		String word = readString(dis);
		List<Match> matches = readMatches(dis);
		return new Pair<String, List<Match>>(word, matches);
	}
	
	public static void writeIndexing(DataOutputStream dos, Map<String, List<Match>> indexing) throws IOException
	{
		dos.writeInt(indexing.size());
		for(Map.Entry<String, List<Match>> word_matches : indexing.entrySet())
		{
			writeWordMatches(dos, word_matches.getKey(), word_matches.getValue());
		}
	}
	
	public static Map<String, List<Match>> readIndexing(DataInputStream dis) throws IOException
	{
		Map<String, List<Match>> indexing = new HashMap<>();
		int size = dis.readInt();
		for(int i = 0; i < size; ++i)
		{
			Pair<String, List<Match>> pair = readWordMatches(dis);
			indexing.put(pair.first, pair.second);
		}
		return indexing;
	}
	
	public static void writeRadixTree(DataOutputStream dos, RadixTree tree) throws IOException
	{
		writeString(dos, tree.prefix);
		dos.writeBoolean(tree.terminal);
		if(tree.terminal)
			writeMatches(dos, tree.matches);
		dos.writeBoolean(tree.next != null);
		if(tree.next != null)
			writeRadixTree(dos, tree.next);
		dos.writeBoolean(tree.child != null);
		if(tree.child != null)
			writeRadixTree(dos, tree.child);
	}
	
	public static RadixTree readRadixTree(DataInputStream dis) throws IOException
	{
		String prefix = readString(dis);
		boolean terminal = dis.readBoolean();
		List<Match> matches = null;
		if(terminal)
			matches = readMatches(dis);
		boolean has_next = dis.readBoolean();
		RadixTree next = null;
		if(has_next)
			next = readRadixTree(dis);
		boolean has_child =  dis.readBoolean();
		RadixTree child = null;
		if(has_child)
			child = readRadixTree(dis);
		if(has_next)
		{
			if(has_child)
			{
				return new RadixTree(child, prefix, matches, next);
			}
			else
			{
				return new RadixTree(prefix, matches, next);
			}
		}
		else
		{
			if(has_child)
			{
				return new RadixTree(child, prefix, matches);
			}
			else
			{
				return new RadixTree(prefix, matches);
			}
		}
	}
}
