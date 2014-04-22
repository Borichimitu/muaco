package ru.ifmo.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;


public class RandomProvider {
	private static RandomProvider instance;
	private Map<Long, Integer> threadToId;
	private AtomicInteger threadsCounter;
	private Random[] random;
	private Random seedRandom;
	
	private RandomProvider(int numberOfThreads) {
		threadToId = new HashMap<Long, Integer>();
		threadsCounter = new AtomicInteger(0);
		seedRandom = new Random();
		random = new Random[numberOfThreads];
		for (int i = 0; i < random.length; i++) {
			random[i] = new Random(seedRandom.nextInt());
		}
	}

	public static void initialize(int numberOfThreads) {
		instance = new RandomProvider(numberOfThreads + 1);
	}

	public static synchronized void register() {
		long threadId = Thread.currentThread().getId();
		if (!instance.threadToId.containsKey(threadId)) {
			System.out.println("Thread #" + threadId + " is registering");
			instance.threadToId.put(threadId, instance.threadsCounter.get());
			instance.threadsCounter.incrementAndGet();
		}
	}
	
	public static Random getInstance() {
		return instance.random[instance.threadToId.get(Thread.currentThread().getId())];
	}
}