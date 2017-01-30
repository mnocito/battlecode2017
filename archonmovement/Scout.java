package archonmovement;

import battlecode.common.*;

public class Scout extends BaseRobot {
	float scoutDir = 0;
	boolean inTree = false;
	MapLocation lastSpottedEnemy = null;
	Team myTeam = rc.getTeam();
	MapLocation initialEnemyArchon = rc.getInitialArchonLocations(myTeam.opponent())[0];
	public Scout(RobotController rc) {
		super(rc);
		if(myTeam == Team.A) {
			scoutDir = (float) Math.PI;
		} else {
			scoutDir = 0;
		}
	}
	public MapLocation[] lastTurns = new MapLocation[3];
	public boolean DirectionBool = false;
	public void run() throws GameActionException {
		MapLocation archonLoc = new MapLocation(Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS-5)),Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS-6)));	
		MapLocation myLocation = rc.getLocation();
		Direction dir = new Direction((float)Math.PI);
		TreeInfo target = null;
		int ARCHON = 1;
		int SOLDIER = 2;
		
		try {
			// See if there are any nearby enemy robots
			Team enemy = myTeam.opponent();
	
			// If there are some...
			RobotInfo robot = null;
			if(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7) != 0 && !nearArchon(myTeam)) {
				System.out.println("going to archon " + Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7)));
				moveTowards(new MapLocation(Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7)), Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 8))));
			}  
			RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
			if (robots.length > 0) {
				robot = robots[0];
				if(rc.readBroadcast(11)!= robots[0].getID() ){
					rc.broadcast(9, (int)robots[0].getLocation().x);
					rc.broadcast(10, (int)robots[0].getLocation().y);
					rc.broadcast(11, (int)robots[0].getID());//target ID
					rc.broadcast(12, (int)robots[0].health);
				}
			} 
			TreeInfo[] neutrees = rc.senseNearbyTrees(RobotType.SCOUT.sensorRadius, Team.NEUTRAL);
			if(neutrees.length > 0) {
				TreeInfo neuTree = null;
				for(TreeInfo t : neutrees) {
					if(t.containedBullets > 0) {
						neuTree = t;
						break;
					}
				}
				if(neuTree != null) {
					System.out.println("trying to find neutral tree");
					rc.setIndicatorDot(neuTree.getLocation(), 255, 50, 20);
					if(rc.canShake(neuTree.location)) {
						rc.shake(neuTree.location);
					}
					bugPathTowards(neuTree.location, null);
				} else if(robot != null) {
					Direction robotDirection = myLocation.directionTo(robot.location);
					if(rc.canFireSingleShot()) {
						rc.fireSingleShot(robotDirection);
					} //else {
					moveTowards(robot.location);	
				//	}{
					
				}
			} else if(robot != null) {
				Direction robotDirection = myLocation.directionTo(robot.location);
				if(rc.canFireSingleShot()) {
					rc.fireSingleShot(robotDirection);
				} //else {
				moveTowards(robot.location);	
			//	}
			} else {
				if(initialEnemyArchon != null) {
					if(rc.getLocation().distanceTo(initialEnemyArchon) < RobotType.SCOUT.sensorRadius) {
						initialEnemyArchon = null;
					} 
				}
				scoutMove(enemy);
			}
			for(int i = 0; i < 3;i++){
				if(lastTurns[i] == null){
					lastTurns[i] = rc.getLocation();
				}
			}
			
				if(lastTurns[2] != null){
					lastTurns[0] = lastTurns[1];
					lastTurns[1] = lastTurns[2];
					lastTurns[2] = rc.getLocation();
				}
			
			
		/*	TreeInfo[] trees = rc.senseNearbyTrees(RobotType.GARDENER.sensorRadius, enemy);
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
			}*/ 
			
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		moveTowards(initialEnemyArchon);
		Clock.yield();
	}
	public void bugPathTowards(MapLocation Loc1, Direction dir1) throws GameActionException{
		rc.setIndicatorLine(rc.getLocation(), Loc1, 100, 100, 100);
		if(lastTurns[0].equals(lastTurns[2])){
			System.out.println("stuck");
			DirectionBool = !DirectionBool;
		}
		if(dir1 == null){
			dir1 = rc.getLocation().directionTo(Loc1);
		}
		try {
			if(!rc.hasMoved()) {
				if(rc.getLocation().distanceTo(Loc1) > rc.getType().strideRadius*4){
					double targetX = rc.getLocation().x + rc.getType().strideRadius * Math.cos(dir1.getAngleDegrees());
					double targetY = rc.getLocation().y + rc.getType().strideRadius * Math.sin(dir1.getAngleDegrees());
					MapLocation targetLocation = new MapLocation((float)targetX, (float)targetY);
					if(rc.senseTreeAtLocation(targetLocation) == null){
						for(int x = 0; x < 36; x++){
							if(DirectionBool == false){
								if(rc.canMove(dir1.rotateLeftDegrees(x*10))){
									rc.move(dir1.rotateLeftDegrees(x*10));
									break;
								}
							}
							if(DirectionBool == true){
								if(rc.canMove(dir1.rotateRightDegrees(x*10))){
									rc.move(dir1.rotateRightDegrees(x*10));
									break;
								}
							}
						}
					}
				}
			}
		} catch (GameActionException e) {
			moveTowards(rc.getLocation().add(dir1));
		}
		
	}
	public void moveTowards(MapLocation loc1) throws GameActionException{
		int r_l = 15;
		Direction targetDir = rc.getLocation().directionTo(loc1);
		if(targetDir == null)
			return;
		for(int i = 0; i < 8; i++){
			if(!rc.hasMoved()) {
				if (rc.canMove(targetDir)){
					rc.move(targetDir);
					rc.setIndicatorLine(rc.getLocation(), loc1, 0, 0, 1000);
					System.out.println(rc.hasMoved());
				} else {
					targetDir = targetDir.rotateLeftDegrees(r_l);
				}
			} else {
				break;
			}
		}
	}
	public void circleAround(RobotInfo robot) {
		MapLocation myLocation = rc.getLocation();
		Direction robotDirection = myLocation.directionTo(robot.location);
		float dist = myLocation.distanceTo(robot.location) - RobotType.SCOUT.bodyRadius - robot.getRadius();
		try {
			if(rc.canMove(robotDirection))
				rc.move(robotDirection);
			System.out.println("dist away " + dist);
		} catch (GameActionException e) {
			e.printStackTrace();
		}
	}
	public void scoutMove(Team t) {
		try {		
			Direction dir;
			dir = new Direction(scoutDir);
			if(!rc.hasMoved() && rc.canMove(dir)) {
				rc.move(dir);
			} else {
				scoutDir = (float) (scoutDir + Math.PI/((Math.random()* 2 + 4)));
			}
		} catch (GameActionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void init() {
		// TODO Auto-generated method stub
		
	}
}
