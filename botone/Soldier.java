package botone;

import battlecode.common.*;

public class Soldier extends BaseRobot {
	float solDir = 0;
	MapLocation targetRobotLocation = null;
	public Direction currentDirection = new Direction(0);
	public Direction initialDirection = new Direction(0);
	public boolean hasmoved = false;
	Team myTeam = rc.getTeam();
	MapLocation initialEnemyArchon = rc.getInitialArchonLocations(myTeam.opponent())[0];
	public boolean haveMoveRadius = false;
	public boolean DirectionBool = false; //false = left ||| true = right
	public MapLocation[] lastTurns = new MapLocation[3];
	float moveRadius = 0;
	int targetRobotID = 666;
	public Soldier(RobotController rc) {
		super(rc);
		// TODO Auto-generated constructor stub
	}
	public void init() {
		if(myTeam == Team.A) {
			solDir = (float) Math.PI;
		} else {
			solDir = 0;
		}
	}
	public void run() throws GameActionException {
		
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo robot = null;
		MapLocation lastSpottedEnemy = null;
		if (robots.length > 0) {
			robot = robots[0];
			float archonX = intToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7));
			float archonY = intToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 8));
			
			MapLocation archonLoc = new MapLocation(archonX, archonY);
			lastSpottedEnemy = new MapLocation(robots[0].getLocation().x, robots[0].getLocation().y);
			if(archonLoc.distanceTo(lastSpottedEnemy) > archonLoc.distanceTo(new MapLocation(rc.readBroadcast(9), rc.readBroadcast(10)))){
				rc.broadcast(9, (int)robots[0].getLocation().x);
				rc.broadcast(10, (int)robots[0].getLocation().y);
				rc.broadcast(11, (int)robots[0].getID());//target ID
				rc.broadcast(12, (int)robots[0].health);
			}
		} else if(rc.readBroadcast(9) != 0) {
			lastSpottedEnemy = new MapLocation(rc.readBroadcast(9), rc.readBroadcast(10));
		}
		
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
							rc.move(rc.getLocation().directionTo(bulletTarget).rotateLeftDegrees(160));
							hasmoved = true;
							rc.setIndicatorDot(bulletTarget, 100, 0, 0);
						}
					}
				}
			}
			// See if there are any nearby enemy robots
			RobotInfo[] friendlies = rc.senseNearbyRobots(-1, rc.getTeam()); 
			if (robots.length == 0 ) {
				if(!hasmoved && rc.readBroadcast(9) == 0) {
					moveTowards(initialEnemyArchon);
				} else if(!hasmoved && rc.readBroadcast(9) != 0){
					bugPathTowards(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)), rc.getLocation().directionTo(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10))));
					if(rc.canSenseLocation(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)))){
						if(rc.senseRobotAtLocation(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10))) == null){
							rc.broadcast(9, (int)initialEnemyArchon.x);
							rc.broadcast(10,(int) initialEnemyArchon.y);
						}
						
					}
					//moveTowards(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
				}
				rc.setIndicatorLine(rc.getLocation(), new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)), 50, 50, 100);
				if(targetRobotID != rc.readBroadcast(11)){
					targetRobotID = rc.readBroadcast(11);
					targetRobotLocation = new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10));
				}
				if(rc.getLocation().distanceTo(targetRobotLocation) < 2){
					rc.broadcast(9, rc.readBroadcast(9) + 1);
					rc.broadcast(10, rc.readBroadcast(10) + 1);
				}
			}else{
				RobotInfo target = closestRobot(robots);
				if(rc.canMove(target.getLocation())){
					rc.move(target.location);;
				}else if(rc.canMove(rc.getLocation().directionTo(target.location), 1)){
					rc.move(rc.getLocation().directionTo(target.location), 1);
				}else{
					//bugPathTowards(target.getLocation(), rc.getLocation().directionTo(target.getLocation()));
					moveTowards(target.getLocation());
				}
				boolean willHitFriend = false;
				for(RobotInfo r: friendlies){
					if(Math.abs(rc.getLocation().directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 ){
						willHitFriend = true;
						rc.setIndicatorDot(r.location, 0, 1000, 0);
					}		
				}
				if(rc.getLocation().distanceTo(target.location)<5){
					if(rc.canFirePentadShot()&& !willHitFriend){
						rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
					}else if(rc.canFireTriadShot()&& !willHitFriend){
						rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
					}else if(rc.canFireSingleShot()&& !willHitFriend){
						rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
					}
				} else {
					if(rc.canFireSingleShot()&& !willHitFriend){
						rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
					}
				}

				targetRobotID = target.ID;
				targetRobotLocation = target.location;	
			}
			int archonNeedsHelp = rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 7);
			TreeInfo[] nearbyTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
			if(archonNeedsHelp != 0 && hasmoved == false){
				//     moveTowards(new MapLocation(rc.readBroadcast(77), rc.readBroadcast(78)));
				//rc.setIndicatorLine(rc.getLocation(), new MapLocation(rc.readBroadcast(77), rc.readBroadcast(78)), 0, 1000, 0);
			}/*else if(nearbyTrees.length > 0){
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
			}*/
		} catch (Exception e) {
			System.out.println("Soldier Exception");
			e.printStackTrace();
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
	public void bugPathTowards(MapLocation Loc1, Direction dir1) throws GameActionException{
		for(int x = 0; x <3 ; x++){
			if(lastTurns[x].x == rc.getLocation().x && lastTurns[x].y == rc.getLocation().y ){
				DirectionBool = !DirectionBool;
				System.out.println("stuck my guy");
				break;
			}
		}
		
		if(dir1 == null){
			dir1 = rc.getLocation().directionTo(Loc1);
		}
		if(rc.getLocation().distanceTo(Loc1) > rc.getType().strideRadius*4){
			double targetX = rc.getLocation().x + rc.getType().strideRadius * Math.cos(dir1.getAngleDegrees());
			double targetY = rc.getLocation().y + rc.getType().strideRadius * Math.sin(dir1.getAngleDegrees());
			MapLocation targetLocation = new MapLocation((float)targetX, (float)targetY);
			if(rc.senseTreeAtLocation(targetLocation) == null){
				for(int x = 0; x < 36; x++){
					if(DirectionBool == false){
						if(rc.canMove(dir1.rotateLeftDegrees(x*10))){
							rc.move(dir1.rotateLeftDegrees(x*10));
						}
					}
					if(DirectionBool == true){
						if(rc.canMove(dir1.rotateRightDegrees(x*10))){
							rc.move(dir1.rotateRightDegrees(x*10));
						}
					}
				}
			}
		}else{
			moveTowards(Loc1);
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