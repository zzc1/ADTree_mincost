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
package adtool.domains.rings;

import java.io.Serializable;

public class RealG0 implements Serializable, Ring {
	static final long serialVersionUID = 122132278985141212L;
	private double value;

	public RealG0() {
		this(Double.POSITIVE_INFINITY);
	}

	public RealG0(final double d) {
		value = d;
		checkValue();
	}

	public Object clone() {
		return new RealG0(value);
	}

	public final boolean updateFromString(String s) {
		value = Double.parseDouble(s);
		return checkValue();
	}

	public final String toString() {
		return new Double(getValue()).toString();
	}

	public final String toUnicode() {
		if (getValue() == Double.POSITIVE_INFINITY) {
			return "\u221E";
		} else {
			return new Double(getValue()).toString();
		}
	}

	public static final RealG0 min(final RealG0 a, final RealG0 b) {
		return new RealG0(Math.min(a.getValue(), b.getValue()));
	}

	public static final RealG0 max(final RealG0 a, final RealG0 b) {
		return new RealG0(Math.max(a.getValue(), b.getValue()));
	}

	public static final RealG0 sum(final RealG0 a, final RealG0 b) {
		return new RealG0(a.getValue() + b.getValue());
	}

	public double getValue() {
		return this.value;
	}

	private boolean checkValue() {
		if (value < 0) {
			value = 0;
			return false;
		}
		return true;
	}

	public int compareTo(Object o) {
		if (o instanceof RealG0) {
			double val2 = ((RealG0) o).getValue();
			if (value == val2) {
				return 0;
			}
			if (value < val2) {
				return -1;
			}
			if (value > val2) {
				return 1;
			}
		}
		throw new ClassCastException("Unable to compare RealG0 class with " + o);
	}
}
