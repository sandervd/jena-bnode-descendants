test: apache-jena/lib/bnode-descendants-1.0-SNAPSHOT.jar test/test-query.rq
	./apache-jena/bin/sparql --query=test/test-query.rq --data=test/data.ttl

apache-jena/lib/bnode-descendants-1.0-SNAPSHOT.jar: src/main/java/eu/essentialcomplexity/sparql/pfunction/bnodeDescendants.java pom.xml apache-jena
	mvn package
	cp target/bnode-descendants-1.0-SNAPSHOT.jar apache-jena/lib


apache-jena:
	./bin/download-jena.sh

.PHONY: test
