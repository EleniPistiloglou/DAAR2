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

package com.su.daar.search.util;

import java.util.Date;
import java.util.List;

import com.su.daar.search.SearchRequestDTO;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchUtil {
    
    private SearchUtil() {}
    private static final Logger LOG = LoggerFactory.getLogger(SearchUtil.class);


    /**
     * Builds a search request.
     * @param indexName The index to search into.
     * @param dto The search criteria.
     * @return A search request.
     */
    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final SearchRequestDTO dto) {
        try {
          
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(getQueryBuilder(dto));

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            LOG.error(""+e);
            return null;
        }
    }

    /**
     * Builds a search request with date of submission restrictions.
     * @param indexName The index to search into.
     * @return A search request.
     */
    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final Date date) {
        try {
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(getQueryBuilder("created", date));

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            LOG.error(""+e);
            return null;
        }
    }

    /**
     * Builds a search request with date of submission restrictions.
     * @param indexName The index to search into.
     * @param dto The search criteria.
     * @param date The earliest date of submission of a document.
     * @return A search request.
     */
    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final SearchRequestDTO dto,
                                                   final Date date) {
        try {

            final BoolQueryBuilder searchQuery = getQueryBuilder(dto); // query for specified criteria in the request body
            final QueryBuilder dateQuery = getQueryBuilder("created", date);  // query for date
            searchQuery.must(dateQuery);
            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(searchQuery);

            //sort the CVs by date of submission
            builder = builder.sort("created", SortOrder.DESC);

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;

        } catch (final Exception e) {
            LOG.error(""+e);
            return null;
        }
    }

    /**
     * Builds a query for searching the keywords specified in dto into the content of a CV.
     * @param dto The keywords
     * @return An instance of {@code QueryBuilder}.
     */
    private static BoolQueryBuilder getQueryBuilder(final SearchRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        BoolQueryBuilder bq = new BoolQueryBuilder();

        // add filter for keywords
        List<String> keywords = dto.getKeywords();
        if(keywords != null)
        keywords.forEach(
            k -> bq.must(QueryBuilders.matchQuery("cvContent", k))
        );

        // add filter for experience
        if (dto.getExpRange() != null){
            if(dto.getExpRange().get(0) != null ){
                bq.must(
                    QueryBuilders.rangeQuery("exp")
                    .gte(dto.getExpRange().get(0))
                );
            }
            if (dto.getExpRange().size() > 1 ){
                bq.must(
                    QueryBuilders.rangeQuery("exp")
                    .lte(dto.getExpRange().get(1))
                );
            }
        }

        // add filter for position
        if(dto.getPosition() != null){
            bq.must(
                QueryBuilders.matchQuery("pos", dto.getPosition().toString())
            );
        }
        return bq;

    }
    
    private static QueryBuilder getQueryBuilder(final String field, final Date date) {
        return QueryBuilders.rangeQuery(field).gte(date);
    }

}
