a Java DDP Client
=================

A DDP client written in java for the Meteor framework (https://github.com/meteor/meteor)

Based on the node-js project by Alan Sikora, built on the java WebSocket of Nathan Rajlich and references the gson library

How to Use
==========

Download the entire source as a java project (will be adding jar shortly)

Example
=======

Examples located in 'examples' package of java project or can be used as simple as in the snippet below:

```java

DdpClient client = new DdpClient("localhost", 3000);
			
client.connect();

```

Acknowledgements
================

Meteor project - https://github.com/meteor/meteor

Based on node-js_ddp-client project - https://github.com/alansikora/node-js_ddp-client

Underlying java WebSocket - https://github.com/TooTallNate/Java-WebSocket - Copyright (c) 2010-2012 Nathan Rajlich

Referenced gson Java library - http://code.google.com/p/google-gson/ - http://www.apache.org/licenses/LICENSE-2.0