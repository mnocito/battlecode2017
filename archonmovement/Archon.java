package archonmovement;

import battlecode.common.*;

public class Archon extends BaseRobot {
	int gardenersMade = 0;
	int broadcastNum = 0;
	Team myTeam = rc.getTeam();
	MapLocation[] myInitArchonLocations = rc.getInitialArchonLocations(myTeam);
	MapLocation initialEnemyArchon = rc.getInitialArchonLocations(myTeam.opponent())[0];
	static float arcDirection = 4.0f;
	Direction teamDir;
	int dirListIndex = -1;
	int numGardeners = 0;
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
		MapLocation myLocation = rc.getLocation();
		for(int i = 100; i < 100+20*4; i+=4){
			numGardeners++;
		}
		try {

			RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
			RobotInfo robot = null;
			MapLocation lastSpottedEnemy = null;
			if (robots.length > 0) {
				robot = robots[0];
				lastSpottedEnemy = new MapLocation(robots[0].getLocation().x, robots[0].getLocation().y);
				if(rc.getLocation().distanceTo(lastSpottedEnemy) > rc.getLocation().distanceTo(new MapLocation(rc.readBroadcast(9), rc.readBroadcast(10)))){
					rc.broadcast(9, (int)robots[0].getLocation().x);
					rc.broadcast(10, (int)robots[0].getLocation().y);
					rc.broadcast(11, (int)robots[0].getID());//target ID
					rc.broadcast(12, (int)robots[0].health);
				}
			} else if(rc.readBroadcast(9) != 0) {
				lastSpottedEnemy = new MapLocation(rc.readBroadcast(9), rc.readBroadcast(10));
			}
			// See if there are any nearby enemy robots
			Team enemy = rc.getTeam().opponent();
			// If there are some...

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
		if(rc.getTeamBullets() > 99) {
			if(dirListIndex != -1) {
				//	MapLocation destination = rc.getLocation().add(dirList[dirListIndex].rotateLeftDegrees(180), 2);
				moveTowards(dirList[dirListIndex]);
			}
			int curRound = rc.getRoundNum();
			if(curRound < 65) {
				TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
				if(myInitArchonLocations[0].x == myLocation.x && myInitArchonLocations[0].y == myLocation.y) {
					tryGardener();
					if(rc.senseNearbyTrees().length > 0) {
						for(TreeInfo tree : trees) {
							if(myLocation.distanceTo(tree.location) < RobotType.ARCHON.bodyRadius + RobotType.GARDENER.bodyRadius*2) {
								rc.setIndicatorDot(tree.location, 255, 255, 255);
							}
						}
					}
				}
			} else if(curRound % 30 == 0){
				if (gardenersMade < 8) {
					tryGardener();	
				} 
			}
		}
		numGardeners = 0;
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
				int gardenerDirection = -1;
				if(curRound < 10) {
					gardenerDirection = getClosestDirection();
				}
				System.out.println("current round: " + curRound);
				if(gardenerDirection == -1) {
					gardenerDirection = (int)(Math.random() * ((5) + 1));
					while(!rc.canHireGardener(dirList[gardenerDirection])) {
						gardenerDirection = (int)(Math.random() * ((5) + 1));
					}
				}
				if(rc.canHireGardener(dirList[gardenerDirection])) {
					
					System.out.println("gard dir" + gardenerDirection);
					rc.broadcast(420, gardenerDirection);
					rc.hireGardener(dirList[gardenerDirection]);
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
	int getClosestDirection() {
		int index = -1;
		float lowestDist = 400;
		if(dirList[0].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 0;
			lowestDist = Math.abs(dirList[0].radians - rc.getLocation().directionTo(initialEnemyArchon).radians);
		}
		if(dirList[1].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 1;
			lowestDist = Math.abs(dirList[1].radians - rc.getLocation().directionTo(initialEnemyArchon).radians);
		}
		if(dirList[2].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 2;
			lowestDist = Math.abs(dirList[2].radians - rc.getLocation().directionTo(initialEnemyArchon).radians);
		}
		if(dirList[3].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 3;
			lowestDist = Math.abs(dirList[3].radians - rc.getLocation().directionTo(initialEnemyArchon).radians);
		}
		if(dirList[4].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 4;
			lowestDist = Math.abs(dirList[4].radians - rc.getLocation().directionTo(initialEnemyArchon).radians);
		}
		if(dirList[5].radians - rc.getLocation().directionTo(initialEnemyArchon).radians < lowestDist) {
			index = 5;
		}
		System.out.println("index: " + index);
		if(rc.canHireGardener(dirList[index])) {
			return index;
		} 
		return -1;
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