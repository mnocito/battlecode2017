package archonmovement;
import java.util.Arrays;
import java.util.Comparator;

import battlecode.common.*;

public class Gardener extends BaseRobot {
	int[] amts = new int[4];
	// 0: scouts, 1: lumberjacks, 2: soldiers, 3: tanks
	static int channel = -1;
	Direction moveDirection = null;
	int roundsExisted = 0;
	int addedNum = 0;
	boolean movement = true;
	int totalTrees = 0;
	int roundsNotMoved = 0;
	static float gardDirection = 4.0f;
	MapLocation[] nodes = new MapLocation[6];
	MapLocation[] findSpaces = new MapLocation[16];
	public Gardener(RobotController rc) {
		super(rc);
	}
	void init() {
		initDirList(rc.getTeam());
		amts[0] = 0;
		amts[1] = 0;
		amts[2] = 0;
		amts[3] = 0;
	}
	void run() throws GameActionException {
		float teamBullets = rc.getTeamBullets();
		roundsExisted++;
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		if(robots.length > 0){
			rc.broadcast(50, (int)robots[0].getLocation().x);
			if (rc.readBroadcast(11) != robots[0].getID()) {
				rc.broadcast(9, (int) robots[0].getLocation().x);
				rc.broadcast(10, (int) robots[0].getLocation().y);
				rc.broadcast(11, (int) robots[0].getID());// target ID
				rc.broadcast(12, (int) robots[0].health);
			}
		}
		if(teamBullets > 140) {
			spawnRobots();
		}
		if (movement) {
			gardenerMove();
		} else {
			spawnStuff();
		}
		Clock.yield();
	}
	void findSpaces() {
	}
	void gardenerMove() throws GameActionException{
		System.out.println("running");
		boolean first, second, third, fourth, fifth, sixth;
		for(int i = 0; i < 6; i++){
			nodes[i] = new MapLocation(rc.getLocation().x + dirList[i].getDeltaX(RobotType.GARDENER.bodyRadius + GameConstants.BULLET_TREE_RADIUS), rc.getLocation().y + dirList[i].getDeltaY(RobotType.GARDENER.bodyRadius + GameConstants.BULLET_TREE_RADIUS));
			rc.setIndicatorDot(nodes[i], 30, 30, 30);
		}
		first = (!rc.isCircleOccupiedExceptByThisRobot(nodes[0], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[0], GameConstants.BULLET_TREE_RADIUS));
		second = (!rc.isCircleOccupiedExceptByThisRobot(nodes[1], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[1], GameConstants.BULLET_TREE_RADIUS));
		third = (!rc.isCircleOccupiedExceptByThisRobot(nodes[2], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[2], GameConstants.BULLET_TREE_RADIUS));
		fourth = (!rc.isCircleOccupiedExceptByThisRobot(nodes[3], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[3], GameConstants.BULLET_TREE_RADIUS));
		fifth = (!rc.isCircleOccupiedExceptByThisRobot(nodes[4], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[4], GameConstants.BULLET_TREE_RADIUS));
		sixth = (!rc.isCircleOccupiedExceptByThisRobot(nodes[5], GameConstants.BULLET_TREE_RADIUS) && rc.onTheMap(nodes[5], GameConstants.BULLET_TREE_RADIUS));
		System.out.println(first);
		System.out.println(second);
		System.out.println(third);
		System.out.println(fourth);
		System.out.println(fifth);
		System.out.println(sixth);
		
		if(first && second && third && fourth && fifth && sixth) {
			movement = false;
			totalTrees = 6;
			return;
		} else if(first && third && fifth && roundsExisted > 80) {
			totalTrees = 3;
			addedNum = 0;
			movement = false;
		} else if(second && fourth && sixth && roundsExisted > 80) {
			totalTrees = 3;
			addedNum = 1;
			movement = false;
		} else if(first && fourth && roundsExisted > 120) {
			totalTrees = 2;
			addedNum = 0;
			movement = false;
		} else if(second && fifth && roundsExisted > 120) {
			totalTrees = 2;
			addedNum = 1;
			movement = false;
		} else {
			randMove();
		}
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
		if(targetDir == null)
			return;
		for(int i = 0; i < 8; i++){
			if(!rc.hasMoved()) {
				if (rc.canMove(targetDir)){
					rc.move(targetDir);
					rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
					System.out.println(rc.hasMoved());
				} else {
					targetDir = targetDir.rotateLeftDegrees(r_l);
				}
			} else {
				break;
			}
		}
	}
	void spawnStuff() throws GameActionException {
		MapLocation m = rc.getLocation();
		if(channel == -1){
			for (int channel1 = 100; channel1 < GameConstants.BROADCAST_MAX_CHANNELS; channel1 += 4) {
				if (rc.readBroadcast(channel1) == 0 || rc.readBroadcast(channel1+3) == rc.getID()) {
					rc.broadcast(channel1, (int) m.x);
					rc.broadcast(channel1+1, (int) m.y);
					rc.broadcast(channel1+2, -1);
					rc.broadcast(channel1+3, rc.getID());
					channel = channel1;
					break;
				}
			}
		} else{			
			if(rc.readBroadcast(channel+1) < 0){
				rc.broadcast(channel+1, (int) m.y);
				rc.broadcast(channel, (int) m.x);
				rc.broadcast(channel+3, rc.getID());
			}



		}
		TreeInfo[] trees = rc.senseNearbyTrees((float)1.5, rc.getTeam());
		tendTrees(trees);
		if(trees.length < totalTrees - 1) {
			spawnTrees(trees);
			if(roundsNotMoved == 0){
				roundsNotMoved = 1;
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 1,(int) rc.getLocation().x);
				rc.broadcast(GameConstants.BROADCAST_MAX_CHANNELS - 2,(int) rc.getLocation().y);
			}
		}
	}
	void tendTrees(TreeInfo[] trees) {
		if(trees.length == 0)
			return;
		Arrays.sort(trees, new Comparator<TreeInfo>() {
			public int compare(TreeInfo b1, TreeInfo b2) {
				if(b1.health > b2.health)
					return 1;
				if(b1.health < b2.health)
					return -1;
				return 0;
			}
		});
		for(TreeInfo t : trees) {
			if(rc.canWater(t.ID)) {
				try {
					rc.water(t.ID);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(rc.canShake(t.ID)) {
				try {
					rc.shake(t.ID);
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	void spawnRobots() {
		int treesWithBullets = 0;
		TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		for(TreeInfo t : nearbyTrees) {
			if(t.containedBullets > 0) {
				treesWithBullets++;
			}
		}
		if(nearbyTrees.length > 12 && amts[1] < 1) {
			spawnRobot(RobotType.LUMBERJACK, 1);
		} else if(amts[2] < 1) {
			spawnRobot(RobotType.SOLDIER, 2);
		} else if(amts[0] < 1 && treesWithBullets > 2) {
			spawnRobot(RobotType.SCOUT, 0);
		} else if(amts[1] < 1) {
			spawnRobot(RobotType.LUMBERJACK, 1);
		} else if(amts[1] < 1) {
			spawnRobot(RobotType.SCOUT, 0);
		} else if(amts[2] < 10) {
			spawnRobot(RobotType.SOLDIER, 2);
		} 
	}
	void spawnRobot(RobotType type, int index) {
		try {
			if(rc.canBuildRobot(type, dirList[0])) {
				rc.buildRobot(type, dirList[0]);
				amts[index]++;
			} else if(rc.canBuildRobot(type, dirList[1])) {
				rc.buildRobot(type, dirList[1]);
				amts[index]++;
			} else if(rc.canBuildRobot(type, dirList[2])) {
				rc.buildRobot(type, dirList[2]);
				amts[index]++;
			} else if(rc.canBuildRobot(type, dirList[3])) {
				rc.buildRobot(type, dirList[3]);
				amts[index]++;
			} else if(rc.canBuildRobot(type, dirList[4])) {
				rc.buildRobot(type, dirList[4]);
				amts[index]++;
			} else if(rc.canBuildRobot(type, dirList[5])) {
				rc.buildRobot(type, dirList[5]);
				amts[index]++;
			} 
		} catch(GameActionException e) {
			e.printStackTrace();
		}
	}
	void spawnTrees(TreeInfo[] trees) {
		try {
			if(rc.canPlantTree(dirList[0])) {
				rc.plantTree(dirList[0]);
			} else if(rc.canPlantTree(dirList[1])) {
				rc.plantTree(dirList[1]);
			} else if(rc.canPlantTree(dirList[2])) {
				rc.plantTree(dirList[2]);
			} else if(rc.canPlantTree(dirList[3])) {
				rc.plantTree(dirList[3]);
			} else if(rc.canPlantTree(dirList[4])) {
				rc.plantTree(dirList[4]);
			} 
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
}
