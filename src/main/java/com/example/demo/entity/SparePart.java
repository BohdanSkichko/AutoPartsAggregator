package com.example.demo.entity;

import lombok.*;


@Getter
@Setter
@ToString
@EqualsAndHashCode

@NoArgsConstructor
@AllArgsConstructor
public class SparePart {
    private String url;
    private double cost;
    private String description;
}
