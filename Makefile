test-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealth test

package:
	mvn -DskipTests -DincludeDeps=true package

build-images:
	make package
	mvn -Dtest=BenchmarkTest#buildImages test

test-dockerized-benchmark:
	mvn -Dtest=BenchmarkTest#checkHealthDockerized test
