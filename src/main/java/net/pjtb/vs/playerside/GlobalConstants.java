/*
 * ArgonMS MapleStory server emulator written in Java
 * Copyright (C) 2011-2013  GoldenKevin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.pjtb.vs.playerside;

/**
 *
 * @author GoldenKevin
 */
public final class GlobalConstants {
	public static final short MAPLE_VERSION = 62;
	public static final int MCDB_VERSION = 3, MCDB_SUBVERSION = 0;
	public static final boolean TEST_SERVER = false;

	public static final int NULL_MAP = 999999999;
	public static final short MAX_LEVEL = 200;

	public static final String DIR_DELIMIT = System.getProperty("file.separator");

	private GlobalConstants() {
		//uninstantiable...
	}
}
