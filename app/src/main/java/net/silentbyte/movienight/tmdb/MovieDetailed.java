package net.silentbyte.movienight.tmdb;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the detailed set of information for a particular movie.
 * This models the response of a GET /movie/{movie_id} request from TMDb.
 */
public class MovieDetailed extends MovieBasic {

    @SerializedName("belongs_to_collection")
    private MovieCollection belongsToCollection;
    private int budget;
    private List<IdName> genres;
    private String homepage;
    @SerializedName("imdb_id")
    private String imdbId;
    @SerializedName("production_companies")
    private List<IdName> productionCompanies;
    @SerializedName("production_countries")
    private List<ProductionCountry> productionCountries;
    private int revenue;
    private int runtime;
    @SerializedName("spoken_languages")
    private List<SpokenLanguage> spokenLanguages;
    private String status;
    private String tagline;
    @SerializedName("release_dates")
    private ReleaseDates releaseDates;

    public MovieCollection getBelongsToCollection() {
        return belongsToCollection;
    }

    public int getBudget() {
        return budget;
    }

    public List<IdName> getGenres() {
        return genres;
    }

    public String getHomepage() {
        return homepage;
    }

    public String getImdbId() {
        return imdbId;
    }

    public List<IdName> getProductionCompanies() {
        return productionCompanies;
    }

    public List<ProductionCountry> getProductionCountries() {
        return productionCountries;
    }

    public int getRevenue() {
        return revenue;
    }

    public int getRuntime() {
        return runtime;
    }

    public List<SpokenLanguage> getSpokenLanguages() {
        return spokenLanguages;
    }

    public String getStatus() {
        return status;
    }

    public String getTagline() {
        return tagline;
    }

    public ReleaseDates getReleaseDates() {
        return releaseDates;
    }

    /**
     * The detailed movie response from TMDb doesn't contain a simple genre ids list.
     * We will instead extract the genre ids from the list of genres and return a new list.
     */
    @Override
    public List<Integer> getGenreIds() {
        List<Integer> genreIds = new ArrayList<>();

        for (IdName idName : genres) {
            genreIds.add(idName.id);
        }

        return genreIds;
    }

    /**
     * Represents the collection that a movie belongs to.
     */
    public class MovieCollection {
        private int id;
        private String name;
        @SerializedName("poster_path")
        private String posterPath;
        @SerializedName("backdrop_path")
        private String backdropPath;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPosterPath() {
            return posterPath;
        }

        public String getBackdropPath() {
            return backdropPath;
        }
    }

    /**
     * Generic ID/Name pair.
     * Used to represent both genre and production company.
     */
    public class IdName {
        private int id;
        private String name;

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Represents the country that produced the movie.
     */
    public class ProductionCountry {
        @SerializedName("iso_3166_1")
        private String iso;
        private String name;

        public String getIso() {
            return iso;
        }

        public String getName() {
            return name;
        }
    }

    /**
     * Represents the movie's spoken language.
     */
    public class SpokenLanguage {
        @SerializedName("iso_369_1")
        private String iso;
        private String name;

        public String getIso() {
            return iso;
        }

        public String getName() {
            return name;
        }
    }
}
