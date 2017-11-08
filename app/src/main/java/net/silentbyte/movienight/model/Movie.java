package net.silentbyte.movienight.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import net.silentbyte.movienight.tmdb.MovieApi;

import java.text.DecimalFormat;
import java.util.Objects;

/**
 * Model object and database entity for a single movie.
 */
@Entity(tableName = "movies")
public class Movie {

    @PrimaryKey
    private int id = 0;
    private String title = "";
    private String overview = "";
    @ColumnInfo(name = "poster_path")
    private String posterPath = "";
    @ColumnInfo(name = "release_date")
    private String releaseDate = "";
    private String certification = "?";
    private String genre = "?";
    private String language = "";
    @ColumnInfo(name = "user_review")
    private String userReview = "";
    private int runtime = 0;
    @ColumnInfo(name = "user_rating")
    private float userRating = 0;
    @ColumnInfo(name = "community_rating")
    private float communityRating = 0;
    @ColumnInfo(name = "vote_count")
    private int voteCount = 0;
    @ColumnInfo(name = "update_time")
    private long updateTime = 0;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getPosterUrl() {
        return posterPath == null ? null : MovieApi.BASE_POSTER_URL + posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getReleaseYear() {
        // Careful! Some movies have an empty release date string.
        if (releaseDate.isEmpty()) {
            return 0;
        }
        else {
            return Integer.valueOf(releaseDate.substring(0, 4));
        }
    }

    public String getFormattedReleaseYear() {
        int releaseYear = getReleaseYear();

        if (releaseYear == 0) {
            return "(????)";
        }
        else {
            return "(" + releaseYear + ")";
        }
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getCertification() {
        return certification;
    }

    public void setCertification(String certification) {
        this.certification = certification;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUserReview() {
        return userReview;
    }

    public void setUserReview(String userReview) {
        this.userReview = userReview;
    }

    public int getRuntime() {
        return runtime;
    }

    public String getFormattedRuntime() {
        int hours = 0;
        int minutes = 0;
        String formatted;

        if (runtime > 60) {
            hours = runtime / 60;
        }

        minutes = runtime % 60;

        if (hours == 0) {
            formatted = minutes + "min";
        }
        else if (minutes == 0) {
            formatted = hours + "h";
        }
        else {
            formatted = hours + "h " + minutes + "min";
        }

        return formatted;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public float getUserRating() {
        return userRating;
    }

    public String getFormattedUserRating() {
        DecimalFormat df = new DecimalFormat("0.0");
        String formatted = df.format(userRating);

        if (userRating == 0) {
            formatted = "0";
        }
        else if (userRating == 10) {
            formatted = "10";
        }

        return formatted;
    }

    public void setUserRating(float userRating) {
        this.userRating = userRating;
    }

    public float getCommunityRating() {
        return communityRating;
    }

    public void setCommunityRating(float communityRating) {
        this.communityRating = communityRating;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Movie) {
            return id == ((Movie) obj).id;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
