package com.api.rottentomatoes;

import com.api.rottentomatoes.model.Movie;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FetchMovies {	
	
	public List<Movie> getMovies(String message) throws IOException
	{
        List<Movie> movieContainer = new ArrayList<>();
		final String movieList = apiMovies(message);
		

		JsonParser jsonParser = new JsonParser();
		
		//fetch the movie id, name and release year for all the movies listed for the search 
		JsonObject mJsonObject = (JsonObject) jsonParser.parse(movieList);
		JsonArray movieArray = (JsonArray) mJsonObject.get("movies");		
		for (JsonElement m : movieArray) {
            final Movie movie = new Movie();

            final JsonObject child = (JsonObject) m;
			final String mId = child.get("id").getAsString();			
			final String mTitle = child.get("title").getAsString();
			final int mYear = child.get("year").getAsInt();

			movie.setmId(mId);
			movie.setmTitle(mTitle);
			movie.setmYear(mYear);

            movieContainer.add(movie);
		}	
		
		return movieContainer;
		
	}
	
	//Returns list of movies. Ten movies per search.
	public static String apiMovies(String message){
		
		String customURL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?q="+
		message+
		"&page_limit=50&page=1&apikey=zsnu54m8bqfzbct8ju4jz88g";
	
		StringBuilder output = new StringBuilder();
		String rw = "";
		try {
			URL url = new URL(customURL);
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			rw = br.readLine();
			while (rw!=null) {
				output.append(rw);
				rw = br.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return output.toString();
	}

}
