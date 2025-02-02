Christopher Wilt's heuristic search repository

I developed this code while attendung the University of New Hampshire, as well as 
on my own time shortly after.  The code base contains a variety of algorithms and
a variety of domains.

The code is "research code" and as a result doesn't have the fanciest documentation
and isn't always constructed according to the absolute best practices.  Unfortunately
no paper has ever been accepted because of outstanding documentation or being 
constructed to the most rigorous of industry standards.

Getting started

The project is built using maven, so you will need to understand the basics of maven 
in order to build and run the project.

The pom file can be used to find the dependencies (although there aren't too many of them).
It will also construct a jar that will run the programs.  The command "mvn package" will
build a jar that will run the different algorithms.

At the current version of 1.0.24 the resulting jar will be called heuristic_search-1.0.24.jar, but as
versions change it may be assigned a different name (or the pom file could assign a different name).

The following is an example of a command to run:

```
java -jar heuristic_search-1.0.24.jar --alg astar --problem {{{(path}}}/vacuumdata/tiny-1.vw --type vacuum
```

In order to run you have to specify what algorithm you want to run, what instance of a problem you want to run, and
what type of problem you are running.  

Other paramters are:

 - --time (how long to allow the program to run for before cutting it off)
 - --rest (additional arguments)
 - --exp (expansion limit)
 - --gen (generation limit)
 - --threads (how many threads to use for multithreaded algorithms)
 - --probargs (some types of problem have arguments)
 - --cost (some types of problem have different cost function)
 - nomemcheck (if you run out of RAM the algorithm is terminated, but you can disable this check)

Further note that some kinds of problem may need additional stuff.  For example TopSpin is a domain where the heuristic is a pattern
database, so you have to provide a pattern database file.  Note that there is code for generating pattern database files.

