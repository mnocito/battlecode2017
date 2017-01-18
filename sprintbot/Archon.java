package sprintbot;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	static float arcDirection = 4.0f;
	boolean calculateNewLocation = false;
	Direction teamDir;
	MapLocation[] gardeners = new MapLocation[20];
	public Archon(RobotController rc) {
		super(rc);
		if(rc.getTeam() == Team.A) {
			teamDir = new Direction((float) Math.PI);
		} else {
			teamDir = new Direction(0);
		}
	}

	void run() throws GameActionException {

		if (gardenersMade < 8) {
			arcMove();
			tryGardener();	
		} else {
			goToCorner();
		}
		Clock.yield();
	}
	void goToCorner() throws GameActionException {
		Direction down = new Direction((float) Math.PI/2);
		if(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS -1 ) != 0){
			for(int i = 0; i<20; i++){
				if(gardeners[i] == null){
					gardeners[i] = new MapLocation((float) rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 1),(float) rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 2));
					calculateNewLocation = true;
				}
			}
		}
		if(rc.canMove(down)) {
			try {
				rc.move(down);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(rc.canMove(teamDir)) {
			try {
				rc.move(teamDir);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	void tryGardener() {
		try {
			int curRound = rc.getRoundNum();

			if(curRound % 30 == 0 || curRound < 10) {
				System.out.println("current round: " + curRound);
				for(int i = 0; i < dirList.length; i++) {
					//if(rc.readBroadcast(15) > 0){
					if(rc.canHireGardener(dirList[0])) {
						rc.hireGardener(dirList[0]);
						gardenersMade++;
						Clock.yield();
						//}
					}
				}
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void arcMove() {
		try {	
			dodgeBullets(); // Archons only move to dodge bullets
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}