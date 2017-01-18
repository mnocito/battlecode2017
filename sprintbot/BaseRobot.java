package sprintbot;

import battlecode.common.*;
public abstract class BaseRobot {
	static float moveDir = 0;
	public static RobotController rc;
	static Direction[] dirList = {new Direction((float) -2.0943952), new Direction((float) -1.0471976), new Direction((float) 0), new Direction((float) 1.0471976), new Direction((float) 2.0943952), new Direction((float) -3.1415927)};
	static Direction[] dirA =  {new Direction((float) 1.0471976), new Direction((float) 2.0943952), new Direction((float) -3.1415927), new Direction((float) -2.0943952), new Direction((float) -1.0471976), new Direction((float) 0)};
	public BaseRobot(RobotController rc) {
		this.rc = rc;
	}
    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }
    
    public BaseRobot() {
    	
    }
    
    abstract void run() throws GameActionException;

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static void initDirList(Team t) {
    	if(t == Team.A) {
    		dirList = dirA;
    	}
    }
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    boolean nearArchon(RobotType type, Team t) {
    	RobotInfo[] rbx = rc.senseNearbyRobots(type.sensorRadius, t);
    	for(RobotInfo r : rbx) {
    		if(r.type == RobotType.ARCHON && rc.getLocation().distanceTo(r.location) < type.sensorRadius / 1.5) {
    			return false;
    		}
    	}
    	return false;
    }
    void randMove() {
		try {		
			Direction dir;
			dir = new Direction(moveDir);
			if(rc.canMove(dir)) {
				rc.move(dir);
			} else {
				moveDir = (float) (moveDir + Math.PI/((Math.random()* 2 + 4)));
			}
		} catch (GameActionException e1) {
			e1.printStackTrace();
		}
    }
    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    static int floatToInt(float f) {
    	int ret = 0;
    	int powNum = String.valueOf(f).length() - 1;
    	ret = (int) (f * Math.pow(10.0, (double) powNum));
    	return ret;
    }
    static float intToFloat(int f) {
    	float ret = 0;
    	int powNum = String.valueOf(f).length() - 1;
    	ret = (float) (f / Math.pow(10.0, (double) powNum));
    	return ret;
    }
    static int randomWithRange(int min, int max) {
       int range = (max - min) + 1;     
       return (int)(Math.random() * range) + min;
    }
}
