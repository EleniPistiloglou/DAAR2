/**********************************************************************************************************************
 * 
 *                      CV querying application using elastic search indexing
 * 
 * University project developed for the master's course Developpement d'Algorithmes pour des Applications Reticulaires 
 * in Sorbonne Universite, 2021.
 * 
 * This file was inspired by the tutorial "How to connect to Elasticsearch from Spring Boot Application" 
 * (https://www.youtube.com/watch?v=IiZZAu2Qtp0) acompanied by the source code 
 * available at https://github.com/liliumbosniacum/elasticsearch
 * 
 **********************************************************************************************************************/

package com.su.daar.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.su.daar.document.Candidate;
import com.su.daar.helper.AcceptedCvFormats;
import com.su.daar.helper.Position;
import com.su.daar.search.SearchRequestDTO;
import com.su.daar.services.CandidateService;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;  

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateService service;
    private static final Logger LOG = LoggerFactory.getLogger(CandidateController.class);


    @Autowired
    public CandidateController(CandidateService service) {
        this.service = service;
    }


    /**
	 * Extracts and indexes the text content of an uploaded file. 
	 * @param file The file to parse
     * @return An http response indicating the success of the indexing.
	 */
    @PostMapping(value="/upload", consumes = "multipart/form-data")
    public ResponseEntity<String> uploadToLocalFileSystem(
			@RequestParam("file") MultipartFile file,  // a .pdf or .doc CV 
			@RequestParam("name") String name,   // candidate name
            @RequestParam("email") String email,  // email address of candidate
			@RequestParam("exp") int exp,    // years of experience of candidate
			@RequestParam("pos") String pos   // position they are applying for
    ) {

        if(file==null || !Candidate.checkEmail(email) || exp<0){
            return new ResponseEntity<>(
                "Required fields must be filled with valid values.", 
                HttpStatus.BAD_REQUEST
            );
        }

        LOG.info("uploading CV : "+file.getOriginalFilename()+", "+name+", "+email+", "+exp+", "+pos);

        Optional<AcceptedCvFormats> format = Arrays.asList(AcceptedCvFormats.values())
            .stream()
            .filter( f -> 
                Pattern.compile("(\\w)*"+f.toString()).matcher(file.getOriginalFilename()).matches()
            ).findFirst();

        if(format.isPresent()){

            Date d = new Date();  // timestamp
            String id = Candidate.idGen(name,d);  // each new cv has a unique id
            String fileName = "CV"+name.replace(' ', '_')+"_"+id;
            String content = ""; 
            // a cleaning is necessary before storing a new CV so that there are no more than one CVs for each candidate
            // service.clean(email); // cleaning will be available with the next version

            try {

                //copying the CV in a temporary file so that it can be converted to a String
                Files.copy(file.getInputStream(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);

                //parsing the file to create a String
                switch(format.get()){
                    case pdf:
                        content = parsePDF(fileName);
                        break;
                    case doc:
                        content = parseDOC(fileName);
                        break;                   
                }

                // indexing
                service.index(new Candidate(
                    id, 
                    name, 
                    email, 
                    content, 
                    exp, 
                    Position.valueOf(pos), 
                    d
                ));

                try{
                    Files.delete(Paths.get(fileName));
                }catch(Exception e){
                    LOG.error("Trying to delete file while in use : "+e);
                    //TODO:
                    // create a method that deletes all temporary files regularly if necessary
                }

                return new ResponseEntity<>(
                        "CV uploaded successfully", HttpStatus.OK);
                        
            } catch (IOException e) {
                LOG.error("IOException : "+e);
                return new ResponseEntity<>(
                    "There was a problem uploading the CV.", 
                    HttpStatus.BAD_REQUEST
                );
            } 

        }else{

            return new ResponseEntity<>(
                "Not supported format. Accepted formats are .pdf and .doc", HttpStatus.BAD_REQUEST
            ); 

        }
    }


    /**
     * Extracts the useful text of a pdf file.
     * example code from https://stackoverflow.com/questions/23813727/how-to-extract-text-from-a-pdf-file-with-apache-pdfbox
     * @param fileName The name of the file to parse
     * @return The text of the file
     * @throws IOException
     * @throws InvalidPasswordException
     */
    private String parsePDF(String fileName) throws InvalidPasswordException, IOException {
        PDDocument doc;
        File pdfFile = new File(fileName);
        doc = PDDocument.load(pdfFile);
        PDFTextStripper extractor = new PDFTextStripper();
        String content = extractor.getText(doc);
        doc.close();
        return content;
    }


    /**
     * Extracts the useful text of a .doc file.
     * Example code taken from https://stackoverflow.com/questions/7102511/how-read-doc-or-docx-file-in-java
     * @param fileName The name of the file to parse
     * @return The text of the file
     * @throws IOException
     */
    private String parseDOC(String fileName) throws IOException {
        WordExtractor extractor = null;
        File docFile = new File(fileName);
        FileInputStream fis = new FileInputStream(docFile.getAbsolutePath());
        HWPFDocument document = new HWPFDocument(fis);
        extractor = new WordExtractor(document);
        String content = extractor.getText();
        extractor.close();
        return content;
    }


    /**
     * Get CV by id from the candidate index in ES.
     * @param id The id of the cv
     * @return Content of cv
     */
    @GetMapping("/{id}")
    public Candidate getById(@PathVariable final String id) {
        return service.getById(id);
    }

    /**
     * Searches all candidates whose CV contains the words specified in the body of the request.
     * @param dto The body of the request.
     * @return A list of candidates.
     */
    @PostMapping("/search/keywords")
    public List<Candidate> search(@RequestBody final SearchRequestDTO dto) {
        return service.search(dto);
    }

    /**
     * Searches all candidates that fullfill the criteria defined in the body of the request and who have submitted their CV after the specified date. 
     * @param dto The body of the request.
     * @param date The earliest date of indexing of a CV. 
     * @return A list of candidates.
     */
    @PostMapping("/searchcreatedsince/{date}")
    public List<Candidate> searchCreatedSince(
            @RequestBody final SearchRequestDTO dto,
            @PathVariable
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            final Date date) {
        return service.searchCreatedSince(dto, date);
    }

    /**
     * Searches all candidates who have submitted their CV after the specified date. 
     * @param date The date limit. 
     * @return A list of candidates.
     */
    @GetMapping("/searchcreatedsince/{date}")
    public List<Candidate> searchCreatedSince(
            @PathVariable
            @DateTimeFormat(pattern = "yyyy-MM-dd")
            final Date date) {
        return service.searchCreatedSince(date);
    }

}