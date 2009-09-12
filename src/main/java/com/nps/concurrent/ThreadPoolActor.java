package com.nps.concurrent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Abstract class implementing an Erlang actor.  Each actor runs in a
 * separate, persistent thread.
 * 
 * @author John Lindal
 */
abstract class PersistentThreadActor
	extends ActorBase
{
	private Thread	itsThread;
	private boolean	itsAliveFlag = true;

	/**
	 * Register this actor with the system.
	 */
	protected final void register()
	{
		itsThread = new Thread(this);
		itsThread.start();
	}

	/**
	 * Register this actor with the system.
	 */
	protected final void die()
	{
		itsAliveFlag = false;
	}

	/**
	 * Process messages.
	 */
	public final void run()
	{
		while (itsAliveFlag)
		{
			waitForMessage();

			// Since the public API only allows adding messages, this
			// doesn't need to be synchronized between hasPendingMessages()
			// and next(), because the number of messages can only
			// increase.

			while (itsAliveFlag && hasPendingMessages())
			{
				if (!process(next()))
				{
					break;
				}
			}
		}
	}

	/**
	 * Wait for a message to arrive.
	 */
	protected final void waitForMessage()
	{
		synchronized (itsMessageQueue)
		{
			if (!hasPendingMessages())
			{
				try
				{
					itsMessageQueue.wait();
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	}

	/**
	 * Notify the actor that it has received a message.
	 */
	protected final void notifyMessageAvailable()
	{
		synchronized (itsMessageQueue)
		{
			itsMessageQueue.notify();
		}
	}
}
