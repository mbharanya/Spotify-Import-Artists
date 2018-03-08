import com.wrapper.spotify.Api;
import com.wrapper.spotify.exceptions.WebApiException;
import com.wrapper.spotify.methods.AlbumRequest;
import com.wrapper.spotify.methods.ArtistRequest;
import com.wrapper.spotify.methods.ArtistSearchRequest;
import com.wrapper.spotify.models.Album;
import com.wrapper.spotify.models.Artist;
import com.wrapper.spotify.models.Track;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by XMBomb on 12.03.2017.
 */
public class App {
    private static final String ARTISTS_FILE_PATH = "res"+File.separator+"artists.txt";
    public static final int API_WAIT_TIME = 100;
    private static final String ACCESS_TOKEN_PATH = "res"+File.separator+"auth-key.txt";

    public static void main(String... args) throws IOException, WebApiException, InterruptedException {
        // Create an API instance. The default instance connects to https://api.spotify.com/.
        Api api = Api.builder().accessToken(FileUtils.readFileToString(new File(ACCESS_TOKEN_PATH))).build();

        List<String> artistNames = FileUtils.readLines(new File(ARTISTS_FILE_PATH), "UTF-8");

        List<String> artistIds = new ArrayList<>();

        artistNames.stream()
                .forEach(artistName -> {
                    ArtistSearchRequest request = api.searchArtists(artistName).build();
                    try {
                        Thread.sleep(API_WAIT_TIME);
                    } catch (InterruptedException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    }
                    try {
                        final List<Artist> artists = request.get().getItems();
                        if (artists.size() <= 0){
                            System.err.println("No Artist found for artistName "+artistName);
                            return;
                        }
                        final Artist artist = artists.get(0);
                        System.out.println("Working on "+artist.getName());
                        artistIds.add(artist.getId());
                    } catch (IOException e) {
                        System.err.println(e);
                        e.printStackTrace();
                    } catch (WebApiException e) {
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