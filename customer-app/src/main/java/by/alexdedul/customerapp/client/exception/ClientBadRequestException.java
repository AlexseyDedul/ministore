package by.alexdedul.customerapp.client.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ClientBadRequestException extends RuntimeException {
    private final List<String> errors;

    public ClientBadRequestException(Throwable throwable, List<String> errors) {
        super(throwable);
        this.errors = errors;
    }
}
