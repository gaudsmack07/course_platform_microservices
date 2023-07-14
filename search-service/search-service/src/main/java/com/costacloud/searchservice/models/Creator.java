package com.costacloud.searchservice.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "creators")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Creator {
    @Id
    private String id;
    private String name;

}
