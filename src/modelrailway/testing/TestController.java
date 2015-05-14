package modelrailway.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import modelrailway.core.Controller;
import modelrailway.core.Event;
import modelrailway.core.Section;
import modelrailway.core.Event.Listener;
import modelrailway.core.Route;
import modelrailway.simulation.Switch;
import modelrailway.core.Train;
import modelrailway.simulation.BackSwitch;
import modelrailway.simulation.Crossing;
import modelrailway.simulation.ForwardSwitch;
import modelrailway.simulation.Simulator;
import modelrailway.simulation.Track;
import modelrailway.util.Pair;
/**
 * This test controller has no collision detection but just tests to make sure that a train can follow a route. in order to test the train simulator.
 * @author powleybenj
 *
 */
public class TestController implements Controller, Listener {

	private Event.Listener trackController = null;
	private List<Listener> listeners = new ArrayList<Listener>();
	private Map<Integer,Route> trainRoutes = new ConcurrentHashMap<Integer,Route>();
	private Map<Integer,modelrailway.core.Train> trainOrientations ;
	private Map<Integer,Boolean> isMoving = new ConcurrentHashMap<Integer,Boolean>();
	private Map<Integer, Section> sections;
	private Track head;

	/**
	 * the trainOrientations method returns a mapping from trainID integers to modelrailway.core.Train objects.
	 * @return
	 */
	public Map<Integer, modelrailway.core.Train> trainOrientations(){
		return trainOrientations;
	}
	/**
	 * sections() returns a mapping from sectionID integers to Section objects.
	 * @return
	 */
	public Map<Integer, Section> sections(){
		return sections;
	}
	/**
	 * the routes() method returns a map from trainID integers to Route objects.
	 * @return
	 */
	public Map<Integer, Route> routes(){
		return trainRoutes;
	}
	/**
	 * The testController is produced using a mapping of trainIDs to trainorientations, a mapping of sectionID's  to Section objects, The head section of a train track,
	 * And a Controller, This TestController is listed as a listener to the controller provided.
	 *
	 * @param orientations
	 * @param intSecMap
	 * @param head
	 * @param trackController
	 */
	public TestController( Map<Integer,modelrailway.core.Train> orientations, Map<Integer, Section> intSecMap , Track head, Event.Listener trackController){
		this.trackController = trackController;
		trainOrientations = orientations; // are the trains going backwards or fowards.
		this.sections = intSecMap;
		this.head = head;
	}

