package net.silentbyte.movienight.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Represents the release dates and associated certifications of a particular movie.
 * This models the response of a GET /movie/{movie_id}/release_dates request from TMDb.
 */
public class ReleaseDates {

    private int id;
    @SerializedName("results")
    private List<Country> results;

    public int getId() {
        return id;
    }

    public List<net.silentbyte.movienight.tmdb.ReleaseDates.Country> getResults() {
        return results;
    }

    /**
     * Represents a country and its associated release dates of a movie.
     */
    public class Country {
        @SerializedName("iso_3166_1")
        private String iso;
        @SerializedName("release_dates")
        List<net.silentbyte.movienight.tmdb.ReleaseDates.ReleaseDate> releaseDates;

        public String getIso() {
            return iso;
        }

        public List<net.silentbyte.movienight.tmdb.ReleaseDates.ReleaseDate> getReleaseDates() {
            return releaseDates;
        }
    }

    /**
     * Represents a particular release date and associated certification of a movie.
     */
    public class ReleaseDate {
        private String certification;
        @SerializedName("iso_639_1")
        private String iso;
        private String note;
        @SerializedName("release_date")
        private String releaseDate;
        int type;

        public String getCertification() {
            return certification;
        }

        public String getIso() {
            return iso;
        }

        public String getNote() {
            return note;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public int getType() {
            return type;
        }
    }
}
