# CSC 375 - Parallel Programming
This repository will contain the three projects completed as part of the CSC 375 - Parallel Programming course at SUNY Oswego taught by Professor Doug Lea.

# Assignment 1 Specification:
Write a parallel genetic algorithm program for an Facilities Layout problem in which:
* There are N stations (N at least 48) and M (M at least N) spots to place them on two-dimensional space (of any shape you like) representing a one-floor factory (There may be unoccupied spots serving as "holes"). The N stations come in F (at least 2) types representing their function. The different types may have different shapes, occupying multiple adjacent spots.
* There is a metric representing the benefit (affinity) of placing any two stations A and B near each other based on their Function and distance, with possibly different maximum values based on capacity or rate. The goal is to maximize total affinity.
* Each of K parallel tasks solve by (at least in part randomly) swapping or modifying station spots (possibly with holes), and occasionally exchanging parts of solutions with others (This is the main concurrent coordination problem). Run the program on a computer with at least 32 cores (and K at least 32). (You can develop with smaller K.)
* The program occasionally (for example twice per second) graphically displays solutions until converged or performs a given number of iterations. Details are up to you.

# Assignment 2 Specification:
This is mainly an exercise in performance measurement. Each of the following steps has many possible variations; you are free to choose any of them.
* Think of some kind of application in which a set of threads all rely on a shared collection of data; sometimes read-only, sometimes modifying the data. For example, a game-server with game-state as the collection, or a campus course scheduling system. Write a stripped-down version of this in which all the threads just emulate clients, and further strips out nearly everything except the reading and writing (while still somehow using results).
* Write one solution using a data structure and/or locking scheme of your own devising (most likely a variant of some known technique). Write another to primarily use standard platform library components.
* Compare the throughput of your program across at least two different loads on each of at least two different platforms. Use JMH unless you have an approved reason not to.
* Plot your results as a set of graphs and place on a web page.
