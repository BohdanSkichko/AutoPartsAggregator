package com.carspareparts.finder.dto;

import lombok.*;
import java.util.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
public class Response {
    private List<SparePart> sparePartList = new ArrayList<>();
    private String error;
}
