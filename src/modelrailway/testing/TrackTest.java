package modelrailway.testing;

import static org.junit.Assert.*;

import java.util.ArrayList;

import modelrailway.simulation.Crossing;
import modelrailway.simulation.Locomotive;
import modelrailway.simulation.BackSwitch;
import modelrailway.simulation.Movable;
import modelrailway.simulation.RollingStock;
import modelrailway.simulation.Straight;
import modelrailway.simulation.ForwardSwitch;
import modelrailway.simulation.Track;
import modelrailway.simulation.Section;
import modelrailway.simulation.Train;

import org.junit.*;

public class TrackTest {
	/**
	 * Assert that we can build a straight ring of 3 track pieces.
	 */
	@Test public void  testTrackBuild0(){
		Section sec = new Section(new ArrayList<Track>());

		Straight st = new Straight(null, null, sec, 100);
		Straight.StraightRing route = new Straight.StraightRing(st);
		Track head = route.ringTrack(3,100); // produce a ring track with 3 track pieces and each track piece has length 100
		sec.add(head);
		Track tp_1 = head.getNext(false);
		Track tp_2 = tp_1.getNext(false);
		Track tp_3 = tp_2.getNext(false);

		if(head != tp_3){
			fail("wrong length");
		}
		assertTrue("check that we have different pieces in the ring",head != tp_1 && tp_1 != tp_2 && tp_2 != tp_3);

		assertTrue(tp_3.getPrevious(false) == tp_2);
		assertTrue(tp_2.getPrevious(false) == tp_1);
		assertTrue(tp_1.getPrevious(false) == head);
	}

	/**
	 * Assert that we can build a straight ring of 2 track pieces
	 */
	@Test public void testTrackBuild1(){
     	Section sec = new Section(new ArrayList<Track>());

		Straight st = new Straight(null, null, sec, 100);
		Straight.StraightRing route = new Straight.StraightRing(st);
		Track head = route.ringTrack(2,100); // produce a ring track with 3 track pieces and each track piece has length 100
		sec.add(head);
		Track tp_1 = head.getNext(false);
		Track tp_2 = tp_1.getNext(false);

		if(head != tp_2){
			fail("wrong length");
		}
		assertTrue("check that we have different pieces in the ring",head != tp_1 && tp_1 != tp_2);
		assertTrue(tp_2.getPrevious(false) == tp_1);
		assertTrue(tp_1.getPrevious(false) == head);
	}
	/**
	 * Assert that we can build a straight ring of 4 track pieces.
	 */
	@Test public void testTrackBuild2(){
        Section sec = new Section(new ArrayList<Track>());

		Straight st = new Straight(null, null, sec, 100);
		Straight.StraightRing route = new Straight.StraightRing(st);
		Track head = route.ringTrack(4,100); // produce a ring track with 3 track pieces and each track piece has length 100
		sec.add(head);
		Track tp_1 = head.getNext(false);
		Track tp_2 = tp_1.getNext(false);
		Track tp_3 = tp_2.getNext(false);
		Track tp_4 = tp_3.getNext(false);
		if(head != tp_4){
			fail("wrong length");
		}
		assertTrue("check that we have different pieces in the ring",head != tp_1 && tp_1 != tp_2 && tp_2 != tp_3 && tp_3 != tp_4);

		assertTrue(tp_4.getPrevious(false) == tp_3);
		assertTrue(tp_3.getPrevious(false) == tp_2);
		assertTrue(tp_2.getPrevious(false) == tp_1);
		assertTrue(tp_1.getPrevious(false) == head);
	}
	/**
	 * Create a ring track with an alternate branch to the main ring which leaves and rejoins the main ring.
	 *
	 */
	@Test public void testTrackBuild3(){
		Section sec = new Section(new ArrayList<Track>());

		Straight st = new Straight(null,null,sec,100);
		Straight.StraightRing route = new Straight.StraightRing(st);
		Track head = route.ringTrack(4, 100);
		sec.add(head);
		Track tp_1 = head.getNext(false);
		Track tp_2 = tp_1.getNext(false);
		Track tp_3 = tp_2.getNext(false);
		Track tp_4 = tp_3.getNext(false);

		Section sec2 = new Section(new ArrayList<Track>());
		Track sw = new ForwardSwitch(null, null, null, sec2, 100 , 100 , 50 ) ; // points are crossed at 50

		Section sec3 = new Section(new ArrayList<Track>());
		Track sw2 = new BackSwitch(null, null, sw, sec3, 100, 100, 50);

		route.replace(tp_1, sw, false);
		route.replace(tp_3, sw2, false);
		//System.out.println("sw.getNext(false) : "+sw.getNext(false));
		//System.out.println("sw.getNext(true) : " + sw.getNext(true));

		Section sec4 = new Section(new ArrayList<Track>());
		Straight str = new Straight(null, null, sec4,100 );

		route.insertBetween(sw, true, sw2, true,  str, false);
		//System.out.println("sw.getNext(false) : "+sw.getNext(false));
		//System.out.println("sw.getNext(true) : " + sw.getNext(true));

		assertTrue (head.getNext(false) == sw);
		assertTrue (sw.getNext(false) == tp_2);
		assertTrue (tp_2.getNext(false) == sw2);
		assertTrue (sw2.getNext(false) == tp_4);
	    assertTrue (tp_4 == head);

	    assertTrue("check that we have different pieces in the ring",head != sw && sw != tp_2 && tp_2 != sw2 && sw2!= tp_4);

	    assertTrue (sw.getNext(true) == str);
	    assertTrue (str.getPrevious(false) == sw);
	    assertTrue (str.getNext(false) == sw2);
	    assertTrue (sw2.getPrevious(true) == str);
	}

