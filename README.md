tgni
====

Experimental NER techniques to address common (for me) text analysis problems.

The objective of the project is to build a taxonomy backed NER that uses standard NLP techniques or Lucene Analysis to solve text analysis problems. Since the conceptual view of the taxonomy is a graph, I am using the Neo4J graph database to store the data.

The NER is modeled as a Lucene analysis pipeline, and individual analyzers are built using OpenNLP and UIMA.

