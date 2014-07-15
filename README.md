Verticle Story
==============

This experiment moves away from the common monolithic game, login, shop, and hub
servers approach and steers towards a more service-oriented architecture. Load
balancing will be implemented internally so that when a player makes a request
to connect to a channel, the current host may choose different instances (host
and port) in a round robin fashion. The only requirement is that there be one
login server because the login server port is constant. Theoretically, the
server should easily attain high availability and scale horizontally no matter
how skewed the player distribution may be on certain channels.

To reduce memory usage, there will be be an option to run these services on a
single process for low traffic servers hosted on low resource machines, although
a minimal level of redundancy is preferable. The hub server will be eliminated
and all communication will be peer to peer to reduce memory overhead and
latency. Instead of specialized processes, all instances should be able to
accept connections for the login server, game server, or shop server, though the
capacity of each instance can be tuned and optimized. To reduce the overhead of
context switches, much of the logic will be done in an asynchronous manner.
Vert.x will be used to coordinate communication among the modular daemons and to
operate the asynchronous event loop.