	@Test public void testTrackBuild4(){
		Section sec = new Section(new ArrayList<Track>());

		Straight st = new Straight(null,null,sec,100);
		Straight.StraightRing route = new Straight.StraightRing(st);
		Track head = route.ringTrack(5, 100);
		sec.add(head);
		Track tp_1 = head.getNext(false);
		Track tp_2 = tp_1.getNext(false);
		Track tp_3 = tp_2.getNext(false);
		Track tp_4 = tp_3.getNext(false);
		Track tp_5 = tp_4.getNext(false);

		Section sec2 = new Section(new ArrayList<Track>());
		Track sw = new ForwardSwitch(null, null, null, sec2, 100 , 100 , 50 ) ; // points are crossed at 50

		Section sec3 = new Section(new ArrayList<Track>());
		Track sw2 = new BackSwitch(null, null, sw, sec3, 100, 100, 50);

		route.replace(tp_1, sw, false);
		route.replace(tp_3, sw2, false);
		//System.out.println("sw.getNext(false) : "+sw.getNext(false));
		//System.out.println("sw.getNext(true) : " + sw.getNext(true));

		Section sec4 = new Section(new ArrayList<Track>());
		Straight str = new Straight(null, null, sec4,100 );

		route.insertBetween(sw, true, sw2, true,  str, false);
	//	System.out.println("sw.getNext(false) : "+sw.getNext(false));
	//	System.out.println("sw.getNext(true) : " + sw.getNext(true));

		assertTrue (head.getNext(false) == sw);
		assertTrue (sw.getNext(false) == tp_2);
		assertTrue (tp_2.getNext(false) == sw2);
		assertTrue (sw2.getNext(false) == tp_4);
		assertTrue (tp_4.getNext(false) == tp_5);
	    assertTrue (tp_5 == head);

	    assertTrue("check that we have different pieces in the ring",head != sw && sw != tp_2 && tp_2 != sw2 && sw2!= tp_4);

	    assertTrue (sw.getNext(true) == str);
	    assertTrue (str.getPrevious(false) == sw);
	    assertTrue (str.getNext(false) == sw2);
	    assertTrue (sw2.getPrevious(true) == str);
	}
	/**
	 * Test adding a diamond crossing;
	 */
	@Test public void testLocoDiamond0(){
		Section sec = new Section (new ArrayList<Track>());
		Straight st = new Straight(null,null,sec,100);
		Straight.StraightRing route = new Straight.StraightRing(st);

		//create a ring track with 3 track sections.
		Track head1 = route.ringTrack(3,100);
		Track firstr1 = head1.getNext(false);
		Track firstr2 = head1.getNext(false).getNext(false);

		Section sec2 = new Section(new ArrayList<Track>());
		Straight st2 = new Straight(null,null,sec2,100);
		Straight.StraightRing route2 = new Straight.StraightRing(st2);

		// create another ring track with 3 track sections.
		Track head2 = route2.ringTrack(3, 100);
		Track secr1 = head2.getNext(false);
		Track secr2 = head2.getNext(false).getNext(false);


		Section crMain = new Section(new ArrayList<Track>());
		Section crAlt = new Section(new ArrayList<Track>());
		Crossing cr = new Crossing(null,null,null,null,crMain,crAlt,100,100);


		// insert the diamond crossing.
		route.insertBetween(head1, false, cr, false);
		route2.insertBetween(head2, false, cr, true);

		// create a locomotive

		Movable loco = new Locomotive(new Track[]{head1,head1}, 50, 50, 40, false); // tracks, dist, length, maxspeed, onAlt

		loco.start();

		loco.move();
		assertTrue(loco.getDistance() == 90);
		assertTrue(loco.getFront() == head1);
		assertTrue(loco.getBack() == head1);

		loco.move();
		assertTrue(loco.getDistance() == 30);
		assertTrue(loco.getFront() == cr);
		assertTrue(loco.getBack() == head1);

		loco.move();
		assertTrue(loco.getDistance() == 70);
		assertTrue(loco.getFront() == cr);
		assertTrue(loco.getBack() == cr);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 10);
		assertTrue(loco.getFront() == firstr1);
		assertTrue(loco.getBack() == cr);
		assertTrue(loco.getOnAlt() ==  false);

