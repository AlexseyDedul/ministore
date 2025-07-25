package by.alexdedul.feedbackservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document("favourite_product")
public class FavouriteProduct {
    @Id
    private UUID id;

    private int productId;
    private String userId;
}
