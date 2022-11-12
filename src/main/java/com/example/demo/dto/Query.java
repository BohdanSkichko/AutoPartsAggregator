package com.example.demo.dto;
import lombok.*;

@Getter
@Setter
@ToString
@EqualsAndHashCode

@AllArgsConstructor
@RequiredArgsConstructor
@Data
public class Query {
    private String query;
}
