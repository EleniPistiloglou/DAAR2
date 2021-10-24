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

package com.su.daar.search.util;

import com.su.daar.search.SearchRequestDTO;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;

public class SearchUtil {
    
    private SearchUtil() {}

    /**
     * Builds a search request.
     * @param indexName The index to search into.
     * @param dto The search criteria.
     * @return A search request.
     */
    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final SearchRequestDTO dto) {
        try {
            final int page = dto.getPage();
            final int size = dto.getSize();
            final int from = page <= 0 ? 0 : page * size;

            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .from(from)
                    .size(size)
                    .postFilter(getQueryBuilder(dto));

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
/*
    public static SearchRequest buildSearchRequest(final String indexName,
                                                   final String field,
                                                   final Date date) {
        try {
            final SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(getQueryBuilder(field, date));

            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }
*/
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
            final BoolQueryBuilder searchQuery = getQueryBuilder(dto);
            final QueryBuilder dateQuery = getQueryBuilder("created", date);

            searchQuery.must(dateQuery);

            SearchSourceBuilder builder = new SearchSourceBuilder()
                    .postFilter(searchQuery);

            //sort the CVs by date of submission
            builder = builder.sort("created", SortOrder.ASC);


            final SearchRequest request = new SearchRequest(indexName);
            request.source(builder);

            return request;
        } catch (final Exception e) {
            e.printStackTrace();
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

        List<String> keywords = dto.getKeywords();

        BoolQueryBuilder bq = new BoolQueryBuilder();

        keywords.forEach(
            k -> bq.must(QueryBuilders.matchQuery("cvContent", k))
        );

        return bq;

    }


/*
        final List<String> fields = dto.getFields();
        if (CollectionUtils.isEmpty(fields)) {
            return null;
        }

        if (fields.size() > 1) {
            final MultiMatchQueryBuilder queryBuilder = QueryBuilders.multiMatchQuery(dto.getSearchTerm())
                    .type(MultiMatchQueryBuilder.Type.CROSS_FIELDS)
                    .operator(Operator.AND);

            fields.forEach(queryBuilder::field);

            return queryBuilder;
        }

        return fields.stream()
                .findFirst()
                .map(field ->
                        QueryBuilders.matchQuery(field, dto.getSearchTerm())
                                .operator(Operator.AND))
                .orElse(null);*/
    
    
    private static QueryBuilder getQueryBuilder(final String field, final Date date) {
        return QueryBuilders.rangeQuery(field).gte(date);
    }

}
