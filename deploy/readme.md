# SYSLAB Deploy client for Supervisor REST API

This client provides a simple command line client for using the distributed functionality of the Supervisor REST API.



## Getting started

This client can be used to deploy programs to the SYSLAB nodes, run and control them. You create a config file describing what files should be uploaded and how the program should be run. Any language can be used: Java, Python, Bash, C# Core etc.. As long as the nodes can run it, you can upload an run it with this Deploy client.

Using the REST API you can then send commands to the program running on multiple nodes e.g. to activate an experiment simultaneously or get the console logs (from multiple nodes at once), start and stop the program and much more. 

The REST API is distributed which means that you chose what nodes to control and the command is sent to the nodes in parallel for fastest execution time. You chose any node to be the executing host and send the command with a list of nodes to that node. That node will then distribute the command (or uploaded program) to the nodes in the list.




## Usage

``` 
usage: java -jar deploy.jar [options] <command>

options:
 -c, --config <filename>          configuration file to use
 -t, --token <token string>       JWT authentication token
 -n, --nodes <nodes string>       list of nodes in a comma delimited string
 -p, --program <program name>     the name of the program
 -a, --address <host address>     the deployment host to utilize
 -m, --message <message string>   send this <message> to program when command is 'send'
 -mn,--message <message string>   send <message> with newline appended at the end (simulate 'enter' click)
     --return-zero                always exit application with 0 status code
 -h, --help                       print this help
 
commands:
 upload                           upload program to nodes
 delete                           delete program from nodes
 update                           update configuration on nodes
 start                            start program on nodes
 stop                             stop  program on nodes
 status                           get information about program from nodes
 send                             send a message to program stdin on nodes
 list                             get a list of nodes known to the host
 cycle                            do sequence: stop, upload and start
```

All options can and should be defined in a config file. By mixing config files with options, it is e.g. possible to run a configuration against other nodes.

The token can be defined in the configuration or given on the command line and as such it is possible to run commands without a config file just using options, except `upload` (because a run-command is required which can only be defined in a config file).



The Supervisor REST API has more functionality than exposed by this client. The purpose of this client is simply to enable distributed deployment from the command line. E.g. to use in automated deployment via GitLab.