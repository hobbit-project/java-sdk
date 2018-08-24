test-benchmark:
	mvn -Dtest=DummyBenchmarkTest#checkHealth test

package-benchmark:
	mvn -DskipTests package

build-benchmark-image:
	mvn -Dtest=DummyBenchmarkTest#buildImages test

test-dockerized-benchmark:
	make package-benchmark
	make build-benchmark-image
	mvn -Dtest=DummyBenchmarkTest#checkHealthDockerized test
