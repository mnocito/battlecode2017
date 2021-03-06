package botone;

import battlecode.common.*;

public class Lumberjack extends BaseRobot {
	static int channel = -1;
	static MapLocation target;
	static boolean claimed = false;
	static int addAngle = 10;

	public Lumberjack(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void init() {

	}

	public void run() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, Team.NEUTRAL);
		TreeInfo[] enemTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());

		if(enemTrees.length > 0){
			chopTree(enemTrees[0]);
		} else if(trees.length > 0) {
			chopTree(trees[0]);
		} 
		RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam().opponent());
		if(robots.length > 0) {
			if (rc.readBroadcast(11) != robots[0].getID()) {
				rc.broadcast(9, (int) robots[0].getLocation().x);
				System.out.println(robots[0].getLocation().x);
				rc.broadcast(10, (int) robots[0].getLocation().y);
				System.out.println(robots[0].getLocation().y);
				rc.broadcast(11, (int) robots[0].getID());// target ID
				rc.broadcast(12, (int) robots[0].health);
			}
			if(rc.canStrike()) {
				rc.strike();
			}
		}

		if (channel != -1) {
			rc.broadcast(channel+2, rc.getID());
		} else {
			for (int i = 100; i < GameConstants.BROADCAST_MAX_CHANNELS; i+=4) {
				if (rc.readBroadcast(i+2) < 0) { // if this is not a "claimed" gardener, then claim it.
					rc.broadcast(i+2, rc.getID());
					target = new MapLocation(rc.readBroadcast(i), rc.readBroadcast(i+1));
					channel = i;
					System.out.println(channel);
					break;
				}
			}
		}
		if(!rc.hasMoved() && !rc.hasAttacked()){
			randMove();
		}
		Clock.yield();
	}
	public void moveTowards(MapLocation loc1) throws GameActionException{
		int r_l = 15;

		Direction targetDir = rc.getLocation().directionTo(loc1);
		for(int i = 0; i < 8; i++){
			if(!rc.hasMoved() && rc.canMove(targetDir)){
				rc.move(targetDir);
			}else{
				targetDir = targetDir.rotateLeftDegrees(r_l);
			}
		}
		rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
	}

	public void moveTowardsCircle(MapLocation loc1, int angle) throws GameActionException{
		angle = 10;


		Direction targetDir = rc.getLocation().directionTo(loc1);
		targetDir = targetDir.rotateLeftDegrees(angle);
		for(int i = 0; i < 8; i++){
			if(!rc.hasMoved() && rc.canMove(targetDir)){
				rc.move(targetDir);
			}else{
				targetDir = targetDir.rotateLeftDegrees(angle);
			}
		}
		addAngle = angle;
		rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
	}
	void strikeBot(RobotInfo bot) {
		RobotInfo[] ourBots = rc.senseNearbyRobots(2, rc.getTeam());
		if(ourBots.length < 2) {
			if(!rc.hasAttacked() && rc.canStrike()) {
				try {
					rc.strike();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	void chopTree(TreeInfo tree) {
		if(rc.canChop(tree.ID)) {
			try {
				rc.chop(tree.ID);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(rc.canShake(tree.ID)) {
			try {
				rc.shake(tree.ID);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	void moveToTree(TreeInfo tree) throws GameActionException {
		Direction dirToMove = rc.getLocation().directionTo(tree.location);
		if(rc.canMove(dirToMove)) {
			try {
				rc.move(dirToMove);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
