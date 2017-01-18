package archonmovement;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	int broadcastNum = 0;
	MapLocation[] gardeners = new MapLocation[20];
	static float arcDirection = 4.0f;
	Direction teamDir;
	float lastHealth = 40000;
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
		for(int i = 100; i < 100+20*3; i+=3){
			if(rc.readBroadcast(i) !=0){
				if(i!= 100){
					gardeners[(i-100)/3] =  new MapLocation(rc.readBroadcast(i), rc.readBroadcast(i+1));
				}
			}
		}
		try {

			float hp = rc.getHealth();
			if(lastHealth < hp) {
				rc.broadcast(15, 1);
			}
			lastHealth = hp;
		} catch (GameActionException e) {

		}
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
			float avg_x = 0;
			int num_gardeners = 0;
			float avg_y = 0;

			for(int i = 0; i < gardeners.length ; i++){
				if(gardeners[i]!= null){
					avg_x += gardeners[i].x;
					avg_y += gardeners[i].y;
					num_gardeners++;
				}
			}
			if (num_gardeners > 0) {
				avg_x = avg_x/num_gardeners;
				avg_y = avg_y/num_gardeners;
				MapLocation gardener_target = new MapLocation(avg_x, avg_y);
				if(num_gardeners == 0){
					dir = new Direction(arcDirection);
					if(rc.canMove(dir)) {
						rc.move(dir);
					} else {
						arcDirection = (float) (arcDirection + Math.PI/((Math.random()* 2 + 4)));
					}
				}else{
					try {
						rc.move(gardener_target);
						rc.setIndicatorDot(gardener_target, 1000, 0, 1000);
					} catch(GameActionException e) {
						e.printStackTrace();
					}
					
				}
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}