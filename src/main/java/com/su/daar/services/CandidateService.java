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

package com.su.daar.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.su.daar.document.Candidate;
import com.su.daar.helper.CustomLoggerProd;
import com.su.daar.helper.Indices;
import com.su.daar.search.SearchRequestDTO;
import com.su.daar.search.util.SearchUtil;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class CandidateService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(CandidateService.class);
    private final RestHighLevelClient client;


    @Autowired
    public CandidateService(RestHighLevelClient client) {
        this.client = client;
    }


    /**
     * Search for candidates based on data provided in the {@link SearchRequestDTO} DTO. For more info take a look
     * at DTO javadoc.
     *
     * @param dto DTO containing info about what to search for.
     * @return Returns a list of found candidates.
     */
    public List<Candidate> search(final SearchRequestDTO dto) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.CANDIDATE_INDEX,
                dto
        );

        return searchInternal(request);
    }


    /**
     * Search for candidates based on data provided in the {@link SearchRequestDTO} DTO. Matches only results indexed after the specified date.
     * @param dto Search criteria.
     * @param date Date to search from. 
     * @return A list of candidates matching the criteria in dto whose CVs have been indexed after the specified date. 
     */
    public List<Candidate> searchCreatedSince(final SearchRequestDTO dto, final Date date) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.CANDIDATE_INDEX,
                dto,
                date
        );

        return searchInternal(request);
    }


    /**
     * Search candidates who submitted their CV after a certain date.
     * @param date Date of indexing. 
     * @return A list of candidates matching the criteria in {@link SearchRequestDTO} dto whose CVs have been indexed after the specified date. 
     */
    public List<Candidate> searchCreatedSince(final Date date) {
        final SearchRequest request = SearchUtil.buildSearchRequest(
                Indices.CANDIDATE_INDEX,
                date
        );

        return searchInternal(request);
    }


    /**
     * Performs a search request and translates the result into a list of {@link Candidate} objects.
     * @param request
     * @return 
     */
    private List<Candidate> searchInternal(final SearchRequest request) {
        if (request == null) {
            LOG.warn("Failed to build search request. Request body is empty");
            return Collections.emptyList();
        }

        try {
            final SearchResponse response = client.search(request, RequestOptions.DEFAULT);

            final SearchHit[] searchHits = response.getHits().getHits();
            final List<Candidate> candidates = new ArrayList<>(searchHits.length);
            for (SearchHit hit : searchHits) {
                candidates.add(
                        MAPPER.readValue(hit.getSourceAsString(), Candidate.class)
                );
            }
            return candidates;
            
        } catch (Exception e) {
            LOG.error(""+e);
            return Collections.emptyList();
        }
    }


    /**
     * Indexes a CV.
     * @param candidate The {@code Candidate} instance containing the candidate information and the content of a CV.
     * @return A boolean indicating the success or failure of the indexing request. 
     */
    public Boolean index(final Candidate candidate) {
        try {
            final String candidateAsString = MAPPER.writeValueAsString(candidate);
            final IndexRequest request = new IndexRequest(Indices.CANDIDATE_INDEX);
            request.id(candidate.getId());
            request.source(candidateAsString, XContentType.JSON);
            final IndexResponse response = client.index(request, RequestOptions.DEFAULT);

            //successful indexing
            return response != null && response.status().equals(RestStatus.OK);

        } catch (final Exception e) {
            LOG.error(""+e);
            return false;
        }
    }


    /**
     * Retrieves a specific CV in the candidates' index. 
     * @param candidateId The id of the cv. 
     * @return Candidate information and CV content as text.
     */
    public Candidate getById(final String candidateId) {
        try {
            final GetResponse documentFields = client.get(
                    new GetRequest(Indices.CANDIDATE_INDEX, candidateId),
                    RequestOptions.DEFAULT
            );
            if (documentFields == null || documentFields.isSourceEmpty()) {
                return null;
            }

            return MAPPER.readValue(documentFields.getSourceAsString(), Candidate.class);
        } catch (final Exception e) {
            LOG.error(""+e);
            return null;
        }
    }


    //TODO
    //public void clean(String name);

}
