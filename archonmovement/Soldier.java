package archonmovement;

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
		System.out.println("channel 9 " + rc.readBroadcast(9) + " channel 10 " + rc.readBroadcast(10));
		RobotInfo[] robots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
		RobotInfo robot = null;
		MapLocation lastSpottedEnemy = null;
		if (robots.length > 0) {
			robot = robots[0];
			float archonX = Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 5));
			float archonY = Float.intBitsToFloat(rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 6));
			
			MapLocation archonLoc = new MapLocation(archonX, archonY);
			lastSpottedEnemy = new MapLocation(robots[0].getLocation().x, robots[0].getLocation().y);
			if(archonLoc.distanceTo(lastSpottedEnemy) > archonLoc.distanceTo(new MapLocation(rc.readBroadcast(9), rc.readBroadcast(10)))){
				rc.broadcast(9, (int)robots[0].getLocation().x);
				rc.broadcast(10, (int)robots[0].getLocation().y);
				rc.broadcast(11, (int)robots[0].getID());//target ID
				rc.broadcast(12, (int)robots[0].health);
			}
		} else {
			if(rc.getLocation().distanceTo(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10))) < RobotType.SOLDIER.sensorRadius) {
					MapLocation[] initialEnemyArchons = rc.getInitialArchonLocations(myTeam.opponent());
				//	MapLocation[] ourArchons = rc.getInitialArchonLocations(myTeam);
					if(initialEnemyArchons.length > 1  && (int)rc.readBroadcast(9) == (int) initialEnemyArchon.x && (int)rc.readBroadcast(10) == (int) initialEnemyArchon.y) {
						initialEnemyArchons[0] = initialEnemyArchons[1];
					} 
					rc.broadcast(9, (int)initialEnemyArchons[0].x);
					rc.broadcast(10, (int)initialEnemyArchons[0].y);
			}
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
							RobotInfo target = robots[0];
							boolean willHitFriend = false;
							MapLocation myLoc = rc.getLocation();
							RobotInfo[] friendlies = rc.senseNearbyRobots(-1,rc.getTeam());
							for(RobotInfo r: friendlies){
								if(Math.abs(myLoc.directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 && myLoc.distanceTo(r.location) < myLoc.distanceTo(target.location)){
									willHitFriend = true;
									rc.setIndicatorDot(r.location, 0, 1000, 0);
									break;
								}		
							}
							boolean willHitTree = false;
							if(!willHitFriend) {
								TreeInfo[] enemytrees = rc.senseNearbyTrees(-1, enemy);
								for(TreeInfo r: enemytrees){
									if(Math.abs(rc.getLocation().directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 && myLoc.distanceTo(r.location) < myLoc.distanceTo(target.location)){
										willHitTree = true;
										rc.setIndicatorDot(r.location, 0, 1000, 0);
										break;
									}		
								}
							}
							if(rc.getLocation().distanceTo(target.location)<2 && rc.getTeamBullets() > 50){
								if(willHitFriend || willHitTree) {
									bugPathTowards(target.location, null);
								} else  {
									if(rc.canFirePentadShot()&& !willHitFriend && !willHitTree){
										rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
									}else if(rc.canFireTriadShot()&& !willHitFriend && !willHitTree){
										rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
									}else if(rc.canFireSingleShot()&& !willHitFriend && !willHitTree){
										rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
									} 
								}
							} else {
								bugPathTowards(target.location, null);
								if(rc.canFireTriadShot() && !willHitFriend && !willHitTree && && rc.getTeamBullets() > 25) {
									rc.fireTriadShot((rc.getLocation().directionTo(target.getLocation())));
								} else if(rc.canFireSingleShot() && !willHitFriend && !willHitTree) {
									rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
								}
							}
							targetRobotID = target.ID;
							targetRobotLocation = target.location;
						}
					}
				}
			}
			// See if there are any nearby enemy robots
			RobotInfo[] friendlies = rc.senseNearbyRobots(-1, rc.getTeam()); 
			if (robots.length == 0 ) {
				if(!hasmoved && rc.readBroadcast(9) != 0){
					bugPathTowards(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)), rc.getLocation().directionTo(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10))));
					//moveTowards(new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)));
				}
				rc.setIndicatorLine(rc.getLocation(), new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10)), 50, 50, 100);
				if(targetRobotID != rc.readBroadcast(11)){
					targetRobotID = rc.readBroadcast(11);
				}
				targetRobotLocation = new MapLocation((float)rc.readBroadcast(9), (float)rc.readBroadcast(10));
				if(rc.getLocation().distanceTo(targetRobotLocation) < RobotType.SOLDIER.sensorRadius){
					MapLocation[] enemyArchs = rc.getInitialArchonLocations(enemy);
					MapLocation arch = rc.getInitialArchonLocations(rc.getTeam())[0];
					if((int)targetRobotLocation.x == (int) initialEnemyArchon.x && (int)targetRobotLocation.y == (int) initialEnemyArchon.y) {
						if(enemyArchs.length > 1) {
							rc.broadcast(9, (int)enemyArchs[1].x);
							rc.broadcast(10, (int)enemyArchs[1].y);
						} else {
							rc.broadcast(9, (int)arch.x);
							rc.broadcast(10, (int)arch.y);
						}
					} else if (enemyArchs.length > 1) {
						if((int)targetRobotLocation.x == (int) enemyArchs[1].x && (int)targetRobotLocation.y == (int)  enemyArchs[1].y) {
							rc.broadcast(9, (int)rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 5));
							rc.broadcast(10, (int)rc.readBroadcast(GameConstants.BROADCAST_MAX_CHANNELS - 6));
						} else {
							rc.broadcast(9, (int)(enemyArchs[1].x + enemyArchs[0].x)/2);
							rc.broadcast(10, (int)(enemyArchs[1].y + enemyArchs[0].y)/2);
						}
					} else {
						rc.broadcast(9, (int)arch.x);
						rc.broadcast(10, (int)arch.y);
					}
				}
			}else{
				RobotInfo target = robots[0];
				boolean willHitFriend = false;
				MapLocation myLoc = rc.getLocation();
				for(RobotInfo r: friendlies){
					if(Math.abs(myLoc.directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 && myLoc.distanceTo(r.location) < myLoc.distanceTo(target.location)){
						willHitFriend = true;
						rc.setIndicatorDot(r.location, 0, 1000, 0);
						break;
					}		
				}
				boolean willHitTree = false;
				if(!willHitFriend) {
					TreeInfo[] enemytrees = rc.senseNearbyTrees(-1, enemy);
					for(TreeInfo r: enemytrees){
						if(Math.abs(rc.getLocation().directionTo(r.location).radians - rc.getLocation().directionTo(target.location).radians) < .4 && myLoc.distanceTo(r.location) < myLoc.distanceTo(target.location)){
							willHitTree = true;
							rc.setIndicatorDot(r.location, 0, 1000, 0);
							break;
						}		
					}
				}
				if(rc.getLocation().distanceTo(target.location)<3.5){
					if(willHitFriend || willHitTree) {
						bugPathTowards(target.location, null);
					} else  {
						if(rc.canFirePentadShot()&& !willHitFriend && !willHitTree){
							rc.firePentadShot(rc.getLocation().directionTo(target.getLocation()));
						}else if(rc.canFireTriadShot()&& !willHitFriend && !willHitTree){
							rc.fireTriadShot(rc.getLocation().directionTo(target.getLocation()));
						}else if(rc.canFireSingleShot()&& !willHitFriend && !willHitTree){
							rc.fireSingleShot((rc.getLocation().directionTo(target.getLocation())));
						} 
					}
				} else {
					bugPathTowards(target.location, null);
					if(rc.canFireTriadShot() && !willHitFriend && !willHitTree) {
						rc.fireTriadShot((rc.getLocation().directionTo(target.getLocation())));
					} else if(rc.canFireSingleShot() && !willHitFriend && !willHitTree) {
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
				}else{
					moveTowards(Loc1);
				}
			}
		} catch (GameActionException e) {
			moveTowards(rc.getLocation().add(dir1));
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
