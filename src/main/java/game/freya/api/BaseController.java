package game.freya.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Base", description = "Основные точки (технические)")
@RequestMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
public class BaseController {

    @Operation(summary = "Пинг бэка", hidden = true)
    @GetMapping("/")
    public String welcome() {
        return "!!! Welcome !!!";
    }

    @Operation(summary = "Страница ошибки (теоретически должна быть)", hidden = true)
    @PostMapping("/error")
    public String err() {
        return "Err here...";
    }
}
