# JInsect
The JINSECT toolkit is a Java-based toolkit and library that supports and demonstrates the use of n-gram graphs within Natural Language Processing applications, ranging from summarization and summary evaluation to text classiﬁcation and indexing.

## Main concepts

## Versions
V1.0
The first version of the JInsect library was born through a strenuous [PhD effort](http://www.iit.demokritos.gr/~ggianna), which means that a lot of small projects were attached to the code. Thus, the 1st version includes:
- The n-gram graphs (NGG) representations. See my [thesis, Chapter 3](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The NGG operators update/merge, intersect, allNotIn, etc. See my [thesis, Chapter 4](http://www.iit.demokritos.gr/~ggianna/thesis.pdf) for more info.
- The AutoSummENG summary evaluation family of methods.
- INSECTDB storage abstraction for object serialization.
- A very rich (and useful!) utils class which one *must* consult before trying to work with the graphs.
- Tools for the estimation of optimal parameters of n-gram graphs
- Support for [DOT](http://www.graphviz.org/doc/info/lang.html) language representation of NGGs.
...and many many side-projects that are hidden including a chunker based on something similar to a language model, a semantic index that builds upon string subsumption to determine meaning and many others. Most of these are, sadly, not documented or published.

I should stress that V1.0:
* supports efficient multi-threaded execution
* contains examples of application for classification
* contains examples of application for clustering
* contains command-line application for language-neutral summarization

**TODO for V1.0:** 
* Clean problematic classes that have dependencies from Web services.

V2.0
The second version of n-gram graphs is hoping to be started. The aim is to remove problematic dependencies, due to subprojects and keep the clean, core part of the project. I am also aiming to convert it into a maven project to improve integration into current solutions.
## License
JInsect is under [LGPL license](https://www.gnu.org/licenses/lgpl.html).
