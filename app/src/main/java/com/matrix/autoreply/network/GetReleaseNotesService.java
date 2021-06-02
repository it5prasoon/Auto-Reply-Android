package com.matrix.autoreply.network;

import com.matrix.autoreply.model.GithubReleaseNotes;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface GetReleaseNotesService {
    @GET("/repos/it5prasoon/Auto-Reply-Android/releases")
    Call<List<GithubReleaseNotes>> getReleaseNotes();
}
