package archonmovement;
import java.util.Arrays;
import java.util.Comparator;

import battlecode.common.*;

public class Gardener extends BaseRobot {
	int[] amts = new int[4];
	// 0: scouts, 1: lumberjacks, 2: soldiers, 3: tanks
	int roundsExisted = 0;
	boolean scoutBuilt = false;
	int addedNum = 0;
	boolean movement = true;
	int totalTrees = 0;
	int roundsNotMoved = 0;
	static float gardDirection = 4.0f;
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
		roundsExisted++;
		RobotInfo[] enemies = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		if(enemies.length > 0){
			//for(RobotInfo r :enemies){
			//if(r.isRobot())
			///}
			rc.broadcast(50, (int)enemies[0].getLocation().x);
		}
		if (movement) {
			gardenerMove();
		} else {
			spawnStuff();
		}
		Clock.yield();
	}
	void gardenerMove() throws GameActionException{
		


		boolean first, second, third, fourth, fifth, sixth;
		first = rc.canPlantTree(dirList[0]);
		second = rc.canPlantTree(dirList[1]);
		third = rc.canPlantTree(dirList[2]);
		fourth = rc.canPlantTree(dirList[3]);
		fifth = rc.canPlantTree(dirList[4]);
		sixth = rc.canPlantTree(dirList[5]);

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
		}
		else {
			try {		
				Direction dir;
				dir = new Direction(gardDirection);
				if(rc.canMove(dir)) {
					rc.move(dir);


				} else {
					gardDirection = (float) (gardDirection + Math.PI/((Math.random()* 2 + 4)));
				}
			} catch (GameActionException e1) {
				e1.printStackTrace();
			}
		}
	}
	void spawnStuff() throws GameActionException {
		MapLocation m = rc.getLocation();

		for (int channel = 100; channel < GameConstants.BROADCAST_MAX_CHANNELS; channel += 4) {
			if (rc.readBroadcast(channel) == 0 || rc.readBroadcast(channel+3) == rc.getID()) {
				rc.broadcast(channel, (int) m.x);
				rc.broadcast(channel+1, (int) m.y);
				rc.broadcast(channel+2, -1);
				rc.broadcast(channel+3, rc.getID());
				break;
			}
		}
		TreeInfo[] trees = rc.senseNearbyTrees((float)1.5, rc.getTeam());
		tendTrees(trees);
		if(trees.length >= totalTrees - 1) {
			spawnRobots();
		} else {
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
		int spawnPos = 0;
		switch(totalTrees) {
		case 6:
			spawnPos = totalTrees - 1;
			break;
		case 3:
			spawnPos = (totalTrees - 1) * 2 + addedNum;
			break;
		case 2: 
			spawnPos = (totalTrees - 1) * 2  + addedNum;
			break;
		default:
			System.out.println("this should never happen");
		}
		if(amts[0] < 1 && rc.canBuildRobot(RobotType.SCOUT, dirList[spawnPos])) {
			try {

				rc.buildRobot(RobotType.SCOUT, dirList[spawnPos]);
				if(rc.readBroadcast(15) == 0){
					rc.broadcast(15, 1);
				}
				amts[0]++;
			} catch (GameActionException e) {
				System.out.println(e);
			}
		} else if(amts[1] < 2 &&  rc.canBuildRobot(RobotType.LUMBERJACK, dirList[spawnPos])) {
			try {
				rc.buildRobot(RobotType.LUMBERJACK, dirList[spawnPos]);
				amts[1]++;
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
		} else if(amts[2] < 10 && rc.canBuildRobot(RobotType.SOLDIER, dirList[spawnPos])) {
			try {
				rc.buildRobot(RobotType.SOLDIER, dirList[spawnPos]);
				amts[2]++;
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				System.out.println(e);
			}
		}
	}
	void spawnTrees(TreeInfo[] trees) {
		int treeLoc = 0;
		switch(totalTrees) {
		case 6:
			treeLoc = trees.length;
			break;
		case 3:
			treeLoc = trees.length * 2 + addedNum;
			break;
		case 2: 
			treeLoc = trees.length * 2 + addedNum;
			break;
		default:
			System.out.println("this should never happen");
		}
		if(!scoutBuilt && rc.canBuildRobot(RobotType.SCOUT, dirList[treeLoc])) {
			try {
				rc.buildRobot(RobotType.SCOUT, dirList[treeLoc]);
				scoutBuilt = true;
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(rc.canPlantTree(dirList[treeLoc])) {
			try {
				rc.plantTree(dirList[treeLoc]);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}