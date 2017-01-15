package archonmovement;

import battlecode.common.*;

public class Lumberjack extends BaseRobot {
	
	public Lumberjack(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void init() {
		
	}
	
	public void run() {
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		if(trees.length > 0) {
			chopTree(trees[0]);
			moveToTree(trees[0]);
		} else {
			RobotInfo[] robots = rc.senseNearbyRobots(2, rc.getTeam().opponent());
			if(robots.length > 0) {
				strikeBot(robots[0]);
				moveToBot(robots[0]);
			} else {
				randMove();
			}
		}
		Clock.yield();
	}
	void strikeBot(RobotInfo bot) {
		RobotInfo[] ourBots = rc.senseNearbyRobots(2, rc.getTeam());
		if(ourBots.length < 2) {
			if(rc.canStrike()) {
				try {
					rc.strike();
					Clock.yield();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	void moveToBot(RobotInfo tree) {
		Direction dirToMove = rc.getLocation().directionTo(tree.location);
		if(rc.canMove(dirToMove)) {
			try {
				rc.move(dirToMove);
				Clock.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			randMove();
			Clock.yield();
		}
	}
	void chopTree(TreeInfo tree) {
		if(rc.canChop(tree.ID)) {
			try {
				rc.chop(tree.ID);
				Clock.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(rc.canShake(tree.ID)) {
			try {
				rc.shake(tree.ID);
				Clock.yield();
			} catch (GameActionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	void moveToTree(TreeInfo tree) {
			Direction dirToMove = rc.getLocation().directionTo(tree.location);
			if(rc.canMove(dirToMove)) {
				try {
					rc.move(dirToMove);
					Clock.yield();
				} catch (GameActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				randMove();
				Clock.yield();
			}
	}
}
