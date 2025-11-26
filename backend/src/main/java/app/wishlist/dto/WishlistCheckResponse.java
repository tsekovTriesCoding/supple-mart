package app.wishlist.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WishlistCheckResponse {

    @JsonProperty("isInWishlist")
    private boolean isInWishlist;
}

