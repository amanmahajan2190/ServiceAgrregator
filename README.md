### What is this repository for? ###

The main purpose of this project is to try to solve the problem faced by clients fetching data from multiple services. 
Let's suppose that our request looks like this:
```
       A
     /   \
    B     C
  /  \   /  \
  D   E  F   G
  ```
Each node of the above graph is a request to the server and downward arrows represent requests that should be triggered
when the request above it is finished.
In the above example:
1. First, send a request to Server 'A' to fetch some data.
2. If the request to the Server 'A' was successful then trigger requests to Server 'B' and Server 'C'.
3. If the request to Server 'B' was successful then trigger a request to Server 'D' and 'E'. Similarly, do the same for C.

More realistic example which can exhibit similiar request pattern shown above can be as follows:
```
A => User basic info {First name, Last Name, Address}
B => User Enhanced info {Education and Last Job information}
C => Social Profile {Lastest Posts, Tweets, etc}
D => {Education Transcripts}
E => {Background check reports and Job Reports}
F => {Best tweets with images}
```

As we can see to get complete user profile for the user, a client needs to talk to multiple services which can different SLAs.
Rather than having all this complex logic on the client and preventing client's to send multiple requests we can push all this code
to the server side. The server side can be helpful as all the requests can leverage the higher bandwidth and higher computing power
of the datacenter which can help aggregating the whole payload.

### Contribution guidelines ###

Of course, Pull Requests are welcome.
This project is in the very initial state.

### Who do I talk to? ###

For any other information: 
rahul.kushwaha@gmail.com
