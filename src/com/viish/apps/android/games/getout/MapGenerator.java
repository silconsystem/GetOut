package com.viish.apps.android.games.getout;


import java.util.Random;
//TODO :
// Generate and use teleports & ennemies
// Fix random shortcuts
public class MapGenerator 
{
	private static final int HOLE_PROBABILITY = 20;
	private static final int MOVABLE_OBSTACLE_ON_THE_WAY_PROBABILITY = 30;
	private static final int MIN_LATITUDE = 2;
	private static final int MAX_ITERATIONS = 50;
	
	public static final int SIZE = 13;
	public static final int DELETED = 0;
	public static final int EMPTY = 1;
	public static final int OBSTACLE = 2; 
	public static final int MOVABLE_OBSTACLE = 3; 
	public static final int TELEPORT = 4;
	public static final int ENNEMY = 5; 
	public static final int IN = 6; 
	public static final int OBSTACLE_TO_COME = 7; 
	public static final int FREE_WAY = 8; 
	public static final int OUT = 9; 
	public static final int BONUS = 10; 
	
	private boolean isMovingObstacles;
	private boolean isTeleportRunes;
	private boolean isEnnemies;
	private boolean isAlmostOnlyMovableObstacles;
	private Difficulty difficulty;
	
	public void configure(int level)
	{
		if (level <= 3)
			difficulty = Difficulty.EASY;
		else if (level <= 8)
			difficulty = Difficulty.MEDIUM;
		else
			difficulty = Difficulty.HARD;
		
		isMovingObstacles = level >= 2;
		isTeleportRunes = level >= 7;
		isEnnemies = level >= 9;
		isAlmostOnlyMovableObstacles = level >= 13;
	}
	
