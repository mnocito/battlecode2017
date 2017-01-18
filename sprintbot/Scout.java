package sprintbot;

import battlecode.common.*;

public class Scout extends BaseRobot {
	float scoutDir = 0;
	boolean inTree = false;
	Team myTeam = rc.getTeam();
	public Scout(RobotController rc) {
		super(rc);
		if(myTeam == Team.A) {
			scoutDir = (float) Math.PI;
		} else {
			scoutDir = 0;
		}
	}
	public void run() {
		Direction dir = new Direction((float)Math.PI);
		TreeInfo target = null;
		try {
			MapLocation myLocation = rc.getLocation();
			// See if there are any nearby enemy robots
			Team enemy = myTeam.opponent();
			RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
			// If there are some...
			if (robots.length > 0) {
				if(rc.readBroadcast(11)!= robots[0].getID() ){
					rc.broadcast(9, (int)robots[0].getLocation().x);
					rc.broadcast(10, (int)robots[0].getLocation().y);
					rc.broadcast(11, (int)robots[0].getID());//target ID
					rc.broadcast(12, (int)robots[0].health);
				}
				
			} 
			TreeInfo[] trees = rc.senseNearbyTrees(RobotType.GARDENER.sensorRadius, enemy);
			if(trees.length > 0) {
				MapLocation enemLoc = trees[0].location;
				target = trees[0];
				rc.setIndicatorDot(target.getLocation(), 100, 50, 20);
				Direction treeDir = myLocation.directionTo(enemLoc);
				float dist = myLocation.distanceTo(enemLoc);
				
				if(inTree || dist < 0.08) {
					for(int i = 0; i < robots.length; i++) {
						if(robots[i].type == RobotType.GARDENER) {
							inTree = true;
							if(rc.canFireSingleShot()) {
								System.out.println("shooting it");
								rc.fireSingleShot(myLocation.directionTo(robots[i].location));
								Clock.yield();
							}
						}
					}
					Clock.yield();
				} else if(dist > 0.08 && dist < 1.2) {
					System.out.println("trying to move in close");
					rc.move(treeDir, (float) (dist / 1.5));
					Clock.yield();
				} else if(rc.canMove(treeDir)) {
					System.out.println("trying to hide in trees"); 
					rc.move(treeDir);
					if(myLocation.distanceTo(enemLoc) < (float) 0.08) {
						inTree = true;
					}
					Clock.yield();
				} else {
					scoutMove(enemy);
				}
			} else {
				scoutMove(enemy);
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Clock.yield();
	}

	public void scoutMove(Team t) {
		try {		
			Direction dir;
			dir = new Direction(scoutDir);
			if(rc.canMove(dir)) {
				rc.move(dir);
			} else {
				scoutDir = (float) (scoutDir + Math.PI/((Math.random()* 2 + 4)));
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
