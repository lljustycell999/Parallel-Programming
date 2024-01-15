package csc375assignment2;

import java.io.*;

// Method 1: HashMap with ReadWriteLock (Custom Approch)
import java.util.Map;
import java.util.HashMap;

// Method 2: Concurrent HashMap (Library Approch)
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.CountDownLatch;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CSC375Assignment2 {
    
    static ReadWriteLock lock = new ReadWriteLock();
    
    static CountDownLatch start;
    static CountDownLatch stop;
    
    static Thread[] theThreads;
    static ScheduleGenerator[] theGenerators;
    
    static Map<Integer, String> courses;
    
    public static void main(String[] args) throws Exception {
            
        final int numThreads = 1000;
        String choice = "1";
            
        if(choice.compareToIgnoreCase("0") != 0 && choice.compareToIgnoreCase("1") != 0){
            System.out.println("You entered an invalid method");
            System.exit(0);
        }    
        
        boolean useRWL = false;
        
        // Load the data to the appropriate data structure
        System.out.println("Obtaining SUNY Oswego Course Data");
        if(choice.compareTo("0") == 0){
            courses = new HashMap<Integer, String>();
            useRWL = true;
        }    
        else
            courses = new ConcurrentHashMap<Integer, String>();
        
        File classesFile = new File("SUNYOswegoCourses.txt");
        Scanner classesFileSC = new Scanner(classesFile);
        int numCourses = 0;
        
        while(classesFileSC.hasNext()){
            courses.put(numCourses, classesFileSC.nextLine());
            numCourses++;
        }
        classesFileSC.close();
        
        // Signals to start & stop
        start = new CountDownLatch(1);
        stop = new CountDownLatch(numThreads);
        
        // Prepare Threads
        theThreads = new Thread[numThreads];
        theGenerators = new ScheduleGenerator[numThreads];
        
        /*
        String[] students = new String[numThreads];
        for(Integer i = 1; i < numThreads + 1; i++){
            students[i-1] = i.toString();
        }
        */
        
        for(int i = 0; i < numThreads; i++)
            theGenerators[i] = new ScheduleGenerator(courses, lock, numCourses, useRWL);
        
        System.out.println("Preparing Threads that will use the Schedule Generator");
        for(int i = 0; i < numThreads; i++){
            theThreads[i] = createThread(theGenerators[i]);
        }
        for(int i = 0; i < numThreads; i++){
            theThreads[i].start();
        }
        
        System.out.println("Threads are running");
        System.out.println();
        
        long stopTime;
        long startTime = System.nanoTime();
        long timeElapsed;
        
        start.countDown();
        
        try{
            stop.await();
        } finally{
            stopTime = System.nanoTime();
        }
        System.out.println("Program Complete");
        timeElapsed = stopTime - startTime;
        System.out.println("Total Time Taken: " + timeElapsed + " ns");
        
        System.exit(0);
          
    }
    
    public static Thread createThread(final ScheduleGenerator generator){
        
        Runnable keepRunning = new Runnable(){
            public void run(){
                try{
                    start.await();
                    
                    // Let each thread do a read or write operation 1000 times
                    //for(int i = 0; i < 1000; i++)
                        generator.readOrWrite();
                    stop.countDown();
                } catch(Exception e){
                    return;
                }
            }
        };
        return new Thread(keepRunning);
        
    }
    
}

class ReadWriteLock {
    
    int activeReaders;
    int activeWriters;
    
    int pendingReaders;
    int pendingWriters;
    
    final ReentrantLock lock = new ReentrantLock();
    
    final Condition canRead = lock.newCondition();
    final Condition canWrite = lock.newCondition();

    void readLock() throws InterruptedException{
        lock.lock();
        try{
            while(activeWriters != 0){
                ++pendingReaders;
                canRead.await();
                --pendingReaders;
            }
            ++activeReaders;
        }
        finally{
            lock.unlock();
        }
    }

    void writeLock(){
        lock.lock();
        try{
            while(activeReaders != 0 || activeWriters != 0){
                ++pendingWriters;
                canWrite.awaitUninterruptibly();
                --pendingWriters;
            }
            ++activeWriters;
        } finally{
            lock.unlock();
        }
    }

    void unlockWrite() throws IllegalMonitorStateException{
        lock.lock();
        try {
            if(activeWriters == 0) 
                throw new IllegalMonitorStateException();
            --activeWriters;
            if(pendingWriters != 0) 
                canWrite.signal();
            else
                canRead.signalAll();
        } finally {
            lock.unlock();
        }
    }

    void unlockRead() throws IllegalMonitorStateException {
        lock.lock();
        try {
            if(activeReaders == 0) 
                throw new IllegalMonitorStateException();
            if(--activeReaders == 0){
                if(pendingWriters != 0)
                    canWrite.signal();
                else
                    canRead.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }
}

class ScheduleGenerator extends Thread{
    
    private int numCourses;
    private final boolean useRWL;
    
    private String[] chosenClasses = {"A", "B", "C", "D", "E", "F"};

    private final Map<Integer, String> courses;
    private final ReadWriteLock lock;

    public ScheduleGenerator(Map<Integer, String> courseData, ReadWriteLock lock, int numClasses, boolean useRWL){
        this.courses = courseData;
        this.lock = lock;
        this.numCourses = numClasses;
        this.useRWL = useRWL;
    }

    public void readOrWrite() throws Exception{
        
        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // Reading feature: Create a sample course schedule
        // Writing feature: Add new courses (Just make a test course)

        double readOrWrite = rand.nextDouble();
        if(readOrWrite >= 0.50)
            writeNewCourse();
        else
            createSchedule();

    }

    private void createSchedule() throws Exception{

        ThreadLocalRandom rand = ThreadLocalRandom.current();

        // Create some random schedule of six classes
        if(useRWL){
            lock.readLock();
            for(int i = 0; i < 6; i++){
                int randomClass = rand.nextInt(numCourses);
                chosenClasses[i] = courses.get(randomClass);
            }
            displaySchedule();
            lock.unlockRead();
        }
        else{
            synchronized(courses){
                System.out.println("Sample Student Schedule");
                for(int i = 0; i < 6; i++){
                    int randomClass = rand.nextInt(numCourses);
                    chosenClasses[i] = courses.get(randomClass);
                    System.out.println(chosenClasses[i]);
                }
                System.out.println();
            }
        }

    }

    private void writeNewCourse() throws Exception{

        // Add a new class to the course list
        if(useRWL){
            lock.writeLock();
            courses.put(numCourses, "This is a test course");
            numCourses++;
            lock.unlockWrite();
        }
        else{
            synchronized(courses){
                courses.put(numCourses, "This is a test course");
                numCourses++;
            }
        }

    }
        
    public void displaySchedule() throws Exception{
        
        lock.unlockRead();
        lock.writeLock();
        System.out.println("Sample Student Schedule:");
        for(int i = 0; i < 6; i++)
            System.out.println(chosenClasses[i]);
        System.out.println();
        lock.unlockWrite();
        lock.readLock();
    }
      
}
