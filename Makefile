test-benchmark:
	mvn -Dtest=DummyBenchmarkTest#checkHealth test

package:
	mvn -DskipTests -DincludeDeps=true package

build-images:
	make package
	mvn -Dtest=DummyBenchmarkTest#buildImages test

test-dockerized-benchmark:
	make package
	make build-images
	mvn -Dtest=DummyBenchmarkTest#checkHealthDockerized test
