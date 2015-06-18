package modelrailway.util;

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
/**
 * This test controller has no collision detection but just tests to make sure that a train can follow a route. in order to test the train simulator.
 * @author powleybenj
 *
 */
public class MovementController implements Controller, Listener {

	private Event.Listener trackController = null;
	private List<Listener> listeners = new ArrayList<Listener>();
	private Map<Integer,Route> trainRoutes = new ConcurrentHashMap<Integer,Route>();
	private Map<Integer,modelrailway.core.Train> trainOrientations ;
	private Map<Integer,Boolean> isMoving = new ConcurrentHashMap<Integer,Boolean>();
	private Map<Integer, Section> sections;
	private Track head;
	public class AlreadyHere extends RuntimeException{

	}
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
	public MovementController( Map<Integer,modelrailway.core.Train> orientations, Map<Integer, Section> intSecMap , Track head, Event.Listener trackController){
		this.trackController = trackController;
		trainOrientations = orientations; // are the trains going backwards or fowards.
		this.sections = intSecMap;
		this.head = head;
	}
	
	public Integer calculateSectionNumber(Event.SectionChanged ev){
		return 1 + ((((Event.SectionChanged)ev).getSection()-1) * 2);
	}

	public Train adjustSection(Event e){
		synchronized(isMoving){
			Pair<Integer,Train> pair = this.sectionTrainMovesInto((Event.SectionChanged)e);
			if(pair == null) throw new AlreadyHere();
			pair.snd.setSection(pair.fst);
			return pair.snd;
	    }
	}
	@Override
	public void notify(Event e) {
		try{
		  if(e instanceof Event.SectionChanged ){ // when there is a section change into another section
			     Train tr = adjustSection(e);
			     System.out.println("section has been adusted");
		         moveIntoSection(e,tr );
		  }
		  for(Listener l : listeners){

			  l.notify(e);
		  }
		}catch(AlreadyHere ex){}
	}

