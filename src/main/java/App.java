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

/**
 * Created by XMBomb on 12.03.2017.
 */
public class App {
    private static final String ARTISTS_FILE_PATH = "res"+File.separator+"artists.txt";
    public static final int API_WAIT_TIME = 100;
    private static final String ACCESS_TOKEN_PATH = "res"+File.separator+"auth-key.txt";

    public static void main(String... args) throws IOException{
        // Create an API instance. The default instance connects to https://api.spotify.com/.
        SpotifyApi api = new SpotifyApi.Builder().setAccessToken(FileUtils.readFileToString(new File(ACCESS_TOKEN_PATH))).build();

        List<String> artistNames = FileUtils.readLines(new File(ARTISTS_FILE_PATH), "UTF-8");

        List<String> artistIds = new ArrayList<>();

        artistNames.stream()
                .forEach(artistName -> {
                    SearchArtistsRequest request = api.searchArtists(artistName).build();
                    try {
                        Thread.sleep(API_WAIT_TIME);
                    } catch (InterruptedException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    try {
                        Artist[] artists = request.execute().getItems();
                        if (artists.length <= 0){
                            System.err.println("No Artist found for artistName "+artistName);
                            return;
                        }
                        final Artist artist = artists[0];
                        System.out.println("Working on "+artist.getName());
                        artistIds.add(artist.getId());
                    } catch (IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    } catch (SpotifyWebApiException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                });

       String curlCommands = artistIds.stream()
                .map(artistId -> {
                    try {
                        return "curl -X PUT \"https://api.spotify.com/v1/me/following?type=artist&ids=" +
                                        artistId +
                                        "\" -H \"Accept: application/json\" -H \"Authorization: Bearer " +
                                        FileUtils.readFileToString(new File(ACCESS_TOKEN_PATH))+
                                        "\"\n";
                    } catch (IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    return "";
                })
                .collect(Collectors.joining("\n"));
       FileUtils.writeStringToFile(new File("commands.sh"), curlCommands);
    }
}