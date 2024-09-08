package game.freya.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Base")
@RequestMapping("/")
public class BaseController {

    @GetMapping("/")
    public String welcome() {
        return "!!! Welcome !!!";
    }

    @PostMapping("/error")
    public String err() {
        return "Err here...";
    }
}
