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

import adtool.domains.rings.RealG0;

public class RealG0MinSum implements Domain<RealG0> {
	// number 4
	static final long serialVersionUID = 465945232556446844L;

	public RealG0MinSum() {
	}

	public final RealG0 getDefaultValue(final boolean proponent) {
		if (proponent) {
			return new RealG0(Double.POSITIVE_INFINITY);
		} else {
			return new RealG0(Double.POSITIVE_INFINITY);
		}
	}

	public final boolean isValueModifiable(final boolean proponent) {
		return proponent;
	}

	public String getName() {
		return "Minimal time for the proponent (sequential)";
	}

	public String getDescription() {
		final String name = "Minimal time for the proponent, assuming that all opponent's actions are in place and that actions are executed one after another";
		final String vd = "&#x211D;\u208A\u222A{\u221E}";
		final String[] operators = { "min(<i>x</i>,<i>y</i>)", "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
				"<i>x</i>&nbsp;+&nbsp;<i>y</i>", "min(<i>x</i>,<i>y</i>)", "<i>x</i>&nbsp;+&nbsp;<i>y</i>",
				"min(<i>x</i>,<i>y</i>)", };
		return DescriptionGenerator.generateDescription(this, name, vd, operators);
	}

	public final RealG0 op(final RealG0 a, final RealG0 b) {
		return RealG0.min(a, b);
	}

	public final RealG0 ap(final RealG0 a, final RealG0 b) {
		return RealG0.sum(a, b);
	}

	public final RealG0 oo(final RealG0 a, final RealG0 b) {
		return RealG0.sum(a, b);
	}

	public final RealG0 ao(final RealG0 a, final RealG0 b) {
		return RealG0.min(a, b);
	}

	public final RealG0 cp(final RealG0 a, final RealG0 b) {
		return RealG0.sum(a, b);
	}

	public final RealG0 co(final RealG0 a, final RealG0 b) {
		return RealG0.min(a, b);
	}
}
