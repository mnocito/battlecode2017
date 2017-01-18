package archonmovement;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	int broadcastNum = 0;
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
	public void moveTowards(MapLocation loc1) throws GameActionException{
		int r_l = 15;
		Direction targetDir = rc.getLocation().directionTo(loc1);
		for(int i = 0; i < 8; i++){
			if(rc.hasMoved() == false && rc.canMove(targetDir)){
				rc.move(targetDir);
				rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
				System.out.println(rc.hasMoved());
			}else{
				targetDir = targetDir.rotateLeftDegrees(r_l);
			}
		}
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
			for(int i = 100; i < 100+20*4; i+=4){
				if(rc.readBroadcast(i) !=0) {
					avg_x += rc.readBroadcast(i);
					avg_y += rc.readBroadcast(i+1);
					
					num_gardeners++;
				}
			}
			System.out.println("gardener number: "+num_gardeners);
			if (num_gardeners > 0) {
				avg_x = avg_x/num_gardeners;
				System.out.println("avg x " + avg_x);
				avg_y = avg_y/num_gardeners;
				System.out.println("avg y " + avg_y);
				MapLocation gardener_target = new MapLocation(avg_x, avg_y);
				rc.setIndicatorDot(gardener_target, 0 , 0, 1000);
				System.out.println(gardener_target);

					try {
						moveTowards(gardener_target);
						rc.setIndicatorDot(gardener_target, 1000, 0, 1000);
					} catch(GameActionException e) {
						e.printStackTrace();
					}
					moveTowards(gardener_target);
					System.out.println("move towards");
				
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}