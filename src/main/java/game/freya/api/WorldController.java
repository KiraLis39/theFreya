package game.freya.api;

import game.freya.dto.roots.StorageDto;
import game.freya.dto.roots.WorldDto;
import game.freya.exceptions.GlobalServiceException;
import game.freya.services.WorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Tag(name = "Worlds")
@RequestMapping(value = "/world", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class WorldController {
    private final WorldService worldService;

    @GMOnly
    @Operation(summary = "Create a new world")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new world created",
                    content = @Content(schema = @Schema(implementation = WorldDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "World not created",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<WorldDto> createWorld(
            @Parameter(description = "World model to create one")
            @RequestBody WorldDto dto
    ) {
        WorldDto saved = worldService.saveOrUpdate(dto);
        if (saved == null) {
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok(saved);
    }

    @GMOnly
    @Operation(summary = "Delete exists world")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A world deleted",
                    content = @Content(schema = @Schema(implementation = StorageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "World wasn't deleted",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteWorld(
            @Parameter(description = "World uid for delete")
            @RequestParam UUID worldUid
    ) {
        return worldService.deleteByUid(worldUid) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
