# DAAR2 : CV indexing application

author : Eleni Pistiloglou
organisation : Sorbonne University Master of Informatics STL track
email : eleni.pistiloglou@etu.sorbonne-universite.fr

Developped for the course Développement d'Algorithmes pour des Applications Réticulaires

## Description

This project consists of a backend for indexing the content of an uploaded CV into an elastic search database 
and for interrogating the data base. 
It offers an API for upload and for search by keyword and/or date.  

## Running the application

To run the application one has to run the command 
The elastic search instance has to run at http://localhost:9200 . 

## API

### Uploading a CV

To upload a CV, a POST request has to be sent to localhost:8080/api/candidate/upload . 
The request body is sent in json format and can contain the following attributes: 
 • file : The CV file. Accepted formats are .pdf and .doc . 
 • name* : The name of the candidate.
 • email* : The candidate's email address.
 • exp : years of experience.
 • pos : The position seeked. The value can be one of Alternance, Stage, CDD, CDI.
 
The attributes marked with asterisc are mandatory. 

This is an example of a request created with Postman: 
![alt text](https://github.com/EleniPistiloglou/DAAR2/blob/main/uploadrequestexample.jpg?raw=true)


### CV search 
 
By id : 

To retrieve a CV using its id, send a GET request to localhost:8080/api/candidate/id, where id is the id of the requested CV. 
 
By date : 

To retrieve all CVs that have been uploaded after a certain date, send the following request : 
GET localhost:8080/api/candidate/date 
Use the format yyyy-mm-dd for date. The results appear in descending order. 
If the date is not specified no results will be returned. 

By keyword : 

Send a POST request to localhost:8080/api/candidate/search/keywords with a json body of the following structure : 
{
  "keywords": \["software engineer"\],
  "expRange": \[2,5\],
  "position": CDD
}

None of the attributes above are mandatory, but the request has to contain a body (empty or not).
A request with empty body returns all CVs in the index. 
One or both values of expRange can be null. 
To restrict the search on CVs uploaded after a certain date, send the request to localhost:8080/api/candidate/searchcreatedsince/date using the format yyyy-mm-dd for date. 
The results will appear in descending order of date. 

This is an example of a request created with Postman: 
![alt text](https://github.com/EleniPistiloglou/DAAR2/blob/main/searchrequestexample.jpg?raw=true)
