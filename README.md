# Chat simulator
Uses Markov chains to simulate a chat.

#### Setup
Get a log from a chat program that looks like this:

`{
  "participants": [
    {
      "name": "Bobby B"
    },
    {
      "name": "Robby R"
    },
    {
      "name": "Sally S"
    }
  ],
  "messages": [
    {
      "sender_name": "Robby R",
      "timestamp_ms": 1222222222222,
      "content": "I'm fine thanks.",
      "type": "Generic"
    },
    {
      "sender_name": "Sally S",
      "timestamp_ms": 1222222211111,
      "content": "How are you today?",
      "type": "Generic"
    },
	...`
  
In theory any chatlog that looks like this will work, although the code is designed with chat logs exported from a *certain popular social media site*.  On said site, you can download your information under settings -> your [*social media site*] information.  You only need the data under "Messages".  This download system can be a bit tempremental so you might want to download the data in chunks split by date.

Clone this repo and set up the right folders (might have to add a couple):

`config.properties  corpus/  memory/  nbproject/  pom.xml  src/  target/`

The JSON data files should be saved in *corpus*.  If you got the data from mentioned social media site, then you'll have to unzip the content and dig out whichever chat log you want to use.

*config.properties* is not required, but if you do make it it can have the following parameters:

| Parameter   | Description                                                        | Default             |
| ----------- |--------------------------------------------------------------------| --------------------|
| learn       | boolean, wheter to learn from corpus file                          | true                |
| save        | boolean, whether to save learnt memory to *memory* directory       | false               |
| load        | boolean, whether to load memory from file, overwrites any learning | false               |
| dataToLearn | string, filepath of data to learn from                             | corpus/message.json |

#### Run
By default when you run the code, it will learn from corpus/message.json (can take a little while) then start printing out a generated message every five seconds.
