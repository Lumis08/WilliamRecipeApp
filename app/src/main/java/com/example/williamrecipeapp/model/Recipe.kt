package com.example.williamrecipeapp.model

data class Recipe (
    var recipeID: String,
    var recipeTitle: String,
    var recipeType: String,
    var recipeIngredient: String,
    var recipeStep: String,
    var recipeImage: String,
    var userID: String
)