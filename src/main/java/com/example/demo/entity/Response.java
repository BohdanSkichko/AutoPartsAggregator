package com.example.demo.entity;

import lombok.*;
import java.util.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@AllArgsConstructor
@Data

public class Response {
    private List<SparePart> sparePartList = new ArrayList<>();
}
