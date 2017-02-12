package org.team3128.narwhalvision;

/**
 * Superclass for all commands that can be sent to the phone from the robot
 */

public abstract class PhoneCommand
{
	/**
	 * Do whatever the command needs to do;
	 * @return true if the pipeline needs to reload its settings.
	 */
	public abstract boolean execute();
}
