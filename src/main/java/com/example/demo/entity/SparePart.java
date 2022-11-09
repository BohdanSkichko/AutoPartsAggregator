package com.example.demo.entity;

import lombok.*;
import nonapi.io.github.classgraph.json.Id;
import org.jsoup.select.Evaluator;


import javax.annotation.Generated;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Data
public class SparePart {

    private int id;
    private String url;
    private String serialNumber;
    private int cost;
    private String description;

}
