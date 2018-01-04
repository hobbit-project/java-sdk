# HOBBIT Java SDK
Summarizing the experience of the successful co-organization of the DEBS Grand Challenge 2017 on the HOBBIT platform, we made one step towards a more lightweight and productive design-development of HOBBIT-related software components. We are happy to announce a standalone software library called the HOBBIT Java SDK.

The proposed SDK was is targeted to make the design and development of HOBBIT-compatible components easier and to execute them locally without having a running HOBBIT-platform instance. More details about the [HOBBIT platform](https://github.com/hobbit-project/platform) and the HOBBIT project can be found [here](https://project-hobbit.eu/). 

The SDK helps platform users with the following tasks:
* Design systems for benchmarking and debug them within a particular benchmark
* Design a benchmark and debug the interactions between its components
* Upload results (docker images of your components) to the online platform

Technically the SDK is focused on the orchestration of docker images/containers for all the required components. Basic implementations of hobbit-related components (described [here](https://github.com/hobbit-project/platform/wiki/Develop-a-component-in-Java)) are also included into SDK to demonstrate how the local debugging process may be organized. 

As a result users may execute and debug their systems/benchmarks either “as is” (and hit the breakpoints in the code) or being packed into docker containers (the same manner as components will be operated by the online platform). The SDK provides users with internal log messages from the containers, which make the debugging process more effective and less error-prone. 

# Usage
HowTo examples have been moved to the separated [repository](https://github.com/hobbit-project/java-sdk-example), which can be cloned for each new project. Here is the detailed manuals of the development process:
1. [Test/Debug the components as java code](https://github.com/hobbit-project/java-sdk/wiki/Debug-components-as-java-code)
1. [Build images and test/debug your components as docker containers](https://github.com/hobbit-project/java-sdk/wiki/Building-images-and-debugging-containers)
1. [Upload images to the online platform](https://github.com/hobbit-project/java-sdk/wiki/Upload-images-to-the-platform)

# Support & Feedback
Feel free to ask your questions and suggestions under the Issues tab. 
Also feel free to put feedback about benchmarks and systems, for which SDK have been helpful.

# Changelog
* **Revision 1:** RabbitMQ dynamic host problem resolved. Reusable DockerBuilders for components added.
* **Revision 2:** Namespaces changed. LocalEvalStorage, EvaluationModule (from examples) refactored. Full-managed hooks-listening mechanism implemented. Any SDK-specifics removed from example components. Wiki pages updated. Released as v1.0. 
