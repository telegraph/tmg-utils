
# Tmg Utils

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0) [![Build Status](https://jenkins-prod.api-platforms.telegraph.co.uk/job/Pipeline/job/tmg-utils/badge/icon)](https://jenkins-prod.api-platforms.telegraph.co.uk/job/Pipeline/job/tmg-utils/)

In this project we have a set of utilities that can help us speed up MicroService 
development. In it we can find:
 * [Base Utils](https://github.com/telegraph/tmg-utils/tree/master/base-utils) - contains a set of extensions and tools to manage configurations, 
 Settings, etc;
 * [Generic Client](https://github.com/telegraph/tmg-utils/tree/master/generic-client) - contains the monitoring mechanism and the *GenericClient* 
 definition;
 * [Http Client](https://github.com/telegraph/tmg-utils/tree/master/http-client) - *GenericClient* implementation for Http based on
 Akka Streams and Akka Http. It also adds some syntax sugar that allow us to reduce the boilerplate code; 
 * [Akka Server Extensions](https://github.com/telegraph/tmg-utils/tree/master/akka-server-ext) - Akka Http Server containing more Directives,
 Json4s support and embedded Monitoring;
 * [Play Server Extensions](https://github.com/telegraph/tmg-utils/tree/master/play-server-ext) - Play Http Server containing a pre-setup
 play extension;
    