	public void adjustSection(Event e){
		synchronized(isMoving){
			Integer sectionID = ((Event.SectionChanged)e).getSection(); // figure out which train was moving in then set section in modelrailway.core.Train

			for(Map.Entry<Integer, modelrailway.core.Train> tr : trainOrientations.entrySet()){
				Train trainObj = tr.getValue();
			//	System.out.println("sectinoID: "+sectionID);
			//	System.out.println("isMoving: "+ isMoving);
			//	System.out.println("tr: "+tr);
			//	System.out.println("tr.getKey(): "+tr.getKey());
			//	System.out.println("trainroutes: "+ trainRoutes);
			//	System.out.println("isMoving.get(tr.getKey): "+isMoving.get(tr.getKey()));
			//	System.out.println("trainRoutes.get(tr.getKey()): "+trainRoutes.get(tr.getKey()));


				if(isMoving.get(tr.getKey()) != null && isMoving.get(tr.getKey())){
					System.out.println("next section: in adjust");
					System.out.println("sectionID: "+sectionID);
					System.out.println("trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection()): "+trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection()));
					if(trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection()) == sectionID){
						trainObj.setSection(sectionID);
					}
				}
			}
	    }
	}
	@Override
	public void notify(Event e) {

		if(e instanceof Event.SectionChanged && ((Event.SectionChanged) e).getInto()){ // when there is a section change into another section
			//System.out.println("section change event."+((Event.SectionChanged) e).getSection());
			adjustSection(e);
		    moveIntoSection(e);
		}


		for(Listener l : listeners){

			l.notify(e);
		}
	}
	
	public Pair<Track,Track> getCurrentTrackSection(Section currentSection, Section previousSection, boolean movingForward){
		Track prev = null;
		Track curr = null;
		for(Track t: currentSection){
			if(movingForward){
				if(t.getPrevious(true).getSection() == previousSection){
					prev = t.getPrevious(true);
					curr = t;
					
				}
                if(t.getPrevious(false).getSection() == previousSection){
                	prev = t.getPrevious(false);
                	curr = t;
                }
			    if(t.getPrevious(true).getAltSection() == previousSection){
			    	prev = t.getPrevious(true);
			    	curr = t;
			    }
			    if(t.getPrevious(false).getAltSection() == previousSection){
					prev = t.getPrevious(false);
					curr = t;
				}

			}
			else{
				if(t.getNext(true).getSection() == previousSection){
					prev = t.getNext(true);
					curr = t;
					
				}
                if(t.getNext(false).getSection() == previousSection){
                	prev = t.getNext(false);
                	curr = t;
                }
			    if(t.getNext(true).getAltSection() == previousSection){
			    	prev = t.getNext(true);
			    	curr = t;
			    }
			    if(t.getNext(false).getAltSection() == previousSection){
					prev = t.getNext(false);
					curr = t;
				}
			}
		}
		return new Pair<Track,Track>(prev,curr);
	}

	/**
	 * We have moved into a section. Do what is necissary to configure the section that we have just moved into.
	 * note find correct train does not support diamond crossing.
	 * @param e
	 * @return
	 */
	private void moveIntoSection(Event e){
		for(Entry<Integer, Train> trainOrientation: trainOrientations.entrySet()){ // for all the trains on the track

			Integer section = trainOrientation.getValue().currentSection();
			//System.out.println("Section: "+section);
			System.out.println("e.getSection(): "+((Event.SectionChanged) e).getSection()+" section: "+section);
	    	if(section ==  ((Event.SectionChanged) e).getSection()){ // check that the front of the train is in the section
	    		// first work out which track segments we are currently dealing with.
	    		
	    		Integer prevSection = trainRoutes.get(trainOrientation.getKey()).prevSection(section); // the previous section that we came from
	    		Integer nextSection = trainRoutes.get(trainOrientation.getKey()).prevSection(section);
	    		Section previous = sections.get(prevSection);
	    		
	    		Integer trainSection = ((Event.SectionChanged) e).getSection(); // store the section number in a variable
	    		
	    		System.out.println("previous: "+prevSection);
	    		System.out.println("trainSection: "+trainSection);
	    		/*
	    		Track lastTrack = sec.get(sec.size()-1);
	    		Track firstTrack = sec.get(0);
	    		Track thisTrack = null;
	    		if(lastTrack.getSection().containsMovable(trainOrientation.getKey())){
	    			thisTrack = lastTrack;
	    		} else if (lastTrack.getAltSection() != null && lastTrack.getAltSection().containsMovable(trainOrientation.getKey())){
	    			thisTrack = lastTrack;
	    		} else if (firstTrack.getSection().containsMovable(trainOrientation.getKey())){
	    			thisTrack = firstTrack;
	    		} else if (firstTrack.getAltSection() != null && firstTrack.getAltSection().containsMovable(trainOrientation.getKey())){
	    			thisTrack = firstTrack;
	    		} else{
	    			throw new RuntimeException("invalid track in move into Section");
	    		}
	*/
	    		System.out.println("try get current track: "+trainSection +" prevSection: "+prevSection);
	    		Track thisTrack = null;
	    		if(trainOrientation.getValue().currentOrientation()){
	    		Pair<Track,Track> prevCurrPair = getCurrentTrackSection(sections.get(trainSection),sections.get(prevSection),trainOrientation.getValue().currentOrientation());
	    		// before handling the switches make sure that the sections of the track piece that we came from do not have this train in any of them
	    		thisTrack = prevCurrPair.snd;
	    		}
	    		else{
	    		Pair<Track,Track> nextCurrPair = getCurrentTrackSection(sections.get(trainSection),sections.get(nextSection),trainOrientation.getValue().currentOrientation());
		    		// before handling the switches make sure that the sections of the track piece that we came from do not have this train in any of them
		    	thisTrack = nextCurrPair.snd;
	    		}

	    		// for all tracks in the previous section, remove this train from the queue of trains that have requested the section, instruct the next train on the queue to go.
	    		for(Track t :previous){
	    			boolean trainMoved = false;
	    			Section tracSec = t.getSection();
	    			Section trackAltSec = t.getAltSection();
	    			if(trackAltSec != null){
	    				Pair<Boolean,Integer> pair = null;
	    				if(!trackAltSec.isQueueEmpty() && trainOrientation.getKey() != null) pair = tracSec.removeFromQueue(trainOrientation.getKey());
	    				if(pair != null){
	    				   if(pair.fst){ // instruct next train to move.
	    					   if(!trainMoved) {
	    						   this.resumeTrain(pair.snd);
	    						   trainMoved = true;
	    					   }
	    				   }
	    				}
	    			}
	    			Pair<Boolean,Integer> pair = null;
	    			if(trackAltSec != null && !trackAltSec.isQueueEmpty() && trainOrientation.getKey() != null)  pair = trackAltSec.removeFromQueue(trainOrientation.getKey());
	    			if(pair != null){
	    			   if(pair.fst){
	    				   if(!trainMoved) {
	    					   this.resumeTrain(pair.snd);
	    					   trainMoved = true;
	    				   }
	    			   }
	    			}
	    		}

	    		
	    		System.out.println("Switch"+thisTrack);
	    		System.out.println("trackSection: "+thisTrack.getSection().getNumber());
	    		
	    		if(thisTrack instanceof Switch){ // move switches
	    			
	    			Integer trainID = trainOrientation.getKey();
	    			Route rt = trainRoutes.get(trainID);
	    			Section sectionS = sections.get(section);
	    		    Integer nextSec = rt.nextSection(section);
	    			Integer prevSec = rt.prevSection(section); // the section we came from
	    			Pair<Integer, Integer> sectionPair = new Pair<Integer,Integer>(prevSec,nextSec);
	    		    // for sectionPair
	    		    List<Boolean> switchingOrder = sectionS.retrieveSwitchingOrder(sectionPair);
	    		    System.out.println("Section,previous: "+ previous);
	    		    System.out.println("sectionS: "+sectionS);
	    		    System.out.println("sectionPair: "+sectionPair);
	    			if(!trainOrientation.getValue().currentOrientation()){
	    			  Track tr = thisTrack;
			    	  for(Boolean bl: switchingOrder){ // for each switch//
			    		  if(!(tr instanceof Switch)) throw new RuntimeException("An invalid Section has been encountered as the section has multiple pieces and a piece is not a switch");
			    		  this.set(((Switch)tr).getSwitchID(), bl);
			    		  tr = thisTrack.getPrevious(bl); // follow track
			    	  }
	    			}
	    			else{
	    			  Track tr = thisTrack;
				      for(Boolean bl: switchingOrder){ // for each switch//
				    	 if(!(tr instanceof Switch)) throw new RuntimeException("An invalid Section has been encountered as the section has multiple pieces and a piece is not a switch");
				    	 this.set(((Switch)tr).getSwitchID(), bl);
				    	 tr = thisTrack.getNext(bl); // follow track
				      }
	    			}
	    			
	    		}
	    		//System.out.println("Section: "+section);
	    		//System.out.println("Track: "+thisTrack);
	    		//System.out.println("TrackSection: "+thisTrack.getSection().getNumber());
	    	   // train has traveled fowards into a section. we check weather the next section is a point or a diamond crossing
/*
	    	    if(thisTrack instanceof ForwardSwitch){
	    	    	//System.out.println("orientation: "+trainOrientation.getValue().currentOrientation());
	    	    	fixPointsFowards(trainOrientation.getKey() ,((ForwardSwitch)thisTrack), sections.get(section),trainOrientation.getValue().currentOrientation());

	    	    } else if (thisTrack instanceof BackSwitch){
	    	    	fixPointsBackwards(trainOrientation.getKey(), ((BackSwitch) thisTrack), sections.get(section), trainOrientation.getValue().currentOrientation());
	    	    } else if (thisTrack instanceof Crossing){
	    	        // there is nothing for us to do in this crossing.
	    	    }
*/


	    	}

       }
	}

	@Override
	public void register(Listener listener) {

		listeners.add(listener);


	}

	@Override
	public boolean start(int trainID, Route route) {
		synchronized(isMoving){
		isMoving.put(trainID, true);
		trainRoutes.put(trainID,route);
		if(trackController == null) throw new RuntimeException("track controller was null in start train");
	    trackController.notify((Event)new Event.SpeedChanged(trainID, Integer.MAX_VALUE));
		return true;
		}


	}

	public boolean resumeTrain(int trainID){
		synchronized(isMoving){
		isMoving.put(trainID, true);
		((Listener) trackController).notify((Event)new Event.SpeedChanged(trainID, Integer.MAX_VALUE));
		return true;
		}

	}

	@Override
	public void stop(int trainID) {
		synchronized(isMoving){
		isMoving.put(trainID, false);
		((Listener) trackController).notify((Event)new Event.EmergencyStop(trainID));
		}
	}


	@Override
	public modelrailway.core.Train train(int trainID) {
		return trainOrientations.get(trainID); // change to notification
	}

	@Override
	/**
	 * Set the switch at the given turnoutId, thrown is true if we are setting the switch to the alternate section.
	 */
	public void set(int turnoutID, boolean thrown) {
		((Listener) trackController).notify(new Event.TurnoutChanged(turnoutID, thrown));

	}
	/**
	 * Takes a section id and returns the route of the first Train object that is currently in that section.
	 * @param section
	 */
	public Map.Entry<Integer,Route> getRoute(int section) {
		for(Map.Entry<Integer, Route> trainRoute : trainRoutes.entrySet()){
			for(Map.Entry<Integer,Train> trainOrientation : trainOrientations.entrySet()){
				if (trainOrientation.getKey() == trainRoute.getKey()) return trainRoute;
			}

		}
		return null; // no train was on the section that we provided.
	}


}
