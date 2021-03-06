package com.nps.concurrent;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class StockExchangeTest
	extends junit.framework.TestCase
{
	public static Object	theTestLock = new Object();
	public static long		theMaxLiveMsgCount;

	public void testPersistentThreads()
	{
		System.out.println("PersistentThreadAgent");

		trade(new PersistentThreadAgent(), 100, 1000);
	}

	public void testTransientThreads()
	{
		System.out.println("JITThreadAgent");

		trade(new JITThreadAgent(), 20, 20);
	}

	public void testThreadPools()
	{
		System.out.println("ThreadPoolAgent (100)");

		ActorThreadPool pool = new ActorThreadPool(25, 100, 1, TimeUnit.SECONDS);
		trade(new ThreadPoolAgent(pool), 100, 1000);

		System.out.println("ThreadPoolAgent (10)");

		pool = new ActorThreadPool(1, 10, 1, TimeUnit.SECONDS);
		trade(new ThreadPoolAgent(pool), 100, 100);
	}

	private void trade(
		Agent	agent,
		int		actorCount,
		int		maxMessageCount)
	{
		theMaxLiveMsgCount = 0;

		StockExchangeActor a[] = new StockExchangeActor[actorCount];
		for (int i=0; i<actorCount; i++)
		{
			a[i] = new StockExchangeActor(agent, maxMessageCount);
		}

		while (true)
		{
			synchronized (theTestLock)
			{
				try
				{
					theTestLock.wait();
				}
				catch (InterruptedException ex)
				{
					continue;
				}

				break;
			}
		}

		StockExchangeActor.flushActorList();

		System.out.println("Maximum active messages: " + theMaxLiveMsgCount);
	}
}
