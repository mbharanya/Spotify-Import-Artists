# Spotify-Import-Artists

VERY simple script to import a given list `res/artists.txt` to spotify using the wonderful https://github.com/thelinmichael/spotify-web-api-java

How to use:
1. create `res/artists.txt` with a `\n` seperated list of your artists
2. create an API-Token, that allows you to set what artists you follow and save it to `res/auth-key.txt` see
https://developer.spotify.com/web-api/console/put-following/
3. run the app with (`java -jar spotify_import_artists-1.0-SNAPSHOT-jar-with-dependencies.jar`) 
4. execute the generated shell script `./commands.sh`
5. ???
6. Profit

If any artists could not be found by their name in Spotify, they will be written to notFound.txt in Step 3, so you can easily try to find them manually (maybe Spotify lists them under a different alias).

How to build:
1. execute `mvn clean compile assembly:single` to create the jar