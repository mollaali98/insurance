### Running the Spring server

Open a terminal and go to the project root directory and type:

```
./gradlew :clients:bootRun
```

After the server is running, a task is triggered. Those are the steps that task does
1. Create Corda Business Network(on **NetwprkOperator**) 
2. Request Membership for the other two nodes(on **Insurance** and **Client**)
3. Activate Membership(on **NetwprkOperator**)
4. Create Network SubGroup(on **NetwprkOperator**) 
5. Assign Business Network Identity for Insurance and Client(on **NetwprkOperator**) 
6. Assign Policy Issuer Role to Insurance(on **NetwprkOperator**)
7. Assign Policy Receiver Role to Client(on **NetwprkOperator**) 
8. For each node will create a user with a given role that will be used for authorisation purposed
    1. Spring creates a H2 Database that keeps users and roles for the authorisation
   
### Expose automated Swagger documentation for the application.
Open your browser and paste the following URL
```
http://localhost:8080/swagger-ui.html#/
```