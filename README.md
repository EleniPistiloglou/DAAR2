# DAAR2 : CV indexing application

author : Eleni Pistiloglou <br />
organisation : Sorbonne University Master of Informatics, STL track <br />
email : eleni.pistiloglou@etu.sorbonne-universite.fr

Developped for the course Développement d'Algorithmes pour des Applications Réticulaires

## Description

This project consists of a backend created with Spring Boot for indexing the content of an uploaded CV into an elastic search database 
and for interrogating the data base.  <br />
It offers an API for upload and for search by keyword and/or date.  

## Running the application

The application runs on `localhost:8080`.  <br />
The address where the elastic search instance is running on has to be specified in application.properties file. The default is `http://localhost:9200`. 
To index log messages produced while sending requests, set the file name in logstash.conf to `springlogprod.log`, as in the example below, and the path to the project's folder.  
```
input {
  file {
    path => "C:/Users/user/DAAR2/springlogprod.log"
    start_position => "beginning"
  }
}

output {

  stdout {
    codec => rubydebug
  }

  elasticsearch {
   hosts => ["localhost:9200"]
  }
}
```

## API

### Uploading a CV

To upload a CV, a `POST` request has to be sent to `localhost:8080/api/candidate/upload` .  <br />
The request body is sent in json format and can contain the following attributes:  <br />
 - file : The CV file. Accepted formats are .pdf and .doc .  <br />
 - name : The name of the candidate. <br />
 - email : The candidate's email address. <br />
 - exp : years of experience. <br />
 - pos : The position sought. The value can be one of `Alternance` `Stage` `CDD` `CDI`. <br />
 
All attributes are mandatory.  <br />

This is an example of a request created with Postman:  <br />
![alt text](https://github.com/EleniPistiloglou/DAAR2/blob/main/uploadrequestexample.jpg?raw=true)


### CV search 
 
* **By id**

To retrieve a CV using its id, send a `GET` request to `localhost:8080/api/candidate/id`, where id is the id of the requested CV. 
 
* **By date**

To retrieve all CVs that have been uploaded after a certain date, send the following request :  <br />
`GET localhost:8080/api/candidate/date`  <br />
Use the format YYYY-MM-DD for date. The results appear in descending order.  <br />
If the date is not specified no results will be returned. 

* **By keyword**

Send a `POST` request to `localhost:8080/api/candidate/search/keywords` with a json body of the following structure :  <br />
```
{ 
  "keywords": ["software engineer"], 
  "expRange": [2,5], 
  "position": "CDD" 
} 
```

None of the attributes above are mandatory, but the request has to contain a body (empty or not). <br />
A request with empty body returns all CVs in the index. <br />
The second value of expRange is not mandatory. Null values are also accepted.  <br />
To restrict the search on CVs uploaded after a certain date, send the request to `localhost:8080/api/candidate/searchcreatedsince/date` using the format YYYY-MM-DD for date.  <br />
The results will appear in descending order of date.  <br />

This is an example of a request created with Postman:  
![alt text](https://github.com/EleniPistiloglou/DAAR2/blob/main/searchrequestexample.jpg?raw=true)
