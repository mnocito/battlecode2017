package archonmovement;

import battlecode.common.*;

public class Lumberjack extends BaseRobot {
	
	public Lumberjack(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void init() {
		
	}
	
	public void run() throws GameActionException {
		TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		TreeInfo[] enemTrees = null;
		if(rc.getTeam() == Team.A){
			enemTrees = rc.senseNearbyTrees(-1, Team.B);
		}
		
		if(rc.getTeam() == Team.B){
			enemTrees = rc.senseNearbyTrees(-1, Team.A);
		}
		if(enemTrees.length > 0){
			chopTree(enemTrees[0]);
			if(rc.getLocation().distanceTo(enemTrees[0].location) > GameConstants.BULLET_TREE_RADIUS + GameConstants.MAX_ROBOT_RADIUS){
				moveToTree(enemTrees[0]);
			}
		}
		if(trees.length > 0) {
			chopTree(trees[0]);
			if(rc.getLocation().distanceTo(trees[0].location) > GameConstants.BULLET_TREE_RADIUS + GameConstants.MAX_ROBOT_RADIUS){
				moveToTree(trees[0]);
			}
			
		} else {
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
	void moveToBot(RobotInfo tree) throws GameActionException {
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
			if(rc.readBroadcast(12) != 10000){
                if(rc.canMove(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)))){
                	rc.move(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
                }else{
                	if(rc.canMove(new Direction(0))){
                		rc.move(new Direction(0));
                	} else{
                		if(rc.canMove(new Direction((float)Math.PI))){
                			rc.move(new Direction((float)Math.PI));
                		}
                	}
                }
            }else{
            	Direction ranDir = new Direction(BaseRobot.randomWithRange(0, (int)Math.PI * 2));
            	if(rc.canMove(ranDir)){
            		rc.move(ranDir);
            	}
            	else{
            		if(rc.canMove(new Direction(0))){
            			rc.move(new Direction(0));
            		}
            		
            	}
            }
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
	void moveToTree(TreeInfo tree) throws GameActionException {
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
				if(rc.readBroadcast(12) != 10000){
	                if(rc.canMove(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)))){
	                	rc.move(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
	                }else{
	                	if(rc.canMove(new Direction(0))){
	                		rc.move(new Direction(0));
	                	} else{
	                		if(rc.canMove(new Direction((float)Math.PI))){
	                			rc.move(new Direction((float)Math.PI));
	                		}
	                	}
	                }
                }else{
                	Direction ranDir = new Direction(BaseRobot.randomWithRange(0, (int)Math.PI * 2));
                	if(rc.canMove(ranDir)){
                		rc.move(ranDir);
                	}
                	else{
                		if(rc.canMove(new Direction(0))){
                			rc.move(new Direction(0));
                		}
                		
                	}
                }
				Clock.yield();
			}
	}
}
