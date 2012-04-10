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

import java.util.Random;

public enum Direction
{
	NORTH, SOUTH, EAST, WEST;

	public static Direction randomDirection(Random r)
	{
		int d = r.nextInt(Direction.values().length);
		return Direction.values()[d];
	}

	public static boolean isOpposite(Direction d1, Direction d2)
	{
		return d1.equals(Direction.NORTH) && d2.equals(Direction.SOUTH)
				|| d2.equals(Direction.NORTH) && d1.equals(Direction.SOUTH)
				|| d1.equals(Direction.EAST) && d2.equals(Direction.WEST)
				|| d2.equals(Direction.EAST) && d1.equals(Direction.WEST);
	}
}