		loco.move();
		assertTrue(loco.getDistance() == 50);
		assertTrue(loco.getFront() == firstr1);
		assertTrue(loco.getBack() == firstr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 90);
		assertTrue(loco.getFront() == firstr1);
		assertTrue(loco.getBack() == firstr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 30);
		//System.out.println("getFront(): "+loco.getFront()+" firstr2: "+firstr2+" firstr1: "+firstr1);
		assertTrue(loco.getFront() == firstr2);
		assertTrue(loco.getBack() == firstr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 70);
		assertTrue(loco.getFront() == firstr2);
		assertTrue(loco.getBack() == firstr2);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 10);
		assertTrue(loco.getFront() == head1);
		assertTrue(loco.getBack() == firstr2);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 50);
		assertTrue(loco.getFront() == head1);
		assertTrue(loco.getBack() == head1);
		assertTrue(loco.getOnAlt() == false);

	}

	@Test public void testLocoDiamond1(){
		Section sec = new Section (new ArrayList<Track>());
		Straight st = new Straight(null,null,sec,100);
		Straight.StraightRing route = new Straight.StraightRing(st);

		//create a ring track with 3 track sections.
		Track head1 = route.ringTrack(3,100);
		Track firstr1 = head1.getNext(false);
		Track firstr2 = head1.getNext(false).getNext(false);

		Section sec2 = new Section(new ArrayList<Track>());
		Straight st2 = new Straight(null,null,sec2,100);
		Straight.StraightRing route2 = new Straight.StraightRing(st2);

		// create another ring track with 3 track sections.
		Track head2 = route2.ringTrack(3, 100);
		Track secr1 = head2.getNext(false);
		Track secr2 = head2.getNext(false).getNext(false);


		Section crMain = new Section(new ArrayList<Track>());
		Section crAlt = new Section(new ArrayList<Track>());
		Crossing cr = new Crossing(null,null,null,null,crMain,crAlt,100,100);


		// insert the diamond crossing.
		route.insertBetween(head1, false, cr, false);
		route2.insertBetween(head2, false, cr, true);

		//System.out.println("head2: "+head2 +" cr: "+cr+" cr.getNext(true): "+cr.getNext(true) );

		// create a locomotive

		Movable loco = new Locomotive(new Track[]{head2,head2}, 50, 50, 40, false); // tracks, dist, length, maxspeed, onAlt

		loco.start();

		loco.move();
		assertTrue(loco.getDistance() == 90);
		assertTrue(loco.getFront() == head2);
		assertTrue(loco.getBack() == head2);

		loco.move();
		assertTrue(loco.getDistance() == 30);
		assertTrue(loco.getFront() == cr);
		assertTrue(loco.getBack() == head2);

		loco.move();
		assertTrue(loco.getDistance() == 70);
		assertTrue(loco.getFront() == cr);
		assertTrue(loco.getBack() == cr);
		assertTrue(loco.getOnAlt() == true);

		loco.move();
		assertTrue(loco.getDistance() == 10);
	//	System.out.println("loco.getFront():" + loco.getFront());
		assertTrue(loco.getFront() == secr1);
		assertTrue(loco.getBack() == cr);
		assertTrue(loco.getOnAlt() ==  false);

		loco.move();
		assertTrue(loco.getDistance() == 50);
		assertTrue(loco.getFront() == secr1);
		assertTrue(loco.getBack() == secr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 90);
		assertTrue(loco.getFront() == secr1);
		assertTrue(loco.getBack() == secr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 30);
		//System.out.println("getFront(): "+loco.getFront()+" firstr2: "+firstr2+" firstr1: "+firstr1);
		assertTrue(loco.getFront() == secr2);
		assertTrue(loco.getBack() == secr1);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 70);
		assertTrue(loco.getFront() == secr2);
		assertTrue(loco.getBack() == secr2);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 10);
		assertTrue(loco.getFront() == head2);
		assertTrue(loco.getBack() == secr2);
		assertTrue(loco.getOnAlt() == false);

		loco.move();
		assertTrue(loco.getDistance() == 50);
		assertTrue(loco.getFront() == head2);
		assertTrue(loco.getBack() == head2);
		assertTrue(loco.getOnAlt() == false);

	}

	@Test public void testLocoDiamond2(){


	}

}