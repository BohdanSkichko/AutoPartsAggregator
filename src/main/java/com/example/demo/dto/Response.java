package com.example.demo.dto;

import com.example.demo.entity.SparePart;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@ToString
@EqualsAndHashCode

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class Response  {
    private List<SparePart> sparePartList = new ArrayList<>();
}
