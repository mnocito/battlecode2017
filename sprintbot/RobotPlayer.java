package sprintbot;
import com.sun.xml.internal.rngom.parse.host.Base;

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
        BaseRobot r = null;
        float teamBullets = rc.getTeamBullets();
		if(teamBullets >= 1000 && rc.getTeamVictoryPoints() == 900) {
			rc.donate(teamBullets);
		}
        if(teamBullets > 1500){
			rc.donate(1000);
		}
        switch (rc.getType()) {
            case ARCHON:
            	r = new Archon(rc);
            	break;
            case GARDENER:
            	r = new Gardener(rc);
            	break;
            case SOLDIER:
            	r = new Gardener(rc);
            	break;
            case LUMBERJACK:
            	r = new Lumberjack(rc);
            	break;
            case SCOUT:
            	r = new Scout(rc);
            	break;
            case TANK: 
            	r = new Scout(rc);
            	break;
        }
        while (true) {
        	r.run();
        }
	}


    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {
        	
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

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

                 //       tryMove(toEnemy);
                    } else {
                        // Move Randomly
                 //       tryMove(randomDirection());
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
}
