package emlyn;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
        }
	}

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        Direction dir = new Direction(0);
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	if(rc.getTeamBullets()>750)
        	rc.donate(300);
        	//if(rc.getTeamBullets() > 250){
        		//rc.donate(rc.getTeamBullets()-250);
        	//}
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
               

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < 1) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                //tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
                MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);

                rc.broadcast(2,(int)dir.radians);

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                dir = new Direction((float) (dir.radians + (Math.PI/6)));
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
 
	static void runGardener() throws GameActionException {
		int spawngen =0;
        System.out.println("I'm a gardener!");
        int dir = rc.readBroadcast(2);
        switch (dir){
        	case (int) (Math.PI/2): {
        		dir = 0;
        	} 
        }

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir1 = new Direction(dir);

                // Randomly attempt to build a soldier or lumberjack in this direction
                //if (rc.canBuildRobot(RobotType.SOLDIER, dir1) && Math.random() < .01) {
                  //  rc.buildRobot(RobotType.SOLDIER, dir1);
                //} else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir1) && Math.random() < .01 && rc.isBuildReady()) {
                  //  rc.buildRobot(RobotType.LUMBERJACK, dir1);
                //}\
                float xDiff = archonLoc.x - rc.getLocation().x;
                float yDiff = archonLoc.y - rc.getLocation().y;
                
                
                Direction angle1 =  new Direction((float)( Math.atan2(yDiff, xDiff)+Math.PI));
                System.out.println(angle1);
                if(distanceFrom( archonLoc,rc.getLocation())<4){
                	if(rc.canMove(angle1)){
                		rc.move(angle1);
                		Clock.yield();
                	}
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for(TreeInfo t : trees){
                	if(rc.canWater(t.location) && rc.getTeam()==t.team){
                		rc.water(t.ID);
                	}
                	if(rc.canShake(t.location) && rc.canInteractWithTree(t.ID)){
                		rc.shake(t.ID);
                	}
                	
                }
                

                if(rc.canPlantTree(angle1)){
                	
                	System.out.println(angle1);
                    
                	rc.plantTree(angle1);
                }
                Direction angle2 = new Direction((float) (angle1.radians + Math.PI/8));
                Direction angle3 = randomDirection();
                switch (spawngen){
                case 0:
                	if(rc.canBuildRobot(RobotType.SOLDIER,angle3)){
                    	rc.buildRobot(RobotType.SOLDIER, angle3);
                    	spawngen = 1;
                    }
                	
                case 1:
                	if(rc.canBuildRobot(RobotType.LUMBERJACK,angle3)){
                    	rc.buildRobot(RobotType.LUMBERJACK, angle3);
                    	spawngen = 0;
                    }
                	
                }
                
                
                // Move randomly
                //tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

                } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
	static float distanceFrom(MapLocation loc1, MapLocation loc2){
		return (float) Math.sqrt((loc1.x-loc2.x)*(loc1.x-loc2.x) + (loc1.y-loc2.y)*(loc1.y-loc2.y));
		
	}
    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();
        int currentTarget = 666;
        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	
        	

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	
            	TreeInfo[] trees = rc.senseNearbyTrees();
                if(trees.length>0){
                	for(TreeInfo t : trees){
                			if(distanceFrom(rc.getLocation(), t.getLocation()) > GameConstants.LUMBERJACK_STRIKE_RADIUS){
                				tryMove(rc.getLocation().directionTo(t.location));
                			}else{
                				if(rc.canShake(t.ID)){
                					rc.shake(t.ID);
                				}else{
                					if(rc.canChop(t.ID)){
                						rc.chop(t.ID);
                					}
                				}
                			}
                			
                		}
                	}
                

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        tryMove(toEnemy);
                    } else {
                        // Move Randomly
                        tryMove(randomDirection());
                    }
                }
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }

    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
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
    
    static void moveTowards( MapLocation loc){
    	float xDiff = rc.getLocation().x - loc.x;
        float yDiff = rc.getLocation().y - loc.y;
        
        
        Direction dir =  new Direction((float)( Math.atan2(yDiff, xDiff)));
        try{
        if(rc.canMove(dir)){
        	rc.move(dir);
        }
        }catch(Exception e){
        	System.out.println(e);
        }
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
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
}
