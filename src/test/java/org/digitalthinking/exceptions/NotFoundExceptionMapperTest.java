package org.digitalthinking.exceptions;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NotFoundExceptionMapperTest {

    @Test
    @DisplayName("toResponse - 404 con estado y mensaje en JSON")
    void testToResponse() {
        Response res = new NotFoundExceptionMapper().toResponse(new NotFoundException("Cliente no encontrado con ID: 7"));

        assertEquals(404, res.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_TYPE, res.getMediaType());
        assertEquals(Map.of("status", 404, "message", "Cliente no encontrado con ID: 7"), res.getEntity());
    }
}
