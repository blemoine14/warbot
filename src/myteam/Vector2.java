/*******************************************************************************
 *  
 *  Adapted from: com.badlogic.gdx.math.Vector2
 *   
 *
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package myteam;

import java.io.Serializable;


/** Encapsulates a 2D vector. Allows chaining methods by returning a reference to itself
 * @author badlogicgames@gmail.com */
public class Vector2 {

 	static public final double PI = 3.1415927f;
 	static public final double radiansToDegrees = 180f / PI;

 	static public final double degreesToRadians = PI / 180;
	// private static final long serialVersionUID = 913902788239530931L;

	/** Static temporary vector. Use with care! Use only when sure other code will not also use this.
	 * @see #tmp() **/
	public final static Vector2 tmp = new Vector2();

	public final static Vector2 X = new Vector2(1, 0);
	public final static Vector2 Y = new Vector2(0, 1);
	public final static Vector2 Zero = new Vector2(0, 0);

	/** the x-component of this vector **/
	public double x;
	/** the y-component of this vector **/
	public double y;

	/** Constructs a new vector at (0,0) */
	public Vector2 () {
	}

	/** Constructs a vector with the given components
	 * @param x The x-component
	 * @param y The y-component */
	public Vector2 (double x, double y) {
		this.x = x;
		this.y = y;
	}

	/** Constructs a vector from the given vector
	 * @param v The vector */
	public Vector2 (Vector2 v) {
		set(v);
	}

	/** @return a copy of this vector */
	public Vector2 cpy () {
		return new Vector2(this);
	}

	/** @return The euclidian length */
	public double len () {
		return (double)Math.sqrt(x * x + y * y);
	}

	/** @return The squared euclidian length */
	public double len2 () {
		return x * x + y * y;
	}

	/** Sets this vector from the given vector
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 set (Vector2 v) {
		x = v.x;
		y = v.y;
		return this;
	}

	/** Sets the components of this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 set (double x, double y) {
		this.x = x;
		this.y = y;
		return this;
	}

	/** Substracts the given vector from this vector.
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 sub (Vector2 v) {
		x -= v.x;
		y -= v.y;
		return this;
	}

	/** Normalizes this vector
	 * @return This vector for chaining */
	public Vector2 nor () {
		double len = len();
		if (len != 0) {
			x /= len;
			y /= len;
		}
		return this;
	}

	/** Adds the given vector to this vector
	 * @param v The vector
	 * @return This vector for chaining */
	public Vector2 add (Vector2 v) {
		x += v.x;
		y += v.y;
		return this;
	}

	/** Adds the given components to this vector
	 * @param x The x-component
	 * @param y The y-component
	 * @return This vector for chaining */
	public Vector2 add (double x, double y) {
		this.x += x;
		this.y += y;
		return this;
	}

	/** @param v The other vector
	 * @return The dot product between this and the other vector */
	public double dot (Vector2 v) {
		return x * v.x + y * v.y;
	}

	/** Multiplies this vector by a scalar
	 * @param scalar The scalar
	 * @return This vector for chaining */
	public Vector2 mul (double scalar) {
		x *= scalar;
		y *= scalar;
		return this;
	}

	/** @param v The other vector
	 * @return the distance between this and the other vector */
	public double dst (Vector2 v) {
		final double x_d = v.x - x;
		final double y_d = v.y - y;
		return (double)Math.sqrt(x_d * x_d + y_d * y_d);
	}

	/** @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return the distance between this and the other vector */
	public double dst (double x, double y) {
		final double x_d = x - this.x;
		final double y_d = y - this.y;
		return (double)Math.sqrt(x_d * x_d + y_d * y_d);
	}

//	/** @param v The other vector
//	 * @return the squared distance between this and the other vector */
//	public double dst2 (Vector2 v) {
//		final double x_d = v.x - x;
//		final double y_d = v.y - y;
//		return x_d * x_d + y_d * y_d;
//	}
//
//	/** @param x The x-component of the other vector
//	 * @param y The y-component of the other vector
//	 * @return the squared distance between this and the other vector */
//	public double dst2 (double x, double y) {
//		final double x_d = x - this.x;
//		final double y_d = y - this.y;
//		return x_d * x_d + y_d * y_d;
//	}

	public String toString () {
		return "[" + x + ":" + y + "]";
	}

	/** Substracts the other vector from this vector.
	 * @param x The x-component of the other vector
	 * @param y The y-component of the other vector
	 * @return This vector for chaining */
	public Vector2 sub (double x, double y) {
		this.x -= x;
		this.y -= y;
		return this;
	}

	/** NEVER EVER SAVE THIS REFERENCE! Do not use this unless you are aware of the side-effects, e.g. other methods might call this
	 * as well.
	 * 
	 * @return a temporary copy of this vector. Use with care as this is backed by a single static Vector2 instance. v1.tmp().add(
	 *         v2.tmp() ) will not work! */
	public Vector2 tmp () {
		return tmp.set(this);
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param v the other vector
	 * @return the cross product */
	public double crs (Vector2 v) {
		return this.x * v.y - this.y * v.x;
	}

	/** Calculates the 2D cross product between this and the given vector.
	 * @param x the x-coordinate of the other vector
	 * @param y the y-coordinate of the other vector
	 * @return the cross product */
	public double crs (double x, double y) {
		return this.x * y - this.y * x;
	}

	/** @return the angle in degrees of this vector (point) relative to the x-axis. Angles are counter-clockwise and between 0 and
	 *         360. */
	public double angle () {
		double angle = (double)Math.atan2(y, x) * radiansToDegrees;
		if (angle < 0) angle += 360;
		return angle;
	}

	/** Rotates the Vector2 by the given angle, counter-clockwise.
	 * @param angle the angle in degrees
	 * @return the */
	public Vector2 rotate (double angle) {
		double rad = angle * degreesToRadians;
		double cos = (double)Math.cos(rad);
		double sin = (double)Math.sin(rad);

		double newX = this.x * cos - this.y * sin;
		double newY = this.x * sin + this.y * cos;

		this.x = newX;
		this.y = newY;

		return this;
	}

	/** Linearly interpolates between this vector and the target vector by alpha which is in the range [0,1]. The result is stored
	 * in this vector.
	 * 
	 * @param target The target vector
	 * @param alpha The interpolation coefficient
	 * @return This vector for chaining. */
	public Vector2 lerp (Vector2 target, double alpha) {
		Vector2 r = this.mul(1.0f - alpha);
		r.add(target.tmp().mul(alpha));
		return r;
	}
	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		result = prime * result + NumberUtils.doubleToIntBits(x);
//		result = prime * result + NumberUtils.doubleToIntBits(y);
//		return result;
//	}

//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Vector2 other = (Vector2) obj;
//		if (NumberUtils.doubleToIntBits(x) != NumberUtils.doubleToIntBits(other.x))
//			return false;
//		if (NumberUtils.doubleToIntBits(y) != NumberUtils.doubleToIntBits(other.y))
//			return false;
//		return true;
//	}

}
