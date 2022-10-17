package entity;

import lombok.*;


import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
//@Entity
//@Table(name="spareParts")
@Data
public class SparePart implements Serializable {
    private static final long serialVersionUID = 1L;
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id")
    private Long id;

//    @Column(name = "name")
    private String name;

//    @Column(name = "serialNumber")
    private String serialNumber;

//    @Column(name = "cost")
    private int cost;

    public SparePart(String name, String serialNumber) {
        this.name = name;
        this.serialNumber = serialNumber;
    }
}
