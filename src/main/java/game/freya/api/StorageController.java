package game.freya.api;

import game.freya.dto.roots.StorageDto;
import game.freya.exceptions.GlobalServiceException;
import game.freya.services.StorageService;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@Tag(name = "storages")
@RequestMapping(value = "/storage", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @GMOnly
    @Operation(summary = "Create a new storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A new storage created",
                    content = @Content(schema = @Schema(implementation = StorageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "Storage not created",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @PostMapping("/create")
    public ResponseEntity<StorageDto> createStorage(
            @Parameter(description = "Storage model to create one")
            @RequestBody StorageDto dto
    ) {
        Optional<StorageDto> created = storageService.createStorage(dto);
        return created.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.internalServerError().build());
    }

    @GMOnly
    @Operation(summary = "Delete exists storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "A storage deleted",
                    content = @Content(schema = @Schema(implementation = StorageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "Storage wasn't deleted",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteStorage(
            @Parameter(description = "Storage uid for delete")
            @RequestParam UUID storageUid
    ) {
        return storageService.deleteStorageByUid(storageUid);
    }

    @GMOnly
    @Operation(summary = "Put some Item in the storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item was stored to the Storage",
                    content = @Content(schema = @Schema(implementation = StorageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "Item was not stored",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @PutMapping("/store")
    public ResponseEntity<HttpStatus> storeItemToStorage(
            @Parameter(description = "Destination storage uid", required = true, example = "7679803b-88c9-47bc-a1a1-163f4fa59cef")
            @RequestParam UUID storageUid,

            @Parameter(description = "Stored item uid", required = true, example = "b9804566-8377-4b69-b206-0d493cbd7d4c")
            @RequestParam UUID itemUid
    ) {
        return storageService.storeTo(storageUid, itemUid);
    }

    @GMOnly
    @Operation(summary = "Translate some Item to other storage")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Item was translated to other Storage",
                    content = @Content(schema = @Schema(implementation = StorageDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "404", description = "Item wasn't translated",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class))),
            @ApiResponse(responseCode = "500", description = "Server error",
                    content = @Content(schema = @Schema(implementation = GlobalServiceException.class)))
    })
    @PatchMapping("/translate")
    public ResponseEntity<HttpStatus> translateItemFromStorage(
            @Parameter(description = "Destination storage uid", required = true, example = "7679803b-88c9-47bc-a1a1-163f4fa59cef")
            @RequestParam UUID srcUid,

            @Parameter(description = "Destination storage uid", required = true, example = "7679803b-88c9-47bc-a1a1-163f4fa59cef")
            @RequestParam UUID dstUid,

            @Parameter(description = "Translated item uid", required = true, example = "b9804566-8377-4b69-b206-0d493cbd7d4c")
            @RequestParam UUID itemUid,

            @Parameter(description = "Translated items count", example = "1")
            @RequestParam(defaultValue = "1", required = false) int count
    ) {
        return storageService.translateItems(srcUid, dstUid, itemUid, count);
    }
}