	public int[][] generate() 
	{
		int[][] map = new int[SIZE][SIZE];
		fillMapWithZeroes(map);
		Random r = new Random();
		generateHolesInMapBorders(r, map);
		
		// Starting point can't be on the border
		int startX = r.nextInt(SIZE - 2) + 1; 
		int startY = r.nextInt(SIZE - 2) + 1;
		
		map[startX][startY] = IN;
		
		boolean movableObstacleOnTheWay = isMovingObstacles && r.nextInt(100) > MOVABLE_OBSTACLE_ON_THE_WAY_PROBABILITY;
		boolean isMovableObstacleOnTheMap = false;
		
		int nbMovements = 0;
		int lastX = startX, lastY = startY;
		int nextX = 0, nextY = 0;
		Direction lastDirection = null;
		
		switch (difficulty)
		{
		case EASY:
			nbMovements = r.nextInt(2) + 4; // [4,5]
			break;
		case MEDIUM:
			nbMovements = r.nextInt(4) + 5; // [5,8]
			break;
		case HARD:
			nbMovements = r.nextInt(8) + 8; // [8,15]
			break;
		}

		int it = 0;
		int itt = 0;
		while (it < nbMovements)
		{
			itt += 1;
			if (itt >= MAX_ITERATIONS) {
				return generate();
			}
			
			Direction dir = Direction.randomDirection(r); // Direction to the exit
			if (lastDirection != null && (lastDirection.equals(dir) || Direction.isOpposite(lastDirection, dir)))
				continue; // We can't go in the same direction as before, on in the opposite one
			
			int latitude; // Number of free cases from the current point in this direction
			int obstacleX = -1, obstacleY = -1;
			int obstacle = OBSTACLE;
			int space = 0;
			
			switch (dir)
			{
			case NORTH:
				latitude = map[lastX][0] == DELETED ? lastY - 1 : lastY;
				if (latitude <= 2)
					continue;
				
				space = r.nextInt(latitude - MIN_LATITUDE) + MIN_LATITUDE; // At least one case
				obstacleY = lastY - space;
				obstacleX = lastX;				
				nextY = obstacleY + 1;
				nextX = lastX;
				break;
			case SOUTH:
				latitude = map[lastX][SIZE-1] == DELETED ? (SIZE - 1 - lastY) - 1 : (SIZE - 1 - lastY);
				if (latitude <= 2)
					continue;
				
				space = r.nextInt(latitude - MIN_LATITUDE) + MIN_LATITUDE; // At least one case
				obstacleY = lastY + space;
				obstacleX = lastX;
				nextY = obstacleY - 1;
				nextX = lastX;
				break;
			case EAST:
				latitude = map[SIZE-1][lastY] == DELETED ? (SIZE - 1 - lastX) - 1 : (SIZE - 1 - lastX);
				if (latitude <= 2)
					continue;
				
				space = r.nextInt(latitude - MIN_LATITUDE) + MIN_LATITUDE; // At least one case
				obstacleX = lastX + space;
				obstacleY = lastY;
				nextX = obstacleX - 1;
				nextY = lastY;
				break;
			case WEST:
				latitude = map[0][lastY] == DELETED ? lastX - 1 : lastX;
				if (latitude <= 2)
					continue;
				
				space = r.nextInt(latitude - MIN_LATITUDE) + MIN_LATITUDE; // At least one case
				obstacleX = lastX - space;
				obstacleY = lastY;
				nextX = obstacleX + 1;
				nextY = lastY;
				break;
			}
			
			// We check the path is free, then we mark the path on the map
			if (map[obstacleX][obstacleY] != EMPTY && map[obstacleX][obstacleY] != OBSTACLE)
				continue;
			
			if (nextX == lastX)
			{
				boolean skipWhile = false;
				for (int j = Math.min(lastY, nextY); j <= Math.max(lastY, nextY); j++) 
				{
					if (!isFreePath(it, map[nextX][j]))
						skipWhile = true;
				}
				if (skipWhile)
					continue;
				
				for (int j = Math.min(lastY, nextY); j <= Math.max(lastY, nextY); j++) 
				{
					if (map[nextX][j] == EMPTY)
						map[nextX][j] = FREE_WAY;
				}
			} else {
				boolean skipWhile = false;
				for (int i = Math.min(lastX, nextX); i <= Math.max(lastX, nextX); i++)
				{
					if (!isFreePath(it, map[i][nextY]))
						skipWhile = true;
				}
				if (skipWhile)
					continue;
				
				for (int i = Math.min(lastX, nextX); i <= Math.max(lastX, nextX); i++)
				{
					if (map[i][nextY] == EMPTY)
						map[i][nextY] = FREE_WAY;
				}
			}

			if (movableObstacleOnTheWay && !isMovableObstacleOnTheMap && it != 0 && (r.nextBoolean() || it == nbMovements - 1))
			{
				int tempX = obstacleX;
				int tempY = obstacleY;
				switch (dir)
				{
				case NORTH:
					obstacleY += 1;
					break;
				case SOUTH:
					obstacleY -= 1;
					break;
				case EAST:
					obstacleX -= 1;
					break;
				case WEST:
					obstacleX += 1;
					break;
				}
				
				if (map[obstacleX][obstacleY] == EMPTY) {
					obstacle = MOVABLE_OBSTACLE;
					map[tempX][tempY] = OBSTACLE_TO_COME;
					isMovableObstacleOnTheMap = true;
				} else {
					obstacleX = tempX;
					obstacleY = tempY;
				}
			}
			
			lastDirection = dir;
			map[obstacleX][obstacleY] = obstacle;
			lastX = nextX;
			lastY = nextY;
			it += 1;
		}
		
		if (map[lastX][lastY] == EMPTY || map[lastX][lastY] == FREE_WAY)
				map[lastX][lastY] = OUT;
		else
			return generate();
		
		int bonusX, bonusY;
		do {
			bonusX = r.nextInt(SIZE-1);
			bonusY = r.nextInt(SIZE-1);
		} while (map[bonusX][bonusY] != EMPTY);
		map[bonusX][bonusY] = BONUS;
		
		insertUselessObstacles(r, map);
		makeTheLevelHarder(map, startX, startY, lastX, lastY);
		
		return map;
	}
	
