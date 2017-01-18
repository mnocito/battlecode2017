package emlyn;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.Team;

public class Tank extends BaseRobot {
	int roundsMoved = 0;
	float scoutDir = 0;
	public Tank(RobotController rc) {
		super(rc);
	}
	public void init() {

	}
	public void run() {
		try {
			scoutMove();
			Team enemy = rc.getTeam().opponent();
			MapLocation myLocation = rc.getLocation();
			// See if there are any nearby enemy robots
			RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
			// If there are some...
			if (robots.length > 0) {
				// And we have enough bullets, and haven't attacked yet this turn...
				if (rc.canFirePentadShot()) {
					// ...Then fire a bullet in the direction of the enemy.
					rc.fireSingleShot(myLocation.directionTo(robots[0].location));
				}
			}
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Clock.yield();
	}
	public void scoutMove() {
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
