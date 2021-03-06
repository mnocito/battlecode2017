package sprintbot;

import battlecode.common.*;

public class Soldier extends BaseRobot {
	float solDir = 0;
	MapLocation targetRobotLocation = null;
	public boolean hasmoved = false;
	int targetRobotID = 666;
	public Soldier(RobotController rc) {
		super(rc);
		if(myTeam == Team.A) {
			solDir = (float) Math.PI;
		} else {
			solDir = 0;
		}	}
	Team myTeam = rc.getTeam();

	public void run() {
		Team enemy = rc.getTeam().opponent();
        // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
        try {
        	hasmoved = false;
            MapLocation myLocation = rc.getLocation();
            // first Priority is dodging
            BulletInfo[] bullets = rc.senseNearbyBullets();
            if(bullets.length > 0){
            	for(BulletInfo b: bullets){ 
            		// if the bullets will hit
            		float x = b.location.x + b.getDir().getDeltaX(b.getSpeed());
            		float y = b.location.y + b.getDir().getDeltaY(b.getSpeed());
            		MapLocation bulletTarget = new MapLocation(x,y);
            		if(myLocation.distanceTo(bulletTarget) < GameConstants.MAX_ROBOT_RADIUS){
            			if(hasmoved == false && rc.canMove(rc.getLocation().directionTo(bulletTarget).rotateLeftDegrees(90))){
            				rc.move(rc.getLocation().directionTo(bulletTarget).rotateLeftDegrees(90));
            				hasmoved = true;
            				rc.setIndicatorDot(bulletTarget, 100, 0, 0);
            			}
            		}
            	}
            }
            // See if there are any nearby enemy robots
            RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
            RobotInfo[] friendlies = rc.senseNearbyRobots(-1, rc.getTeam()); 
            if (robots.length == 0 ) {
            	if(hasmoved == false){
            		moveTowards(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
            	}
            	rc.setIndicatorLine(rc.getLocation(), new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)), 50, 50, 100);
                if(targetRobotID != rc.readBroadcast(11)){
                	targetRobotID = rc.readBroadcast(11);
                	targetRobotLocation = new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10));
                }
            }else{
            	RobotInfo target = closestRobot(robots);
            	if(rc.canMove(target.getLocation())){
            		rc.move(target.location);;
            	}else if(rc.canMove(rc.getLocation().directionTo(target.location), 1)){
            		rc.move(rc.getLocation().directionTo(target.location), 1);
            	}else{
            		moveTowards(target.getLocation());
            	}
            	boolean willHitFriend = false;
            	for(RobotInfo r: friendlies){
        			if(Math.abs(rc.getLocation().directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 ){
        			willHitFriend = true;
        			rc.setIndicatorDot(r.location, 0, 1000, 0);
        			}		
        		}
            	
            	if(rc.canFirePentadShot()&& !willHitFriend){
            		rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
            	}else if(rc.canFireTriadShot()&& !willHitFriend){
            		rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
            	}else if(rc.canFireSingleShot()&& !willHitFriend){
            		rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
            	}
            	
            	targetRobotID = rc.readBroadcast(target.ID);
                targetRobotLocation = target.location;	
            }
            int archonNeedsHelp = rc.readBroadcast(15);
            System.out.println(archonNeedsHelp);
            TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
            if(archonNeedsHelp == 1 && hasmoved == false){
            	moveTowards(new MapLocation(rc.readBroadcast(23), rc.readBroadcast(24)));
            }else if(nearbyTrees.length > 0){
            	for(TreeInfo t: nearbyTrees){
            		if(rc.canFireSingleShot()){
            			rc.fireSingleShot(rc.getLocation().directionTo(t.location));
            		}
            	}
            }
           
            TreeInfo[] enemyTrees = rc.senseNearbyTrees(-1, rc.getTeam().opponent());
             if(enemyTrees.length > 0){
            	for(TreeInfo t: enemyTrees){
            		if(rc.canFireSingleShot()){
            			rc.fireSingleShot(rc.getLocation().directionTo(t.location));
            		}
            	}
            }
        } catch (Exception e) {
            System.out.println("Soldier Exception");
            e.printStackTrace();
        }
		Clock.yield();
	}
	public RobotInfo closestRobot(RobotInfo[] robots){
		RobotInfo closest = null;
		for(int i = 0; i < robots.length; i++){
			if(i == 0){
				closest = robots[i];
			}else{
				if( rc.getLocation().distanceTo(robots[i].location) < rc.getLocation().distanceTo(robots[i -1].location)){
					closest = robots[i];
				}
			}
		}
		return closest;	
	}
	public void moveTowards(MapLocation loc1) throws GameActionException{
		int r_l = 15;
		if(Math.random() > .5){
			 r_l = 15;
		} else{
			 r_l = -15;
		}
		Direction targetDir = rc.getLocation().directionTo(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
		for(int i = 0; i < 8; i++){
			if(hasmoved == false && rc.canMove(targetDir)){
				rc.move(targetDir);
				hasmoved = true;
				rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
				System.out.println(hasmoved);
			}else{
				targetDir = targetDir.rotateLeftDegrees(r_l);
			}
		}
	}
	public void soldierMove(Team t) {
		try {		
			Direction dir;
			dir = new Direction((float) -Math.PI/2);
			if(rc.canMove(dir)) {
				rc.move(dir);
			} else {
				solDir = (float) (solDir + Math.PI/((Math.random()* 2 + 4)));
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}