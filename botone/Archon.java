package botone;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	int broadcastNum = 0;
	MapLocation[] myInitArchonLocations = rc.getInitialArchonLocations(rc.getTeam());
	static float arcDirection = 4.0f;
	Direction teamDir;
	int dirListIndex = -1;
	float lastHealth = 40000;
	MapLocation[] nodes = new MapLocation[6];
	MapLocation[][] treenodes = new MapLocation[6][6];
	public Archon(RobotController rc) {
		super(rc);
	}
	void init() {
		if(rc.getTeam() == Team.A) {
			teamDir = new Direction((float) Math.PI);
		} else {
			teamDir = new Direction(0);
		}
		for(int i = 0; i < 6; i++){
			nodes[i] = new MapLocation(rc.getLocation().x + dirList[i].getDeltaX(RobotType.ARCHON.bodyRadius + RobotType.GARDENER.bodyRadius), rc.getLocation().y + dirList[i].getDeltaY(RobotType.ARCHON.bodyRadius + RobotType.GARDENER.bodyRadius));
		}
	}
	void run() throws GameActionException {
		rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS-5, Float.floatToIntBits(rc.getLocation().x));
		rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS-6, Float.floatToIntBits(rc.getLocation().y));		
		int numGardeners = 0;
		MapLocation myLocation = rc.getLocation();
		for(int i = 100; i < 100+20*4; i+=4){
			numGardeners++;
		}
		try {
			// See if there are any nearby enemy robots
			Team enemy = rc.getTeam().opponent();
			RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
			// If there are some...
			float enemX = rc.readBroadcast(9);
			float enemY = rc.readBroadcast(10);
			if(enemX != 0 && rc.senseNearbyRobots(new MapLocation(enemX, enemY), -1, enemy) == null) {
				rc.broadcast(9, (int) (enemX + Math.random()));
				rc.broadcast(10, (int) (enemY + Math.random()));
			}
			if (robots.length > 0) {
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7, Float.floatToIntBits(robots[0].location.x));
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 8, Float.floatToIntBits(robots[0].location.y));
			} else {
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7, 0);
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 8, 0);
			}
			float hp = rc.getHealth();
		
		} catch (GameActionException e) {

		}
		if(rc.getTeamBullets() > 115) {
			int curRound = rc.getRoundNum();
			if(curRound < 65) {
				if(myInitArchonLocations[0].x == myLocation.x && myInitArchonLocations[0].y == myLocation.y) {
					tryGardener();
				}
			} else {
				if (gardenersMade < 8) {
					tryGardener();	
				} 
			}
			if(dirListIndex != -1) {
			//	MapLocation destination = rc.getLocation().add(dirList[dirListIndex].rotateLeftDegrees(180), 2);
				moveTowards(dirList[dirListIndex]);
			}
		}
		Clock.yield();
	}
	public void moveTowards(Direction dir) throws GameActionException{
		int r_l = 15;
		Direction targetDir = dir;
		if(targetDir == null)
			return;
		for(int i = 0; i < 8; i++){
			if(!rc.hasMoved()) {
				if (rc.canMove(targetDir)){
					rc.move(targetDir);
					System.out.println(rc.hasMoved());
				} else {
					targetDir = targetDir.rotateLeftDegrees(r_l);
				}
			} else {
				break;
			}
		}
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
				int gardenerDirection = (int)(Math.random() * ((5) + 1));
					while(!rc.canHireGardener(dirList[gardenerDirection])) {
						gardenerDirection = (int)(Math.random() * ((5) + 1));
					}
					if(rc.canHireGardener(dirList[gardenerDirection])) {
						System.out.println("gard dir" + gardenerDirection);
						rc.broadcast(420, gardenerDirection);
						rc.hireGardener(dirList[gardenerDirection]);
						moveTowards(rc.getLocation().add(dirList[gardenerDirection].rotateLeftDegrees(180)));
						dirListIndex = gardenerDirection;
						gardenersMade++;
						Clock.yield();
						//}
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
			if (num_gardeners > 2) {
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
					System.out.println("move towards");
				
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}