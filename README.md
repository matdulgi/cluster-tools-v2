# Cluster Tools

### versions
- scala 2.13.10 
- java 1.8     
- sbt 1.9.8  

### Prerequisites
- ssh key change
- set PATH environment variable to use **cltls** command


# Usage
``` bash
cltls [parr] &lt;cmd> &lt;target> [args]
```

### Commands
- cltls cmd all ls -al  
- cltls cp all ~/goodday.txt

- cltls sync all 

### Parallel Commands

- cltls par cmd all ls -al
- cltls par cp all ~/goodday.txt
- cltls par sync all 

### Tools
- cltls ssh key exchange
- cltls hosts 

### Help
- cltls help
- cltls cmd help
- cltls sync help



