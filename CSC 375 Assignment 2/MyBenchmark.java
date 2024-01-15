/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

import java.io.*;

// Method 1: HashMap with ReadWriteLock (Custom Approch)
import java.util.Map;
import java.util.HashMap;

// Method 2: Concurrent HashMap (Library Approch)
import java.util.concurrent.ConcurrentHashMap;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.*;

@State(Scope.Benchmark)
public class MyBenchmark {
    
    private final static int numThreads = 32;
    private static ReadWriteLock lock = new ReadWriteLock();
    private static ScheduleGenerator theGenerator;
    private static Map<Integer, String> courses;
    
    @Param(value = {"0", "1"})
    private static String choice;
        
    @Setup
    public void setup() throws IOException{
        
        boolean useRWL = false;

        // Load the data to the appropriate data structure
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

        // Prepare Generator
        theGenerator = new ScheduleGenerator(courses, lock, numCourses, useRWL);

    }
    
    @Threads(numThreads)
    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MICROSECONDS)
    //@Fork(value = 1)
    //@Warmup(iterations = 1)
    //@Measurement(iterations = 1)
    @Benchmark
    public void courseGenerator() throws Exception {
        theGenerator.readOrWrite();
    }
    
    static class ReadWriteLock{
        
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
    
    static class ScheduleGenerator extends Thread{
        
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
            if(readOrWrite >= 0.99)
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
                //displaySchedule();
                lock.unlockRead();
            }
            else{
                synchronized(courses){
                    //System.out.println("Sample Student Schedule");
                    for(int i = 0; i < 6; i++){
                        int randomClass = rand.nextInt(numCourses);
                        chosenClasses[i] = courses.get(randomClass);
                        //System.out.println(chosenClasses[i]);
                    }
                    //System.out.println();
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
        
        /*
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
        */

    }
    
}
