package game.freya.api;

import game.freya.dto.roots.ItemDto;
import game.freya.entities.roots.Item;
import game.freya.exceptions.GlobalServiceException;
import game.freya.mappers.ItemsMapper;
import game.freya.services.ItemsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@Tag(name = "items")
@RequestMapping(value = "/item", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ItemController {
    private final ItemsService itemsService;
    private final ItemsMapper itemsMapper;

    @GMOnly
    @Operation(summary = "Create a new iStorable item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new iStorable created",
                    content = @Content(schema = @Schema(implementation = ItemDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "iStorable not created",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<ItemDto> createStorage(
            @Parameter(description = "iStorable model to create one")
            @RequestBody ItemDto dto
    ) {
        Optional<Item> saved = itemsService.createItem(itemsMapper.toEntity(dto));
        return saved.map(storage -> ResponseEntity.ok(itemsMapper.toDto(storage)))
                .orElse(ResponseEntity.internalServerError().build());
    }
}
