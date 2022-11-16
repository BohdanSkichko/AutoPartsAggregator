package com.example.demo.entity;

import lombok.*;
import java.util.ArrayList;
import java.util.List;


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
