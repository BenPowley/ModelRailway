package modelrailway.core;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import modelrailway.simulation.Track;
/**
 * Section is a list of track pieces with pointers to next and previous sections
 * Section also maintains both a set of movable object id's inside the section,
 * and a queue of movable object id's that want to obtain a lock to the section.
 * @author powleybenj
 *
 */
public class Section extends CopyOnWriteArrayList<Track>{ // a section is a list of tracks , sections can detect trains
	public class RemovePair{
		public RemovePair(boolean retValue, Integer listedTrain){this.retValue = retValue; this.listedTrain = listedTrain;}
		public boolean retValue;
		public Integer listedTrain;
	}

	static{
		sectionNumberCounter = 0;
	}

	// The set of movable objects keeps track of the movable objects that are inside the section

	private Set<Integer> movableObjects = new ConcurrentSkipListSet<Integer>(); // the trains on the section.

	private Queue<Integer> entryRequests = new ConcurrentLinkedQueue<Integer>();

	public RemovePair removeFromQueue(Integer t){
		if(entryRequests.contains(t)){

			return new RemovePair(entryRequests.remove(t), entryRequests.peek());

		}
		else{
			return new RemovePair(false,null);
		}
	}


	private static int sectionNumberCounter;
	private int sectionNumber;
    /**
     * Section takes a list of tracks that are being given to the section.
     * The section constructor automatically allocates an id number that is used as the ID of the section.
     * The ID number can be set later by hand also however the automatically generated ID numbers ensure that IDs are unique.
     * @param tr
     */
	public Section(List<Track> tr){
		sectionNumber = sectionNumberCounter;
		sectionNumberCounter++;
		this.addAll(tr);
	}
	/**
	 * The reserveSection method reserves the section for the supplied train. if there is nothing on the waiting list the section is reserved
	 * and true is returned. otherwise false is returned. The calling method should stop the train if false is returned.
	 * @param t
	 * @return
	 */
	public boolean reserveSection(Integer t){
		synchronized(movableObjects){
		synchronized(entryRequests){
		//System.out.println("Train: "+t +" reserving section for section "+this.getNumber() + "entryRequests :"+entryRequests+" movables: "+movableObjects);
		//if there are no requests for access to the section, and there are no objects inside the section we offer the train object to the list of requests
		// and return true. Returning true signifies that the train attempting to reserve the section does not need to stop.
		if(entryRequests.size() == 0 && movableObjects.size() == 0){
			entryRequests.offer(t);
			//System.out.println("return true");
			return true;
		}
		else { // else if either there were other entry requests or there were objects inside the section we add the supplied
			// train  to the queue of entry requests then return false. Returning false signifies that the train requesting access to
			// the section needs to stop.
			entryRequests.offer(t);
			return false;
		}
		}
		}

	}
	/**
	 * The onRequestList method checks if the ID for the train provided is on the list of trains that are queued for access to the section
	 * @param t
	 * @return
	 */
	public boolean onRequestList(Integer t){
		synchronized(entryRequests){
		if(entryRequests.contains(t)) return true;
		return false;
		}
	}
	/**
	 * The resetCounter method sets the sectionNumberCounter to zero.
	 * This is used in unit testing when we wish to create another track.
	 */
	public static void resetCounter(){
		sectionNumberCounter = 0;
	}
	/**
	 * getNumber returns an integer which is the section number.
	 * @return
	 */
	public int getNumber(){
		return sectionNumber;
	}
	/**
	 * Add movable adds a movable object to the section.
	 * When there are some entry requests and the supplied integer the next train id to be polled we remove the train from the entry requests.
	 * @param m
	 * @return
	 */
	public boolean addMovable(Integer m){
		//System.out.println(entryRequests);
		synchronized(entryRequests){
		synchronized(movableObjects){
		if(entryRequests.size() != 0){
		  if(m != null && entryRequests.peek() == m) entryRequests.poll();
		  }
		return movableObjects.add(m);
		}
		}
	}
	/**
	 * Check whether the supplied trainID is in the list of movableObjects.
	 * @param m
	 * @return
	 */
	public boolean containsMovable(Integer m){
		synchronized(movableObjects){
		return movableObjects.contains(m);
		}
	}

	/**
	 * The removeMovable method removes a movable object from the list of Movable objects in the Section,
	 * then the method returns a pair containing the return value of the removal and a train on the waiting queue if there is one.
	 * The calling method is responsible for notifying the train that it may start.
	 * @param m
	 * @return
	 */
	public Section.RemovePair  removeMovable(Integer m){
	   synchronized(movableObjects){
		boolean ret = movableObjects.remove(m);
		Integer tr = null;
		if(movableObjects.size() == 0){
		    tr = entryRequests.peek();
		}
		return new Section.RemovePair(ret,tr);
	   }
	}

	/**
	 * emptyMovable returns wheather there are no trains or other movables in the section.
	 * @return
	 */
	public boolean emptyMovable(){
		synchronized(movableObjects){
		return (movableObjects.size() == 0);
		}
	}
	/**
	 * returns the set of movables in the section.
	 * @return
	 */
	public Set<Integer> getMovableSet(){
		return movableObjects;
	}

	/**
	 * Returns a string representation of the section.
	 * @return
	 */
	public String toString(){

		return "section: "+super.toString()+"  movables: "+movableObjects.toString() +" entryRequests: "+entryRequests.toString();
	}
}
