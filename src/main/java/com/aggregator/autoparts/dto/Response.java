package com.aggregator.autoparts.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Response {
    private List<SparePart> sparePartList = new ArrayList<>();
    private String error;
}
