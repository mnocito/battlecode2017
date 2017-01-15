package archonmovement;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	static float arcDirection = 4.0f;
	public Archon(RobotController rc) {
		super(rc);
	}
	void init() {
		
	}
	void run() {
		if (gardenersMade < 10) {
			arcMove();
			tryGardener();	
		} else {
			goToCorner();
		}
		Clock.yield();
	}
	void goToCorner() {
		Direction down = new Direction((float) Math.PI/2);
		if(rc.canMove(down)) {
			try {
				rc.move(down);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	void tryGardener() {
		try {
			int curRound = rc.getRoundNum();
			if(curRound % 30 == 0) {
				System.out.println("current round: " + curRound);
				Direction dir = new Direction(0);
				if(rc.canHireGardener(dir)) {
					rc.hireGardener(dir);
					gardenersMade++;
				}
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void arcMove() {
			try {		
				Direction dir;
				dir = new Direction(arcDirection);
				if(rc.canMove(dir)) {
					rc.move(dir);
				} else {
					arcDirection = (float) (arcDirection + Math.PI/((Math.random()* 2 + 4)));
				}
			} catch (GameActionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	}
}
