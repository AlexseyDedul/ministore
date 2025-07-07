package by.alexdedul.feedbackservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductReview {

    private UUID id;
    private int productId;
    private int rating;
    private String review;
}
