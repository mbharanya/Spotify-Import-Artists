import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.requests.data.search.simplified.SearchArtistsRequest;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    private static final String ARTISTS_FILE_PATH = "res"+File.separator+"artists.txt";
    private static final String NOT_FOUND_ARTISTS_FILE_PATH = "notFound.txt";
    private static final int API_WAIT_TIME = 100;
    private static final String ACCESS_TOKEN_PATH = "res"+File.separator+"auth-key.txt";
    private static final String DEFAULT_ENCODING = "UTF-8";

    public static void main(String... args) throws IOException{
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(FileUtils.readFileToString(new File(ACCESS_TOKEN_PATH), DEFAULT_ENCODING )).build();

        List<String> artistNames = FileUtils.readLines(new File(ARTISTS_FILE_PATH), "UTF-8");

        List<Artist> foundArtists = new ArrayList<>();
        List<String> notFoundArtists = new ArrayList<String>();

        artistNames
                .forEach(artistName -> {
                    SearchArtistsRequest request = api.searchArtists(artistName).build();
                    try {
                        Thread.sleep(API_WAIT_TIME);
                    } catch (InterruptedException e) {
						logError( e );
                    }
                    try {
                        Artist[] artists = request.execute().getItems();
                        if (artists.length <= 0){
                            System.err.println("No artist found for artistName "+artistName);
                            notFoundArtists.add(artistName);
                            return;
                        }
                        final Artist artist = artists[0];
                        System.out.println("Working on "+artist.getName());
                        foundArtists.add(artist);
                    } catch (IOException|SpotifyWebApiException e) {
                        logError( e );
                    }
                });

       String curlCommands = foundArtists.stream()
                .map(artist -> {
                    try {
                        return
                        "# "+artist.getName()+"\n"+
                        "curl -X PUT \"https://api.spotify.com/v1/me/following?type=artist&ids=" +
                                        artist.getId() +
                                        "\" -H \"Accept: application/json\" -H \"Authorization: Bearer " +
                                        FileUtils.readFileToString(new File(ACCESS_TOKEN_PATH), DEFAULT_ENCODING )+
                                        "\"\n";
                    } catch (IOException e) {
                        logError( e );
                    }
                    return "";
                })
                .collect(Collectors.joining("\n"));
       FileUtils.writeStringToFile(new File("commands.sh"), curlCommands, DEFAULT_ENCODING);

       if (notFoundArtists.size() > 0) {
           String notFoundArtistsString = "";
           for (String s : notFoundArtists) {
               if (notFoundArtistsString.isEmpty() == false) {
                   notFoundArtistsString = notFoundArtistsString + "\n" + s;
               }
               else {
                   notFoundArtistsString = s;
               }
           }
           FileUtils.writeStringToFile(new File(NOT_FOUND_ARTISTS_FILE_PATH), notFoundArtistsString, DEFAULT_ENCODING);
           System.out.println("The " + notFoundArtists.size() + " artist(s) which could not be found were written to " +
                   NOT_FOUND_ARTISTS_FILE_PATH + ".");
       }
    }

	private static void logError ( Exception e ){
		System.err.println(e.getMessage());
		e.printStackTrace();
	}
}