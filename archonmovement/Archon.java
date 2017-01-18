package archonmovement;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	static float arcDirection = 4.0f;
	Direction teamDir;
	public Archon(RobotController rc) {
		super(rc);
	}
	void init() {
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
	void goToCorner() {
		Direction down = new Direction((float) Math.PI/2);
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
				 BulletInfo[] bullets = rc.senseNearbyBullets();
		            if(bullets.length>0){
		            	Direction newDir = rc.getLocation().directionTo(bullets[0].location).rotateLeftDegrees(90);
		            	Direction newDir2 = rc.getLocation().directionTo(bullets[0].location).rotateLeftDegrees(90);
		            	Direction newDir3 = rc.getLocation().directionTo(bullets[0].location).rotateRightDegrees(90);
		            	if(rc.canMove(newDir)){
		            		rc.move(newDir);
		            		Clock.yield();
		            	}
		            	else if(rc.canMove(newDir2)){
		            		rc.move(newDir2);
		            		Clock.yield();
		            	}
		            	else if(rc.canMove(newDir3)){
		            		rc.move(newDir3);
		            		Clock.yield();
		            	}
		            }
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