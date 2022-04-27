###Startup

prerequisites: java11, maven, docker, docker-compose and free 8080 + 5432 ports

to startup application execute from project root dir:

```
./bin/startup-application.sh
```

access http://localhost:8080/swagger-ui/index.html

###couple more words about implementation

basically we have 2 db tables implemented.  
1. with employees and their main statuses (ADDED, IN_CHECK, APPROVED, ACTIVE).  
2. with employee check (basically it's all nested flow statuses). they're sectioned with check_region field to split states into regions and select always latest state within a region for further state-machine transitions and state recreation.  

employee_check table allows to easily extend nested flows (add new regions and statuses) if needed.   

you can find test scenarios mentioned in test task in com.example.demo.config.StateMachineConfigTest