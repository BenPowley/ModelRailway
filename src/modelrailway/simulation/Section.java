package modelrailway.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
/**
 * Section is a list of track pieces with pointers to next and previous sections
 * @author powleybenj
 *
 */
public class Section extends ArrayList<Track>{ // a section is a list of tracks , sections can detect trains 

	private Set<Movable> movableObjects = new HashSet<Movable>(); // the trains on the section.
	/**
	 * Section takes a list of tracks in the section.
	 * @param tr
	 */
	public Section(List<Track> tr){
		this.addAll(tr);
	}
	
	public boolean addMovable(Movable m){
		return movableObjects.add(m);
	}
	
	public boolean contains(Movable m){
		return movableObjects.contains(m);
	}
	
	public boolean remove(Movable m){
		return movableObjects.remove(m);
	}
	
	public boolean empty(){
		return (movableObjects.size() == 0);
	}
}
