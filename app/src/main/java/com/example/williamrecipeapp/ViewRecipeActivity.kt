package com.example.williamrecipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import com.example.williamrecipeapp.model.Recipe
import com.example.williamrecipeapp.model.UserSetting
import kotlinx.android.synthetic.main.activity_view_recipe.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewRecipeActivity : AppCompatActivity() {

    private val imageUrl = "https://thetemp.000webhostapp.com/ReceipeApp/getRecipeImage.php?imageName=";
    private lateinit var userSetting: UserSetting
    private lateinit var recipeID: String
    private var recipeImage = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        // Action Bar
        supportActionBar?.title = getString(R.string.viewRecipe)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Share Preference
        userSetting = UserSetting(this)

        recipeID = intent.getStringExtra("recipeID") ?: ""

        // Button
        buttonEdit.setOnClickListener {
            val intentEdit = Intent(this@ViewRecipeActivity, EditRecipeActivity::class.java)
            intentEdit.putExtra("recipeID", recipeID)
            startActivity(intentEdit)
        }

        buttonDelete.setOnClickListener {
            val builder = AlertDialog.Builder(this@ViewRecipeActivity)

            builder.setTitle(R.string.delete)
            builder.setMessage("Are you sure want to delete recipe?")
            builder.setPositiveButton("Yes") { dialog, which ->
                deleteRecipe()
            }
            builder.setNegativeButton(android.R.string.no) { dialog, which ->
                dialog.dismiss()
            }

            val alertDialog = builder.create()
            alertDialog.show()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        if (recipeID.isNotEmpty()) {
            getRecipeDetail()
        }
    }

    private fun getRecipeDetail() {
        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call = apiInterface.selectRecipeDetail(recipeID)

        call.enqueue(object : Callback<Recipe> {
            override fun onFailure(call: Call<Recipe>, t: Throwable) {
                Toast.makeText(this@ViewRecipeActivity,
                    "Failed to retrieve recipe detail!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
            }

            override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                val recipe = response.body()

                val recipeTitle = recipe?.recipeTitle ?: ""
                if (recipeTitle.isNotEmpty()) {
                    supportActionBar?.title = "${getString(R.string.viewRecipe)} - $recipeTitle"
                }
                textViewRecipeTitle.text = recipeTitle

                textViewRecipeType.text = recipe?.recipeType ?: ""
                textViewRecipeIngredient.text = recipe?.recipeIngredient ?: ""
                textViewRecipeStep.text = recipe?.recipeStep ?: ""

                if (!recipe?.recipeImage.isNullOrEmpty()) {
                    recipeImage = recipe?.recipeImage ?: ""
                    Glide.with(this@ViewRecipeActivity)
                        .load(imageUrl + recipe?.recipeImage)
                        .into(imageViewRecipe)
                } else {
                    imageViewRecipe.setImageResource(0)
                }
            }

        })
    }

    private fun deleteRecipe() {
        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call = apiInterface.deleteRecipe(recipeID, recipeImage)

        call.enqueue(object : Callback<HashMap<String, String>> {
            override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                Toast.makeText(this@ViewRecipeActivity,
                    "Failed to delete recipe!!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
                Log.i("TAG", call.toString())
            }

            override fun onResponse(call: Call<HashMap<String, String>>,
                                    response: Response<HashMap<String, String>>) {
                val success = response.body()?.get("success") ?: "0"
                if (success == "1") {
                    val msg = response.body()?.get("message") ?: "Recipe deleted successfully!"
                    Toast.makeText(this@ViewRecipeActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.body()?.get("message") ?: "Something went wrong!"
                    Toast.makeText(this@ViewRecipeActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

}