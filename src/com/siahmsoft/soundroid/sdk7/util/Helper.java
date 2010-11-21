/*
 * Copyright 2009 Yannick Stucki (yannickstucki.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.siahmsoft.soundroid.sdk7.util;

/**
 * Defines some constants used across different classes.
 * 
 * @author Yannick Stucki (yannickstucki@gmail.com)
 * 
 */
public final class Helper {

	/**
	 * The number of milliseconds in a second.
	 */
	public static final int MILLISECONDS_IN_SECOND = 1000;

	/**
	 * The number of seconds in a minute.
	 */
	public static final int SECONDS_IN_MINUTE = 60;

	/**
	 * The smallest number with two digits. Everything below has only one digit
	 * and therefore needs to be prepended by a 0 to have two digits.
	 */
	private static final int HAS_TWO_DIGITS = 10;

	/**
	 * Utility class has private constructor.
	 */
	private Helper() {
	}

	/**
	 * Returns a string of an integer which is at least two digits long. The first
	 * digit is filled witha 0 if needed.
	 */
	private static String getTwoDigitNumber(int number) {
		return (number < HAS_TWO_DIGITS ? "0" : "") + Integer.toString(number);
	}

	/**
	 * Returns a String formatted with minutes and seconds.
	 */
	public static String getTime(int minutes, int seconds) {
		return getTwoDigitNumber(minutes) + ":" + getTwoDigitNumber(seconds);
	}
}
