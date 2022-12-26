package com.example.demo.entity;

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
