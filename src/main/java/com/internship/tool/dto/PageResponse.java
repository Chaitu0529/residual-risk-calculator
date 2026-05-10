package com.internship.tool.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * Redis-serializable wrapper for Spring Data Page.
 * Spring's PageImpl has no default Jackson constructor so it cannot be
 * deserialized from Redis. This DTO solves that problem.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<T> content;
    private final int pageNumber;
    private final int pageSize;
    private final long totalElements;
    private final int totalPages;
    private final boolean last;
    private final boolean first;

    @JsonCreator
    public PageResponse(
            @JsonProperty("content")      List<T> content,
            @JsonProperty("pageNumber")   int pageNumber,
            @JsonProperty("pageSize")     int pageSize,
            @JsonProperty("totalElements") long totalElements,
            @JsonProperty("totalPages")   int totalPages,
            @JsonProperty("last")         boolean last,
            @JsonProperty("first")        boolean first) {
        this.content       = content;
        this.pageNumber    = pageNumber;
        this.pageSize      = pageSize;
        this.totalElements = totalElements;
        this.totalPages    = totalPages;
        this.last          = last;
        this.first         = first;
    }

    /** Build a PageResponse from a Spring Data Page. */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast(),
                page.isFirst()
        );
    }

    public List<T> getContent()      { return content; }
    public int     getPageNumber()   { return pageNumber; }
    public int     getPageSize()     { return pageSize; }
    public long    getTotalElements(){ return totalElements; }
    public int     getTotalPages()   { return totalPages; }
    public boolean isLast()          { return last; }
    public boolean isFirst()         { return first; }
}
