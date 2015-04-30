package modelrailway.testing;

import java.util.Map;

import modelrailway.core.Controller;
import modelrailway.core.Event;
import modelrailway.core.Route;
import modelrailway.core.Section;
import modelrailway.simulation.Track;
import modelrailway.simulation.Train;

public class TestControlerCollision extends TestController implements Controller{

	public TestControlerCollision(Map<Integer, modelrailway.core.Train> trains,
			Map<Integer, Section> sections, Track head,
			Controller trackController) {
		super(trains, sections, head, trackController);
		// TODO Auto-generated constructor stub
	}

	public void notify(Event e){
		super.notify(tryLocking(e));
	}
	/**
	 * Try to obtain the lock for the section ahead of where we are traveling into if we are traveling into a section.
	 * @param e
	 * @return
	 */
	private Event tryLocking(Event e){

		if((e instanceof Event.SectionChanged) && ((Event.SectionChanged) e).getInto()){ // when we are moving into a section
			//System.out.println("sectionChanged: "+((Event.SectionChanged)e).getSection());
			Map.Entry<Integer, Route> entry = super.getRoute(((Event.SectionChanged)e).getSection()); //


			Integer nextSec = entry.getValue().nextSection(((Event.SectionChanged) e).getSection());  // get section number the train changed into.
			Integer train = entry.getKey(); // get the train
			Route trainRoute = entry.getValue(); // get the route that the train has planned.



			if(this.trainOrientations().get(train).currentOrientation() == true){
				Section thisSec = this.sections().get(this.trainOrientations().get(train).currentSection());

				Track front = thisSec.get(0);
				Track notAltNext = front.getNext(false);
				Track altNext = front.getNext(true);
				boolean reserved = false ;
				//System.out.println("reserve sections: notalt: "+ notAltNext.getSection().getNumber() + " alt: "+altNext.getSection().getNumber() +" section: "+ nextSec);
				//System.out.println("nextSec: "+nextSec);
				//System.out.println("notAltNext: "+notAltNext.getSection().getNumber());
				//System.out.println("altNext: "+altNext.getSection().getNumber());
				//System.out.println("thisSec: "+thisSec.getNumber());
				if(notAltNext.getSection().getNumber() == nextSec){
					//System.out.println("notalt number: "+notAltNext.getSection().getNumber());
					reserved = notAltNext.getSection().reserveSection(train);

				}else  if(notAltNext.getAltSection() != null && notAltNext.getAltSection().getNumber() == nextSec){

					reserved = notAltNext.getAltSection().reserveSection(train);

				}else if(altNext.getSection().getNumber() == nextSec){
					//System.out.println();
					reserved = altNext.getSection().reserveSection(train);

				}else if(altNext.getAltSection() != null && altNext.getAltSection().getNumber() == nextSec){
					reserved = altNext.getAltSection().reserveSection(train);
				}
				//System.out.println("reserved: "+reserved);
				if(reserved == false){ // we need to trigger an emergency stop
					this.stop(train);
					super.notify(new Event.EmergencyStop(train));
				}
			}
			else{
				Section thisSec = this.sections().get(this.trainOrientations().get(train).currentSection());
				Track back = thisSec.get(0); // length is not supported.
				Track notAltPrev = back.getPrevious(false);
				Track altPrev = back.getPrevious(true);
				boolean reserved = false ;
				//System.out.println("backend: ");
				if(notAltPrev.getSection().getNumber() == nextSec){
					reserved = notAltPrev.getSection().reserveSection(train);

				} if(notAltPrev.getAltSection() != null && notAltPrev.getAltSection().getNumber() == nextSec){
					reserved =notAltPrev.getAltSection().reserveSection(train);

				} if(altPrev.getSection().getNumber() == nextSec){
					reserved = altPrev.getSection().reserveSection(train);

				} if(altPrev.getAltSection() != null && altPrev.getAltSection().getNumber() == nextSec){
					reserved = altPrev.getAltSection().reserveSection(train);
				}

				if(reserved == false){
					this.stop(train);
					super.notify(new Event.EmergencyStop(train));
				}
			}
		}
		return e;
	}

}
