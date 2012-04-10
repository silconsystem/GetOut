/**
   GetOut  
   Copyright (C) 2012 Sylvain "Viish" Berfini

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.viish.apps.android.games.getout;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

/*
 * TODO
 * Keep scores
 * Don't be able to push boxes into rocks
 * Add a timer
 * Display and Handle bonus item
 * Change boxes color once one has been pushed
 * Better graphics
 * Display & Handle teleporters and ennemies
 */
public class GameEngine
{
	private MapGenerator	mapGenerator;
	private int				level				= 3;
	private int[][]			map;
	private int				size				= MapGenerator.SIZE;
	private Bitmap			box, rock, player, in, out, tile;
	private Bitmap			up, down, left, right;
	private int				currentI, currentJ, inJ, outJ, inI, outI;
	private int				movedBoxOldI, movedBoxOldJ, movedBoxNewI, movedBoxNewJ, movedBoxOldCellValue;
	private int				currentX, currentY;
	private int				destX, destY;
	private int				speed				= 3;
	private float			scale				= 2.6f;
	private boolean			isMoving			= false;
	private boolean			canObstacleBeMoved	= true;

	public void update()
	{
		if ((Math.abs(currentX - outI * tile.getWidth()) <= speed)
				&& Math.abs(currentY - outJ * tile.getHeight()) <= speed)
		{
			newLevel();
		}
		else if (currentX < 0 || currentY < 0
				|| currentX >= size * tile.getWidth()
				|| currentY >= size * tile.getHeight())
		// Teleport player on starting case and reset moving box if needed
		{
			currentX = destX = inI * tile.getWidth();
			currentY = destY = inJ * tile.getHeight();
			currentI = inI;
			currentJ = inJ;
			if (!canObstacleBeMoved)
			{
				map[movedBoxNewI][movedBoxNewJ] = movedBoxOldCellValue;
				map[movedBoxOldI][movedBoxOldJ] = MapGenerator.MOVABLE_OBSTACLE;
				canObstacleBeMoved = true;
			}
		}
		else if (currentX == destX && currentY != destY)
		{
			if (currentY > destY)
			{
				currentY -= speed;
				if (Math.abs(currentY - destY) <= speed)
					currentY = destY;
			}
			else
			{
				currentY += speed;
				if (Math.abs(currentY - destY) <= speed)
					currentY = destY;
			}
		}
		else if (currentY == destY && currentX != destX)
		{
			if (currentX > destX)
			{
				currentX -= speed;
				if (Math.abs(currentX - destX) <= speed)
					currentX = destX;
			}
			else
			{
				currentX += speed;
				if (Math.abs(currentX - destX) <= speed)
					currentX = destX;
			}
		}
		else if (currentX == destX && currentY == destY)
		{
			isMoving = false;
			if (currentI == outI && currentJ == outJ)
			{
				newLevel();
			}
		}
	}

	private void newLevel()
	{
		level += 1;
		canObstacleBeMoved = true;
		isMoving = false;
		mapGenerator.configure(level);
		Log.d("Generation new level", level + "");
		map = mapGenerator.generate();
		mapGenerator.printMap(map);

		for (int i = 0; i < map.length; i++)
		{
			for (int j = 0; j < map.length; j++)
			{
				if (map[i][j] == MapGenerator.IN)
				{
					currentI = inI = i;
					currentJ = inJ = j;
					currentX = destX = i * tile.getWidth();
					currentY = destY = j * tile.getHeight();
				}
				else if (map[i][j] == MapGenerator.OUT)
				{
					outI = i;
					outJ = j;
				}
			}
		}
	}

	private void moveTo(int i, int j)
	{
		if (j == currentJ)
			destX = tile.getWidth() * i;
		else
			destY = tile.getHeight() * j;

		currentI = i;
		currentJ = j;
	}

	public void move(float x, float y)
	{
		if (isMoving)
			return;

		x = x / scale;
		y = y / scale;

		if (x >= 0
				&& x <= left.getWidth()
				&& y >= size * tile.getHeight() + up.getHeight()
				&& y <= size * tile.getHeight() + up.getHeight()
						+ left.getHeight())
			move(Direction.WEST);
		else if (x >= left.getWidth() + up.getWidth()
				&& x <= left.getWidth() + up.getWidth() + right.getWidth()
				&& y >= size * tile.getHeight() + up.getHeight()
				&& y <= size * tile.getHeight() + up.getHeight()
						+ left.getHeight())
			move(Direction.EAST);
		else if (x >= left.getWidth() && x <= left.getWidth() + up.getWidth()
				&& y >= size * tile.getHeight()
				&& y <= size * tile.getHeight() + up.getHeight())
			move(Direction.NORTH);
		else if (x >= left.getWidth()
				&& x <= left.getWidth() + up.getWidth()
				&& y >= size * tile.getHeight() + up.getHeight()
						+ left.getHeight()
				&& y <= size * tile.getHeight() + up.getHeight()
						+ left.getHeight() + down.getHeight())
			move(Direction.SOUTH);
	}

