package com.example.williamrecipeapp

import com.example.williamrecipeapp.model.Recipe
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {

    @FormUrlEncoded
    @POST("login.php")
    fun login(
        @Field("email") email: String,
        @Field("pass") pass: String
    ) : Call<HashMap<String, String>>

    @FormUrlEncoded
    @POST("insertRecipe.php")
    fun insertRecipe(
        @Field("recipeType") recipeType: String,
        @Field("recipeTitle") recipeTitle: String,
        @Field("recipeIngredient") recipeIngredient: String,
        @Field("recipeStep") recipeStep: String,
        @Field("recipeImage") recipeImage: String,
        @Field("userID") userID: String
    ) : Call<HashMap<String, String>>

    @FormUrlEncoded
    @POST("updateRecipe.php")
    fun updateRecipe(
        @Field("recipeID") recipeID: String,
        @Field("recipeType") recipeType: String,
        @Field("recipeTitle") recipeTitle: String,
        @Field("recipeIngredient") recipeIngredient: String,
        @Field("recipeStep") recipeStep: String,
        @Field("recipeImage") recipeImage: String
    ) : Call<HashMap<String, String>>

    @FormUrlEncoded
    @POST("deleteRecipe.php")
    fun deleteRecipe(
        @Field("recipeID") recipeID: String,
        @Field("recipeImage") recipeImage: String
    ) : Call<HashMap<String, String>>

    @GET("selectRecipe.php")
    fun selectRecipe(
        @Query("userID") userID: String,
        @Query("criteria") criteria: String
    ) : Call<List<Recipe>>

    @GET("selectRecipeDetail.php")
    fun selectRecipeDetail(
        @Query("recipeID") recipeID: String
    ) : Call<Recipe>

}