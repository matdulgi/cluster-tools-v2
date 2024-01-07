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
cltls [par] <task> <group> [args]
```

### Task
- cltls cmd all ls -al  
- cltls cp all ~/goodday.txt
- cltls sync all 

### Parallel Task
- cltls par cmd all ls -al
- cltls par cp all ~/goodday.txt
- cltls par sync all 

### Tools (developing)
- cltls ssh key exchange
- cltls hosts 

### Help
- cltls help



