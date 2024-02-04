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
cltls <task> <group> [args]
```

### Task
- cltls cmd all ls -al  
- cltls cp all ~/goodday.txt
- cltls sync all 

### Parallel Task
- cltls pcmd all ls -al
- cltls pcp all ~/goodday.txt
- cltls psync all 

### Tools (developing)
- cltls ssh key exchange
- cltls hosts 

### Help
- cltls help



