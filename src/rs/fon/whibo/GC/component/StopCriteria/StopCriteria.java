/*
 *  WhiBo
 *
 *  Copyright (C) 2010- by WhiBo development team and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://www.whibo.fon.bg.ac.rs
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package rs.fon.whibo.GC.component.StopCriteria;

import rs.fon.whibo.GC.algorithm.ClusterState;

// TODO: Auto-generated Javadoc
/**
 * The Interface StopCriteria.
 */
public interface StopCriteria {

	/**
	 * Should stop.
	 * 
	 * @param clusterState
	 *            the cluster state
	 * 
	 * @return true, if successful
	 */
	public boolean shouldStop(ClusterState clusterState);
}