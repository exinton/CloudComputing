# Intro
This is code base for CS 6343 - CLOUD COMPUTING

# How to use request generator
You are expected to re-write|modify files under sample/Request to get your own request generator, since io functions for each file system differ. The suggested way is to copy/past useful functions|code snippets to your own classes and do modifications accordingly.

Note for tree file outputed by listfs.py: the root directory name is number (directory XX), please replace the number XX to your own name, so multiple tree files can be concatenated together.

* SrcTest - We offer req/rand/ContentSrc, a file like object to fill content of file. You can upload from it to any nio channel, which can be also easily obtained from File object. This demos how to copy content to system.out. Of course, it's also ok to call external programs like dd.
* SinkTest - We also offer req/rand/ContentSink to digest file content you read.
* FSPropagate - This is demo on how to generate your file system structure from a tree output from listfs.py. You are expected to write a class implementing req/RequestCallback (re-write NullCall part), which takes file info (req/Request) as parameter, and call your own file|dir creation functions accordingly. For those who need to generate unbalanced requests, the return value is a list of node id on which files are allocated, and that list will be recorded to a rank file.
* ReqGenerator - This is sample request generator. It also expects you to offer your own req/RequestCallback (re-write dump) implementations for each request type. You might also consider adding more time profiling in run() method to get finer grain of performance. So far it only logs total number of request and average time for request|overhead|overall (request+overhead+sleep). The default implementation can also takes a ranking file to generate un-even request patent (call req/StaticTree.shuffleFilesUneven), else if the parameter is null, it generates zipf style patent, which favors files placed in front of tree file (if not shuffled), or shuffled order (if shuffled by calling req/StaticTree.shuffleFiles). 

# How to use iocontrol io lib
The heart of this piece of code is IOControl. It wraps over TCP socket and offers request/response interface as well as server in filter/handler structure.

A request is a self-contained Session object, into which you can insert any key/value, and extract from receiver side. A session has its MsgType, which you can define yourself by implementing MsgType interface.

A MsgFilter intercepts raw connect without extracting session (you should not extract session manually, The framework will extract for you). Multiple filters can be chained.

After that, session info is extract by the framework, and pass it to registered MsgHandler for specific MsgType. Also, multiple handlers can be chained. It's important to return false after responding to stop any further handler from giving response more than once.

Filters and handlers are registered by calling IOControl's registerXXX methods before any IO operations.

The supported IO operations are: send, request, response.

If IOControl.startServer is called, it is in server mode. Call waitServer to block your main thread if it has no other thing to do so it won't exit until server receives quit command. If IOControl.startServer is not called, it will be in client mode, and filters/handlers can still be used.

Please check FileReadEchoServer/FileWriteServer for use of MsgHandler, and RawLogger for MsgFilter usage.

# Samples
* LogPrintServer - A remote log server. You can redirect your log to it. It should be started before other server of coz.
* FileReadEchoServer - A simple server that responses to echo msg and also file read.
* EchoClient - Command line client that sends your input to SimpleServer.
* FileReadClient - Command line client that reads remote file given file path.
* FileWriteServer - A file server utilizing GFS like protocol.
* FileWiteClient - File upload client. Accept coomandline input.
* SystemInfo - Demo on how to get system load info.
* GracefulShutdownXXX - Demo on how to gracefully shutdown a connection

# Javadoc
Some javadoc is offered. So if you are unsure about how to use the code, first check samples, then read javadoc, then if it's still unclear, send me email.
