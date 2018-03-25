# MOSS Memory Management Simulator

## Overview
The memory management simulator illustrates **page fault** behavior in a paged virtual memory system.
The program reads the initial state of the page table and a sequence of virtual memory instructions and writes a trace log indicating the effect of each instruction. It includes a graphical user interface so that students can observe page replacement algorithms at work. Students may be asked to implement a particular page replacement algorithm which the instructor can test by comparing the output from the student's algorithm to that produced by a working implementation.

For more read [user guide for the MOSS Memory Management Simulator](http://www.ontko.com/moss/memory/user_guide.html).

### Usage
Using written `Makefile` we can run easily run program

```
$ make run
```

[Page replacement algorithm](https://en.wikipedia.org/wiki/Page_replacement_algorithm) implemented in `src/PageFault.java`

### GUI

| Button  | Description                                                                                                 |
|:-------:|-------------------------------------------------------------------------------------------------------------|
|run      | Runs the simulation to completion. Note that the simulation pauses and updates the screen between each step.|
|step     | Runs a single setup of the simulation and updates the display.                                              |
|reset    | Initializes the simulator and starts from the beginning of the command file.                                |
|exit     | Exits the simulation.                                                                                       |
|page n   | Display information about this virtual page in the display area at the right.                               |

## License
Project released under the terms of the [MIT license](./LICENSE).