	private void makeTheLevelHarder(int[][]map, int startX, int startY, int exitX, int exitY) 
	{
		//Hacks to make the level harder
		
		// Put an obstacle between start and exit if they are aligned
		if (startX == exitX)
		{
			for (int j = Math.min(startY, exitY); j <= Math.max(startY, exitY); j++) 
			{
				if (map[startX][j] == EMPTY) {
					map[startX][j] = OBSTACLE;
					break;
				}
			}
		} else if (startY == exitY)
		{
			for (int i = Math.min(startX, exitX); i <= Math.max(startX, exitX); i++) 
			{
				if (map[i][startY] == EMPTY) {
					map[i][startY] = OBSTACLE;
					break;
				}
			}
		}
	}

	private void insertUselessObstacles(Random r, int[][] map) 
	{
		int probability = 100;
		
		switch (difficulty)
		{
		case EASY:
			probability = 5;
			break;
		case MEDIUM:
			probability = 15;
			break;
		case HARD:
			probability = 25;
			break;
		}
		
		for (int i = 0; i < SIZE; i++)
		{
			for (int j = 0; j < SIZE; j++)
			{
				if (map[i][j] == EMPTY)
				{
					if (r.nextInt(100) <= probability) {
						int obstacle = OBSTACLE;
						if (isAlmostOnlyMovableObstacles && i != 0 && j != 0 && i != SIZE - 1 && j != SIZE - 1)
						{ // No moving obstacles on border
							if (r.nextBoolean())
								obstacle = MOVABLE_OBSTACLE;
						}
						map[i][j] = obstacle;
					}
				}
			}
		}
	}

	private boolean isFreePath(int it, int i) 
	{
		boolean free = i == EMPTY || i == OUT || i == BONUS || i == MOVABLE_OBSTACLE || i == TELEPORT || i == ENNEMY || i == FREE_WAY;
		if (it == 0) // To avoid the path to use the starting point as free cell, we only authorize it at the first loop
			free = free || i == IN;
		return free;
	}

	private void generateHolesInMapBorders(Random r, int[][] map) 
	{
		for (int i = 0; i < SIZE; i++)
		{
			map[i][0] = r.nextInt(100) > HOLE_PROBABILITY ? EMPTY : DELETED;
		}
		
		for (int i = 0; i < SIZE; i++)
		{
			map[i][SIZE-1] = r.nextInt(100) > HOLE_PROBABILITY ? EMPTY : DELETED;
		}
		
		for (int j = 0; j < SIZE; j++)
		{
			map[0][j] = r.nextInt(100) > HOLE_PROBABILITY ? EMPTY : DELETED;
		}
		
		for (int j = 0; j < SIZE; j++)
		{
			map[SIZE-1][j] = r.nextInt(100) > HOLE_PROBABILITY ? EMPTY : DELETED;
		}
	}

	private void fillMapWithZeroes(int[][] map) {
		for (int i = 0; i < SIZE; i++)
		{
			for (int j = 0; j < SIZE; j++)
			{
				map[i][j] = EMPTY;
			}
		}
	}

	enum Difficulty {
		EASY, MEDIUM, HARD;
	}
	
	enum Direction {
		NORTH, SOUTH, EAST, WEST;
		
		public static Direction randomDirection(Random r)
		{
			int d = r.nextInt(Direction.values().length);
			return Direction.values()[d];
		}
		
		public static boolean isOpposite(Direction d1, Direction d2)
		{
			return d1.equals(Direction.NORTH) && d2.equals(Direction.SOUTH) ||
					d2.equals(Direction.NORTH) && d1.equals(Direction.SOUTH) ||
					d1.equals(Direction.EAST) && d2.equals(Direction.WEST) ||
					d2.equals(Direction.EAST) && d1.equals(Direction.WEST);
		}
	}
}