	public Pair<Track,Track> getCurrentTrackSection(Section currentSection, Section previousSection, boolean movingForward){
		Track prev = null;
		Track curr = null;
		if(currentSection == null) throw new RuntimeException("The section was null");
		if(currentSection.size() == 0) throw new RuntimeException("No sections for track: ");
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

	public Pair<Integer,Train> sectionTrainMovesInto(Event.SectionChanged e){
		Integer eventsectionID =  this.calculateSectionNumber((Event.SectionChanged) e);
		
		if(((Event.SectionChanged) e).getInto()){ // if we are moving into a section, 
			for(Map.Entry<Integer, Train> tr: trainOrientations.entrySet()){ // go through all the trains
			    Train trainObj = tr.getValue(); // for each train
			    if(isMoving.get(tr.getKey()) != null && isMoving.get(tr.getKey())){ // if the train is moving
			    	System.out.println("found a moving train");
			    	System.out.println("find the next section is: "+trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection()));
			    	System.out.println("The eventsectionID: "+eventsectionID);
			    	System.out.println("currentSection: "+trainObj.currentSection());
			    	System.out.println("routes: "+this.trainRoutes);
			    	System.out.println("e.getSection(): "+((Event.SectionChanged) e).getSection());
			    	if(trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection()) == eventsectionID){ 
			    		return new Pair<Integer,Train>(eventsectionID, trainObj);
			    	}
			    }
			}
		}
		else {
			for(Map.Entry<Integer, modelrailway.core.Train> tr : trainOrientations.entrySet()){
				   Train trainObj = tr.getValue();
				   System.out.println("trainObj: "+tr.getKey());
				   if(isMoving.get(tr.getKey()) != null && isMoving.get(tr.getKey())){
					   System.out.println("trainObjMoving: "+tr.getValue());
					   System.out.println("currentSection: "+trainObj.currentSection()+"SectionID: "+eventsectionID);
					   if(trainObj.currentSection() == eventsectionID){
						    Integer nextOne = trainRoutes.get(tr.getKey()).nextSection(trainObj.currentSection());
					     	eventsectionID = nextOne;
					     	return new Pair<Integer,Train>(eventsectionID,trainObj);
					   }
				   }
			}
		}
		return null;
	}
	
	/**
	 * We have moved into a section. Do what is necissary to configure the section that we have just moved into.
	 * note find correct train does not support diamond crossing.
	 * @param e
	 * @return
	 */
	private void moveIntoSection(Event e, Train train){
		Integer eventsectionID = train.currentSection();
		for(Entry<Integer, Train> trainOrientation: trainOrientations.entrySet()){ // for all the trains on the track

			Integer section = trainOrientation.getValue().currentSection();
			//System.out.println("Section: "+section);
			//System.out.println("e.getSection(): "+((Event.SectionChanged) e).getSection()+" section: "+section);
	    	if(section ==  eventsectionID){ // check that the front of the train is in the section
	    		// first work out which track segments we are currently dealing with.

	    		Integer prevSection = trainRoutes.get(trainOrientation.getKey()).prevSection(section); // the previous section that we came from
	    		Integer nextSection = trainRoutes.get(trainOrientation.getKey()).nextSection(section);  // next section.
	    		Section previous = sections.get(prevSection);

	    		Integer trainSection = eventsectionID;//((Event.SectionChanged) e).getSection(); // store the section number in a variable
	    		//System.out.println("try get current track: "+trainSection +" prevSection: "+prevSection);
	    		Track thisTrack = null;
	    		System.out.println("trainOrientation.getValue().currentOrientation(): "+trainOrientation.getValue().currentOrientation());
	    		if(trainOrientation.getValue().currentOrientation()){ // if the orientation is facing backwards
	    		//Section currSec =  sections.get(trainSection);
	    		//System.out.println("sections: "+sections);
	    		Pair<Track,Track> prevCurrPair = getCurrentTrackSection(sections.get(trainSection),sections.get(prevSection),trainOrientation.getValue().currentOrientation());
	    		// before handling the switches make sure that the sections of the track piece that we came from do not have this train in any of them
	    		thisTrack = prevCurrPair.snd;
	    		}
	    		else{ // if the orientation is facing foward
	    		Pair<Track,Track> nextCurrPair = getCurrentTrackSection(sections.get(trainSection),sections.get(nextSection),trainOrientation.getValue().currentOrientation());
		    		// before handling the switches make sure that the sections of the track piece that we came from do not have this train in any of them
		    	thisTrack = nextCurrPair.snd;
	    		}
	    		System.out.println("thisTrackNumber: "+ thisTrack.getSection().getNumber());

	    		// for all tracks in the previous section, remove this train from the queue of trains that have requested the section, instruct the next train on the queue to go.
	    		for(Track t :previous){
	    			boolean trainMoved = false;
	    			Section tracSec = t.getSection();
	    			Section trackAltSec = t.getAltSection();
	    			if(tracSec != null){
	    				Pair<Boolean,Integer> pair = null;
	    				if(!tracSec.isQueueEmpty() && trainOrientation.getKey() != null) pair = tracSec.removeFromQueue(trainOrientation.getKey());
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
	    			if((trackAltSec != null) && (!trackAltSec.isQueueEmpty()) && (trainOrientation.getKey() != null)){
	    				//System.out.println("trackAltSec.isQueueEmpty: "+trackAltSec.isQueueEmpty()+" track: "+trackAltSec.getNumber());
	    				pair = trackAltSec.removeFromQueue(trainOrientation.getKey());
	    			}
	    			if(pair != null && pair.snd != null){
	    			   if(pair.fst){
	    				   if(!trainMoved) {
	    					  // if(pair.snd == null) throw new RuntimeException("trackAltSec Number: "+trackAltSec.getNumber()+" peep at altsec: "+trackAltSec.getEntryRequests().toString()+"trackAltSec: "+trackAltSec.getNumber());
	    					   this.resumeTrain(pair.snd);
	    					   trainMoved = true;
	    				   }
	    			   }
	    			}
	    		}


	    		//System.out.println("Switch"+thisTrack);
	    		//System.out.println("trackSection: "+thisTrack.getSection().getNumber());

	    		if(thisTrack instanceof Switch){ // move switches

	    			Integer trainID = trainOrientation.getKey();
	    			Route rt = trainRoutes.get(trainID);
	    			Section sectionS = sections.get(section);
	    		    Integer nextSec = rt.nextSection(section);
	    			Integer prevSec = rt.prevSection(section); // the section we came from
	    			Pair<Integer, Integer> sectionPair = new Pair<Integer,Integer>(prevSec,nextSec);
	    		    // for sectionPair
	    		    List<Boolean> switchingOrder = sectionS.retrieveSwitchingOrder(sectionPair);
	    		    System.out.println("Section,previous: "+ previous.getNumber());
	    		    System.out.println("sectionS: "+sectionS.getNumber());
	    		    System.out.println("sectionPair: "+sectionPair.fst + ", "+sectionPair.snd);
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
				    	 System.out.println("trSwitch ID: "+((Switch) tr).getSwitchID());
				    	
				    	 this.set(((Switch)tr).getSwitchID(), bl);
				    	 tr = thisTrack.getNext(bl); // follow track
				      }
	    			}
	    		}
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
	    trackController.notify((Event)new Event.SpeedChanged(trainID, 6));
		return true;
		}


	}

	public boolean resumeTrain(int trainID){
		synchronized(isMoving){
		isMoving.put(trainID, true);
		((Listener) trackController).notify((Event)new Event.SpeedChanged(trainID, 6));
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
	public Map.Entry<Integer,Route> getRoute(int section){
		//System.out.println("routes: "+trainRoutes.size() + " routes: "+trainRoutes.toString());
		//System.out.println("orientations: "+trainOrientations.size()+" orientations: "+trainOrientations.toString());
		for(Map.Entry<Integer, Route> trainRoute : trainRoutes.entrySet()){
			for(Map.Entry<Integer,Train> trainOrientation : trainOrientations.entrySet()){
				//System.out.println("testTrain: "+ trainOrientation.getKey() +" trainRouteKey: "+trainRoute.getKey());
				if (trainOrientation.getKey() == trainRoute.getKey()) return trainRoute;
			}

		}
		return null; // no train was on the section that we provided.
	}


}
