package com.nps.concurrent;

import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

/**
 * Abstract base class for implementing an Erlang actor.  Each actor runs
 * in a separate thread, but how actors are mapped to threads is left up to
 * the derived class:  implement <code>run()</code>, <code>die()</code>,
 * <code>notifyMessageAvailable()</code>.
 * 
 * To be useful, an Actor must installed on which to call
 * <code>process()</code>, so messages will be processed.  The Actor can
 * optionally install a <code>MessageFilter</code> to filter messages.
 * 
 * @author John Lindal
 */
/* package */ abstract class Agent
	implements Runnable
{
	protected List<Object>	itsMessageQueue = new LinkedList<Object>();		// ought to be private
	private MessageFilter	itsMessageFilter;
	private Actor			itsActor;

	/**
	 * Gets the current filter which determines which messages to accept.
	 * 
	 * @return	the installed filter, or null if there is no filter
	 */
	public final MessageFilter getMessageFilter()
	{
		return itsMessageFilter;
	}

	/**
	 * Sets the filter which determines which messages to accept.
	 * 
	 * @param filter	the filter to install
	 */
	public final void setMessageFilter(
		MessageFilter filter)
	{
		itsMessageFilter = filter;
	}

	/**
	 * Returns true if we have an actor and it has unprocessed messages.
	 * 
	 * @return	true if this actor has unprocessed messages
	 */
	public final boolean hasPendingMessages()
	{
		synchronized (itsMessageQueue)
		{
			return (itsActor != null && itsMessageQueue.size() > 0);
		}
	}

	/**
	 * Receive a message.
	 * 
	 * @param msg				the message to receive
	 * @throws InvalidMessage	if the pre-filter rejects the message
	 */
	public final void recv(
		Object  msg)
		throws  InvalidMessage
	{
		if (itsMessageFilter != null && !itsMessageFilter.acceptMessage(msg))
		{
			throw new InvalidMessage();
		}

		synchronized (itsMessageQueue)
		{
			itsMessageQueue.add(msg);
			notifyMessageAvailable();
		}
	}

	/**
	 * Retrieve the next message in the queue.
	 * 
	 * @return	the next message
	 */
	/* package */ final Object next()
	{
		Object msg;
		synchronized (itsMessageQueue)
		{
			msg = itsMessageQueue.remove(0);
		}

		return msg;
	}

	/**
	 * Retrieve the next message of the specified type.
	 * 
	 * @return	the next message of the specified type or null if no such message
	 */
	/* package */ final Object next(
		final Class clazz)
	{
		return next(new MessageFilter()
		{
			public boolean acceptMessage(
				Object msg)
			{
				return clazz.isInstance(msg);
			}
		});
	}

	/**
	 * Retrieve the first message matching the specified filter.
	 * 
	 * @return	the first matching message or null if no such message
	 */
	/* package */ final Object next(
		MessageFilter f)
	{
		synchronized (itsMessageQueue)
		{
			Iterator iter = itsMessageQueue.iterator();
			while (iter.hasNext())
			{
				Object msg = iter.next();
				if (f.acceptMessage(msg))
				{
					iter.remove();
					return msg;
				}
			}
		}

		return null;
	}

	/**
	 * Duplicate this execution context for use by another actor.  This
	 * does not copy the message filter.
	 */
	abstract /* package */ Agent dup();

	/**
	 * Unregister this actor with the thread management system.
	 */
	abstract /* package */ void die();

	/**
	 * Notify the thread management system that this actor has received a
	 * message.
	 */
	abstract protected void notifyMessageAvailable();

	/**
	 * @return	true if this execution context already has an actor
	 */
	/* package */ final boolean hasActor()
	{
		return (itsActor != null);
	}

	/**
	 * Sets the actor.
	 * 
	 * @param actor	the actor to execute
	 */
	/* package */ final void setActor(
		Actor actor)
	{
		itsActor = actor;
	}

	/**
	 * Process a message.  This function is allowed to call next() to
	 * attempt to retrieve additional messages.  This function should never
	 * be called unless we have an actor.
	 * 
	 * @param msg	the message
	 */
	protected final void process(
		Object msg)
	{
		itsActor.process(msg);
	}
}