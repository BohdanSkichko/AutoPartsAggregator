package com.aggregator.autoparts.dto;

import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Query {
    private String query;
    private int RegionId;
    private String SuggestionType;
}
