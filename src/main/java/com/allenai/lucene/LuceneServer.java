package com.allenai.lucene;

import com.sun.jersey.spi.inject.Inject;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.lucene.queryparser.classic.ParseException;

@Path ("/server")
public class LuceneServer {
	@Inject
	Searcher searcher;
 
	@GET
	@Path("/get_score")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMaxIndex(
			@QueryParam("query") String query,
			@QueryParam("nresult") Integer nResult,
			@QueryParam("pathtocorpus") String pathToCorpus,
			@QueryParam("returndocs") String returnDocs,
			@QueryParam("analyzertype") String analyzerType,
			@QueryParam("similarity") String similarity
	) throws IOException, ParseException {
		System.out.println(" # Query: " + query);
		if (!searcher.indexIsOpen()) {
			System.out.println(" # Search, but need open Index");
			searcher.openIndex(pathToCorpus, similarity);
		}
		IndexScores resp = searcher.getScore(query, nResult, returnDocs.equals("yes"), analyzerType);
		return Response.status(200).entity(resp).build();
	}
 
	@GET
	@Path("/create_index")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createIndex(
			@QueryParam("pathtocorpus") String pathToCorpus,
			@QueryParam("byline") String byLine,
			@QueryParam("similarity") String similarity,
			@QueryParam("analyzertype") String analyzerType,
			@QueryParam("minlengthtext") String minLengthText
	) throws IOException, InterruptedException, java.text.ParseException {
		if (searcher.indexIsOpen()) {
			System.out.println(" # Create Index, but Searcher is use Index: need close index.");
			searcher.closeIndex();
		}
		System.out.println(" # Create Index Start");
		String statusCreateIndex = 
				IndexCreator.createIndex(pathToCorpus, similarity, byLine.equals("yes"), analyzerType, Integer.valueOf(minLengthText));
		System.out.println(" # Create Index Finish");
		return Response.status(200).entity("{\"status\":\"" + statusCreateIndex + "\"}").build();
	}
	
	@GET
	@Path("/close_index")
	@Produces(MediaType.APPLICATION_JSON)
	public Response closeIndex(
			@QueryParam("pathtocorpus") String pathToCorpus
	) throws IOException {
		if (searcher.indexIsOpen()) {
			System.out.println(" # Close Open Index.");
			searcher.closeIndex();
		} else {
			System.out.println(" # Index is now Сlosed.");
		}
		return Response.status(200).entity("{\"status\":\"Ok\"}").build();
	}
}
