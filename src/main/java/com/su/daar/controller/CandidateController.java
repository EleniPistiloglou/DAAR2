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
 **********************************************************************************************************************/

package com.su.daar.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.su.daar.document.Candidate;
import com.su.daar.helper.CustomLogger;
import com.su.daar.helper.Position;
import com.su.daar.search.SearchRequestDTO;
import com.su.daar.services.CandidateService;

import org.apache.pdfbox.pdmodel.PDDocument;
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


@RestController
@RequestMapping("/api/candidate")
public class CandidateController {

    private final CandidateService service;
    private Logger loggerDev;
    private Logger loggerProd;

    @Autowired
    public CandidateController(CandidateService service) {
        this.service = service;
        this.loggerDev = CustomLogger.getLogger("CandidateController","springlogdev.log");
        this.loggerProd = CustomLogger.getLogger("CandidateController","springlogprod.log");
    }


    /**
	 * Extracts the text content of an uploaded pdf file and sends an indexing request.
	 * @param file The pdf file to parse
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
        System.out.println(file.getOriginalFilename());
        // recognise .pdf extension
        if( Pattern.compile("(\\w)+.pdf").matcher(file.getOriginalFilename()).matches() ) {

            Date d = new Date();  // timestamp
            String id = Candidate.idGen(name,d);  // each new cv has a unique id 
            // a cleaning is necessary before storing a new cv so that there are no more than one cvs for each candidate
            // service.clean(name); // cleaning will be available with the next version
            
            String fileName = "CV"+name.replace(' ', '_')+"_"+id;

            // storing a copy of the pdf file
            Path path = Paths.get(fileName);
            try {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(
                    "There was a problem uploading the CV.", 
                    HttpStatus.BAD_REQUEST
                );
            }

            File pdfFile = new File(fileName);

            //parsing the pdf file
            // example code from https://stackoverflow.com/questions/23813727/how-to-extract-text-from-a-pdf-file-with-apache-pdfbox
            PDDocument doc;
            try {
                doc = PDDocument.load(pdfFile);
                String content = (new PDFTextStripper()).getText(doc);
                //System.out.print(content);

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

                return new ResponseEntity<>(
                    "CV uploaded successfully", HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(
                    "There was a problem uploading the CV.", 
                    HttpStatus.BAD_REQUEST
                );
            }
        }

        // recognise .doc extension
        else if (Pattern.compile("(\\w)+.doc").matcher(file.getOriginalFilename()).matches() ){

            Date d = new Date();  // timestamp
            String id = Candidate.idGen(name,d);  // each new cv has a unique id 
            // a cleaning is necessary before storing a new cv so that there are no more than one cvs for each candidate
            // service.clean(name); // cleaning will be available with the next version
            
            String fileName = "CV"+name.replace(' ', '_')+"_"+id;

            // storing a copy of the pdf file
            Path path = Paths.get(fileName);
            try {
                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(
                    "There was a problem uploading the CV.", 
                    HttpStatus.BAD_REQUEST
                );
            }

            //parsing the doc file
            try {
               /* InputStream fileInputStream;
                fileInputStream = file.getInputStream();
                int nbrBytes = fileInputStream.available();
                byte[] buffer = new byte[nbrBytes];
                fileInputStream.read(buffer);
                String content = new String(buffer);
                fileInputStream.close();*/
                
                WordExtractor extractor = null;
                File docFile = new File(fileName);
                FileInputStream fis = new FileInputStream(docFile.getAbsolutePath());
                HWPFDocument document = new HWPFDocument(fis);
                extractor = new WordExtractor(document);
                String content = extractor.getText();

                System.out.println(content);

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

                return new ResponseEntity<>(
                    "CV uploaded successfully", HttpStatus.OK);

            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(
                    "There was a problem uploading the CV.", 
                    HttpStatus.BAD_REQUEST
                );
            }
        }

        // not supported format
        else{
            return new ResponseEntity<>(
                "Not supported format. Accepted formats are .pdf and .doc", HttpStatus.BAD_REQUEST
            ); 
        }
    }

    /**
     * Get CV by id.
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