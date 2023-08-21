# Apache Jena bnode descendants
This plugin was developed as a way to efficiently work with with [Concise Bounded Descriptions](https://www.w3.org/Submission/CBD/) in SPARQL.
It defines a property function that returns all blank nodes 'nested' under a resource.

## What is CBD
### Definition
Given a particular node (the starting node) in a particular RDF graph (the source graph), a subgraph of that particular graph, taken to comprise a concise bounded description of the resource denoted by the starting node, can be identified as follows:
1. Include in the subgraph all statements in the source graph where the subject of the statement is the starting node;
1. Recursively, for all statements identified in the subgraph thus far having a blank node object, include in the subgraph all statements in the source graph where the subject of the statement is the blank node in question and which are not already included in the subgraph.
1. Recursively, for all statements included in the subgraph thus far, for all reifications of each statement in the source graph, include the concise bounded description beginning from the rdf:Statement node of each reification.

This results in a subgraph where the object nodes are either URI references, literals, or blank nodes not serving as the subject of any statement in the graph.

## How the plugin helps
The second item in the definition is what this plugin helps with: recursively retrieving all blank nodes. This is something that is difficult in SPARQL to do for arbitrary depth trees (cycles in blank nodes should be avoided, and will result in a warning).

## Setup
Build the jar:
```
mvn package
```
Copy the resulting package to the 'lib' folder of your Jena installation.

Alternatively, use make to download Jena and build the project.

## Example usage
See [test/test-query.rq](test/test-query.rq).