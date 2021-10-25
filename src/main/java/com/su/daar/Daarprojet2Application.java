/**********************************************************************************************************************
 * 
 *                      CV querying application using elastic search indexing
 * 
 * University project developed for the master's course Developpement d'Algorithmes pour des Applications Reticulaires 
 * in Sorbonne Universite, 2021.
 **********************************************************************************************************************/

package com.su.daar;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class Daarprojet2Application {

	public static void main(String[] args) {
		SpringApplication.run(Daarprojet2Application.class, args);
	}

	/**
	 * Simple front ent for file uploading.
	 * @return A page containing a form to fill in information about the candidate and to upload their cv. 
	 */
	@GetMapping
	public String uploadpage(){
		
		return "<!DOCTYPE html>"+
		"<html>"+
		"<head>"+
		"<script type = 'text/javascript' src='../../../../WebApp/index.js'>"+
		"</script>"+
		"</head>"+
		"<body>"+
		"<h1>Candidate profile</h1>"+

		"<form action='/upload' method='post' enctype='multipart/form-data'>"+
		"<label for='fname'>First name:</label>"+
		"<input type='text' id='fname' name='fname'><br><br>"+
		"<label for='lname'>Last name:</label>"+
		"<input type='text' id='lname' name='lname'><br><br>"+

		"<p>"+
		"<label for='cv'>CV upload: </label>"+
		"<input multiple id='cv' name='cv' type='file'>"+
		"</p>"+
		"<input type='submit' value='Submit'>"+

		"</form>"+
		"</body>"+
		"</html>";
	}

	
}

