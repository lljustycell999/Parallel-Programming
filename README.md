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

# Assignment 3 Specification:
Part A. Consider a rectangular piece of metal alloy, four times as wide as high, consisting of three different metals, each with different thermal characteristics. For each region of the alloy, there is a given amount (expressed in terms of a percentage) of each of the three base metals, that varies up to 25 percent due to random noise. The top left corner (at the mesh element at index [0,0]) is heated at $S$ degrees Celsius and the bottom right corner (index [width - 1,height - 1]) is heated at $T$ degrees Celsius. The temperature at these points may also randomly vary over time.

Your program calculates the final temperature for each region on the piece of alloy. The new temperature for a given region of the alloy is calculated using the formula:

<img align="center" width="403" alt="Screen Shot 2024-01-14 at 10 10 42 PM" src="https://github.com/lljustycell999/CSC375/assets/123667513/056240e0-1ada-43a8-a3f4-b849cdbb8e87">

where $m$ represents each of the three base metals, $C_m$ is the thermal constant for metal $m$, $N$ is the set representing the neighbouring regions, $temp_n$ is the temperature of the neighbouring region, $p^{m}_{n}$ is the percentage of metal $m$ in neighbour $n$, and $\vert N\vert$ is the number of neighbouring regions. 

This computation is repeated until the temperatures converge to a final value or a reasonable maximum number of iterations is reached.

The values for $S$, $T$, $C_1$, $C_2$, $C_3$, the dimensions of the mesh, and the threshold should be parameters to the program. Note however, that combinations of these parameters do not do not converge well. Try values of (0.75, 1.0, 1.25) C1, C2, C3 for your test/demo.

Assume that the edges are maximally insulated from their surroundings.

Your program should graphically display the results by drawing a grid of points with intensities or colors indicating temperature. (Alternatively, you can just output them to a file and use something like gnuplot to display them.)

Acknowledgment: This assignment was adapted from a study by Steve MacDonald.

Part B. Reimplement using SIMD parallelism, a GPU, or multiple servers (I decided to use multiple servers via two ServerSockets).
