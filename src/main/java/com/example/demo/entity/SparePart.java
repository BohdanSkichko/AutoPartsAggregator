package com.example.demo.entity;

import lombok.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@Getter
@Setter
@ToString
@EqualsAndHashCode

@RequiredArgsConstructor
@AllArgsConstructor
@Data
public class SparePart {
    private String url;
    private double cost;
    private String description;
}
