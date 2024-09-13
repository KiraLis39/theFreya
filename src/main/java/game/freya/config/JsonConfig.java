package game.freya.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.awt.geom.Rectangle2D;

@Component
public class JsonConfig {
    private ObjectMapper mapper;

    @Bean
    public ObjectMapper getMapper() {
        if (mapper == null) {
            mapper = new ObjectMapper();

            mapper.registerModule(new JavaTimeModule());
//            mapper.registerModule(new JSR310Module());

            // для защиты от переполнения закольцованным AWT-методом getBounds2D():
            mapper.addMixIn(Rectangle2D.class, Rectangle2DJsonIgnore.class);
        }

        return mapper;
    }

    public interface Rectangle2DJsonIgnore {
        @JsonIgnore
        String getBounds2D();
    }
}
