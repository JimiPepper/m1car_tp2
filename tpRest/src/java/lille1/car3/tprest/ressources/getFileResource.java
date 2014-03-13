/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package lille1.car3.tprest.ressources;

import java.io.File;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author gouzer
 */
@Path("/getFile")
public class getFileResource {

//    @GET
//    @Produces("text/html")
//    public String getXml() {
//        return "<html><body><h1>Heu je suis au bon endroit</h1></body></html>";
//    }
    /**
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response getFile() {
        File file = new File("/home/m1/gouzer/Bureau/sex.jpg");
        return Response.ok(file, MediaType.APPLICATION_OCTET_STREAM).build();
    }

    /**
     * PUT method for updating an instance of FilesResource
     *
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
//    @PUT
//    @Consumes("text/xml")
//    public void putXml(String content) {
//        this.putXml();
//    }
}
