package archonmovement;

import battlecode.common.*;

public class Lumberjack extends BaseRobot {
	static int channel = -1;
	static MapLocation target;
	static boolean claimed = false;
	static int addAngle = 10;
	TreeInfo targetTree = null;
	MapLocation initialEnemyArchon = rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
	public Lumberjack(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void init() {

	}

	public void run() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo[] enemTrees = null;
		enemTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, rc.getTeam().opponent());
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());		
		TreeInfo[] neutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		for(TreeInfo t: neutralTrees) {
			if(rc.canShake(t.ID))
				rc.shake(t.ID);
		}
		if(robots.length > 0) {
			if (rc.readBroadcast(11) != robots[0].getID()) {
				rc.broadcast(9, (int) robots[0].getLocation().x);
				rc.broadcast(10, (int) robots[0].getLocation().y);
				rc.broadcast(11, (int) robots[0].getID());// target ID
				rc.broadcast(12, (int) robots[0].health);
			}
			strikeBot(robots[0]);
		}
		
		if(!rc.canMove(initialEnemyArchon)) {
			if(trees.length > 0 || targetTree != null) {
				if(targetTree == null) {
					targetTree = trees[0];
				}
				if(!rc.canChop(targetTree.ID) && trees.length > 0) {
					targetTree = trees[0];
				}
				chopTree(targetTree);
				rc.setIndicatorDot(targetTree.location, 100, 100, 100);
				moveTowards(targetTree.location);
			} else {
				if (channel != -1) {
					rc.broadcast(channel+2, rc.getID());
					
			
				}else{
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
				if(!rc.hasMoved()){
					randMove();
				}
			}
		} else {
			rc.move(rc.getLocation().directionTo(initialEnemyArchon));
		}
		
		Clock.yield();
	}
	public void circleTowards(MapLocation loc1) throws GameActionException{
		Direction targetDir = rc.getLocation().directionTo(loc1);
		while(!rc.canMove(targetDir)) {
			addAngle += 5;
		}
		System.out.println("trying to move around");
		rc.move(targetDir.rotateRightDegrees(addAngle));
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
		if(rc.canShake(tree.ID)) {
			try {
				rc.shake(tree.ID);
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(rc.canChop(tree.ID)) {
			try {
				rc.chop(tree.ID);
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
