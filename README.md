# HOBBIT Java SDK [![Build Status](https://travis-ci.org/hobbit-project/java-sdk.svg?branch=master)](https://travis-ci.org/hobbit-project/java-sdk)

Develop of HOBBIT-compatible components easier and to execute them locally without having a running HOBBIT-platform instance. 

The SDK helps platform users with the following tasks:
* Design systems for benchmarking and debug them within a particular benchmark
* Design a benchmark and debug the interactions between its components
* Upload results (docker images of your components) to the online platform

# Details
Technically the SDK is focused on the orchestration of docker images/containers for all the required components. Basic implementations of hobbit-related components (described [here](https://github.com/hobbit-project/platform/wiki/Develop-a-component-in-Java)) are also included into SDK to demonstrate how the local debugging process may be organized. 

As a result users may execute and debug their systems/benchmarks either “as is” (and hit the breakpoints in the code) or being packed into docker containers (the same manner as components will be operated by the online platform). The SDK provides users with internal log messages from the containers, which make the debugging process more effective and less error-prone. 

# How to use
See the [SDK basic example](https://github.com/hobbit-project/java-sdk-example) in the separate repository.

# Support & Feedback
Feel free to ask your questions and suggestions under the Issues tab. 
Also feel free to put feedback about benchmarks and systems, for which SDK have been helpful.

# News/Changelog
See the [Releases page](https://github.com/hobbit-project/java-sdk/releases)
