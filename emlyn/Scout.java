package emlyn;

import battlecode.common.*;

public class Scout extends BaseRobot {
	float scoutDir = 0;
	Team myTeam = rc.getTeam();
	public Scout(RobotController rc) {
		super(rc);
	}
	public void init() {
		if(myTeam == Team.A) {
			scoutDir = (float) Math.PI;
		} else {
			scoutDir = 0;
		}
	}
	public void run() {
		Direction dir = new Direction((float)Math.PI);
		Team enemy = myTeam.opponent();
		try {
			MapLocation myLocation = rc.getLocation();
			// See if there are any nearby enemy robots
			RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
			// If there are some...
			if (robots.length > 0) {
				// And we have enough bullets, and haven't attacked yet this turn...
				
				//three targets. if one does not equal the one we see and is not taken 
			
				if(rc.readBroadcast(11)!= robots[0].getID() ){
					rc.broadcast(9, (int)robots[0].getLocation().x);
					System.out.println(robots[0].getLocation().x);
					rc.broadcast(10, (int)robots[0].getLocation().y);
					System.out.println(robots[0].getLocation().y);
					rc.broadcast(11, (int)robots[0].getID());//target ID
					rc.broadcast(12, (int)robots[0].health);
				}
				
				if (rc.canFireSingleShot()) {
					// ...Then fire a bullet in the direction of the enemy.
					rc.fireSingleShot(myLocation.directionTo(robots[0].location));
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
