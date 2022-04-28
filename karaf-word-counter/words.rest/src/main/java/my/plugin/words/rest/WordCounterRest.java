package my.plugin.words.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import my.plugin.words.api.WordCounter;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;

@Component(service = WordCounterRest.class, immediate = true)
@Path("/words")
public class WordCounterRest {
    private static final ObjectMapper mapper = new ObjectMapper();
    private WordCounter wordCounter;

    @Reference
    void setWordCounter(WordCounter wordCounter) {
        this.wordCounter = wordCounter;
    }

    @POST
    @Path("count")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Operation(
            tags = "word-counter",
            summary = "Retrieves the word count for give text",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "JSON Object with Number of the count"),
                    @ApiResponse(responseCode = "403",
                            description = "Forbidden"),
                    @ApiResponse(responseCode = "500",
                            description = "INTERNAL SERVER ERROR"),
            }
    )
    public Response getWordCount(
            @Context UriInfo uriInfo,
            @Parameter(schema = @Schema(type = "String", description = "Word to count", required = true))
            @FormDataParam("word") String word,
            @Parameter(schema = @Schema(type = "String", description = "Text to search word", required = true))
            @FormDataParam("text") String text){
        int count = wordCounter.countWord(word, text);
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("count", count);
        return Response.ok(objectNode.toString()).build();
    }

    @POST
    @Path("countFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed("user")
    @Operation(
            tags = "word-counter",
            summary = "Retrieves the word count for give text file",
            responses = {
                    @ApiResponse(responseCode = "200",
                            description = "JSON Object with Number of the count"),
                    @ApiResponse(responseCode = "403",
                            description = "Forbidden"),
                    @ApiResponse(responseCode = "500",
                            description = "INTERNAL SERVER ERROR"),
            }
    )
    public Response getWordCountFile(
            @Context UriInfo uriInfo,
            @Parameter(schema = @Schema(type = "String", description = "Word to count", required = true))
            @FormDataParam("word") String word,
            @Parameter(schema = @Schema(type = "File", description = "File to search word", required = true))
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail

    ){
        // TODO add validation and error handling
        int count = wordCounter.countWord(word, fileInputStream);
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("count", count);
        return Response.ok(objectNode.toString()).build();
    }

}
