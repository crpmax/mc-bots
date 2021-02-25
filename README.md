# Minecraft bot stress tester
A simple app written in Java used for stress testing Minecraft servers.  
It can be also used to test plugins or minigames.  
The MC version of the bots is 1.16.4.

## Features
- ✅ Connect as many bots as you want
- ✅ Receive colored or noncolored chat
- ✅ Set connection delay
- ✅ Set message or command on join
- ✅ Generate random or real looking nicknames

## Usage
Minimal Java version: 8  
Use of pre-compiled jar from [releases](https://github.com/crpmax/mc-bots/releases " releases"):  
`java -jar mc-bots.jar -s <server address> [arguments]`  
When running, you can write a chat message to the terminal to send it by all bots.

#### Options
`-c <count>` The count of bots to connect, default is 1  
`-d <min> <max>` Set the minimum and maximum connection delay range in ms, default is 4000-5000  
`-j <message>` The message or command to send on join, does not work with `-m` or `-x` option  
`-p <prefix>` Custom bot nickname prefix eg. `BOT_`  
`-r` Generate real looking nicknames instead of random ones  
`-n` Do not use color in terminal - useful when the terminal does not support it  
`-m` Minimal run - do not use any listeners, will not receive chat, useful for large amounts of bots  
`-x` The most minimal run - No listeners, no control, no chat - useful for large amounts of bots for better performance  

## DISCLAIMER
**This app is made for educational and testing purposes only  
I am not responsible for any abuse**