	private void move(Direction d)
	{
		isMoving = true;
		switch (d)
		{
			case NORTH:
				if (canObstacleBeMoved
						&& map[currentI][currentJ - 1] == MapGenerator.MOVABLE_OBSTACLE)
				{
					movedBoxOldI = currentI;
					movedBoxOldJ = currentJ - 1;
					movedBoxNewI = currentI;
					movedBoxNewJ = currentJ - 2;
					movedBoxOldCellValue = map[currentI][currentJ - 2];
					
					map[currentI][currentJ - 2] = MapGenerator.MOVABLE_OBSTACLE;
					map[currentI][currentJ - 1] = MapGenerator.EMPTY;
					canObstacleBeMoved = false;
					moveTo(currentI, currentJ - 1);
					return;
				}

				for (int j = currentJ; j >= 0; j--)
				{
					if (map[currentI][j] == MapGenerator.OBSTACLE
							|| map[currentI][j] == MapGenerator.MOVABLE_OBSTACLE)
					{
						moveTo(currentI, j + 1);
						return;
					}
				}

				moveTo(currentI, -1);
				break;
			case SOUTH:
				if (canObstacleBeMoved
						&& map[currentI][currentJ + 1] == MapGenerator.MOVABLE_OBSTACLE)
				{
					movedBoxOldI = currentI;
					movedBoxOldJ = currentJ + 1;
					movedBoxNewI = currentI;
					movedBoxNewJ = currentJ + 2;
					movedBoxOldCellValue = map[currentI][currentJ + 2];
					
					map[currentI][currentJ + 2] = MapGenerator.MOVABLE_OBSTACLE;
					map[currentI][currentJ + 1] = MapGenerator.EMPTY;
					canObstacleBeMoved = false;
					moveTo(currentI, currentJ + 1);
					return;
				}

				for (int j = currentJ; j < size; j++)
				{
					if (map[currentI][j] == MapGenerator.OBSTACLE
							|| map[currentI][j] == MapGenerator.MOVABLE_OBSTACLE)
					{
						moveTo(currentI, j - 1);
						return;
					}
				}

				moveTo(currentI, size);
				break;
			case EAST:
				if (canObstacleBeMoved
						&& map[currentI + 1][currentJ] == MapGenerator.MOVABLE_OBSTACLE)
				{
					movedBoxOldI = currentI + 1;
					movedBoxOldJ = currentJ;
					movedBoxNewI = currentI + 2;
					movedBoxNewJ = currentJ;
					movedBoxOldCellValue = map[currentI + 2][currentJ];
					
					map[currentI + 2][currentJ] = MapGenerator.MOVABLE_OBSTACLE;
					map[currentI + 1][currentJ] = MapGenerator.EMPTY;
					canObstacleBeMoved = false;
					moveTo(currentI + 1, currentJ);
					return;
				}

				for (int i = currentI; i < size; i++)
				{
					if (map[i][currentJ] == MapGenerator.OBSTACLE
							|| map[i][currentJ] == MapGenerator.MOVABLE_OBSTACLE)
					{
						moveTo(i - 1, currentJ);
						return;
					}
				}

				moveTo(size, currentJ);
				break;
			case WEST:
				if (canObstacleBeMoved
						&& map[currentI - 1][currentJ] == MapGenerator.MOVABLE_OBSTACLE)
				{
					movedBoxOldI = currentI - 1;
					movedBoxOldJ = currentJ;
					movedBoxNewI = currentI - 2;
					movedBoxNewJ = currentJ;
					movedBoxOldCellValue = map[currentI - 2][currentJ];
					
					map[currentI - 2][currentJ] = MapGenerator.MOVABLE_OBSTACLE;
					map[currentI - 1][currentJ] = MapGenerator.EMPTY;
					canObstacleBeMoved = false;
					moveTo(currentI - 1, currentJ);
					return;
				}

				for (int i = currentI; i >= 0; i--)
				{
					if (map[i][currentJ] == MapGenerator.OBSTACLE
							|| map[i][currentJ] == MapGenerator.MOVABLE_OBSTACLE)
					{
						moveTo(i + 1, currentJ);
						return;
					}
				}

				moveTo(-1, currentJ);
				break;
		}
	}

	public void init(Resources resources)
	{
		box = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.box));
		rock = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.rock));
		in = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.in));
		out = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.out));
		player = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.caracter));
		tile = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.tile));

		up = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.up));
		down = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.down));
		left = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.left));
		right = BitmapFactory.decodeStream(resources
				.openRawResource(R.drawable.right));

		mapGenerator = new MapGenerator();
		newLevel();
	}

	public void draw(Canvas c)
	{
		c.scale(scale, scale);
		Paint p = new Paint();
		c.drawARGB(255, 79, 138, 144); // Background color

		for (int i = 0; i < size; i++)
		{
			for (int j = 0; j < size; j++)
			{
				if (map[i][j] != MapGenerator.DELETED)
					c.drawBitmap(tile, i * tile.getWidth(),
							j * tile.getHeight(), p);

				if (map[i][j] == MapGenerator.OBSTACLE)
					c.drawBitmap(rock, i * tile.getWidth(),
							j * tile.getHeight(), p);
				else if (map[i][j] == MapGenerator.MOVABLE_OBSTACLE)
					c.drawBitmap(box, i * tile.getWidth(),
							j * tile.getHeight(), p);
				else if (map[i][j] == MapGenerator.IN)
					c.drawBitmap(in, i * tile.getWidth(), j * tile.getHeight(),
							p);
				else if (map[i][j] == MapGenerator.OUT)
					c.drawBitmap(out, i * tile.getWidth(),
							j * tile.getHeight(), p);
			}
		}

		c.drawBitmap(player, currentX, currentY, p);

		c.drawBitmap(up, left.getWidth(), size * tile.getHeight(), p);
		c.drawBitmap(down, left.getWidth(),
				size * tile.getHeight() + up.getHeight() + left.getHeight(), p);
		c.drawBitmap(left, 0, size * tile.getHeight() + up.getHeight(), p);
		c.drawBitmap(right, left.getWidth() + up.getWidth(),
				size * tile.getHeight() + up.getHeight(), p);
	}
}
