/**
 * Author: Piotr Kordy (piotr.kordy@uni.lu <mailto:piotr.kordy@uni.lu>)
 * Date:   06/06/2013
 * Copyright (c) 2013,2012 University of Luxembourg -- Faculty of Science,
 *     Technology and Communication FSTC
 * All rights reserved.
 * Licensed under GNU Affero General Public License 3.0;
 *    This program is free software: you can redistribute it and/or modify
 *    it under the terms of the GNU Affero General Public License as
 *    published by the Free Software Foundation, either version 3 of the
 *    License, or (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU Affero General Public License for more details.
 *
 *    You should have received a copy of the GNU Affero General Public License
 *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package adtool.domains.predefined;

import adtool.domains.Domain;

public class RealG0MinSumCost extends RealG0MinSum {
	// number 6
	static final long serialVersionUID = 665945232556446846L;

	public RealG0MinSumCost() {
		super();
	}

	public final String getName() {
		return "Minimal cost for the proponent (not reusable)";
	}

	public final String getDescription() {
		final String name = "Minimal cost for the proponent, "
				+ "assuming that all opponent\'s actions are in place and" + " that resources are not reused";
		final String vd = "&#x211D;\u208A\u222A{\u221E}";
		final String[] operators = { "min(<i>x</i>,<i>y</i>)", "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
				"<i>x</i>&nbsp;+&nbsp;<i>y</i>", "min(<i>x</i>,<i>y</i>)", "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
				"min(<i>x</i>,<i>y</i>)", };
		return DescriptionGenerator.generateDescription(this, name, vd, operators);
	}
}
