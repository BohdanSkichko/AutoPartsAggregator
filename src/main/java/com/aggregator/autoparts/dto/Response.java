package com.aggregator.autoparts.dto;

import lombok.*;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@SessionAttributes("response")
public class Response {
    private List<SparePart> sparePartList = new ArrayList<>();
    private String error;
}
