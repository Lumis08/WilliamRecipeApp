package com.example.williamrecipeapp

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.bumptech.glide.Glide
import com.example.williamrecipeapp.model.CustomSpinnerAdapter
import com.example.williamrecipeapp.model.Recipe
import com.example.williamrecipeapp.model.UserSetting
import kotlinx.android.synthetic.main.activity_view_recipe.*
import kotlinx.android.synthetic.main.activity_view_recipe.imageViewRecipe
import kotlinx.android.synthetic.main.activity_view_recipe.textViewRecipeTitle
import kotlinx.android.synthetic.main.content_create_update_recipe.*
import kotlinx.android.synthetic.main.recipe_entry_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.HashMap

class EditRecipeActivity : AppCompatActivity() {

    val IMAGE_PERMISSION_REQUEST_CODE = 101
    val IMAGE_RESULT_REQUEST_CODE = 1

    private val imageUrl = "https://thetemp.000webhostapp.com/ReceipeApp/getRecipeImage.php?imageName=";
    private lateinit var userSetting: UserSetting
    private lateinit var recipeID: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_recipe)

        // Action Bar
        supportActionBar?.title = getString(R.string.editRecipe)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Share Preference
        userSetting = UserSetting(this)

        recipeID = intent.getStringExtra("recipeID") ?: ""

        // Spinner
        val recipeTypeList = resources.getStringArray(R.array.recipeType).toMutableList()
        recipeTypeList.remove("All")
        val customSpinnerAdapter = CustomSpinnerAdapter(this@EditRecipeActivity, recipeTypeList)
        spinnerRecipeType.adapter = customSpinnerAdapter

        // Image View
        imageViewRecipe.setOnClickListener {
            selectImage()
        }

        // Button
        buttonCreateUpdate.setText(R.string.edit)
        buttonCreateUpdate.setOnClickListener {
            when {
                editTextRecipeIngredient.length() == 0 -> {
                    editTextRecipeIngredient.error = "Please enter the ingredient"
                }
                editTextRecipeStep.length() == 0 -> {
                    editTextRecipeStep.error = "Please enter the step"
                }
                else -> {
                    updateRecipe()
                }
            }
        }

        getRecipeDetail()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            if (requestCode == IMAGE_RESULT_REQUEST_CODE) {
                val selectedImage = data?.data
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImage)
                imageViewRecipe.setImageBitmap(bitmap)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                                            grantResults: IntArray) {
        if (requestCode == IMAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intentImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intentImage, IMAGE_RESULT_REQUEST_CODE)
            } else {
                Toast.makeText(this@EditRecipeActivity,
                    "Please provide permission to access storage", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getRecipeDetail() {
        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call = apiInterface.selectRecipeDetail(recipeID)

        call.enqueue(object : Callback<Recipe> {
            override fun onFailure(call: Call<Recipe>, t: Throwable) {
                Toast.makeText(this@EditRecipeActivity,
                    "Failed to retrieve recipe detail!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
            }

            override fun onResponse(call: Call<Recipe>, response: Response<Recipe>) {
                val recipe = response.body()

                val recipeTitle = recipe?.recipeTitle ?: ""
                if (recipeTitle.isNotEmpty()) {
                    supportActionBar?.title = "${getString(R.string.editRecipe)} - $recipeTitle"
                }
                editTextRecipeTitle.setText(recipeTitle)

                editTextRecipeIngredient.setText(recipe?.recipeIngredient ?: "")
                editTextRecipeStep.setText(recipe?.recipeStep ?: "")

                val recipeType = recipe?.recipeType ?: ""
                val index = resources.getStringArray(R.array.recipeType)
                    .toMutableList().indexOf(recipeType)
                spinnerRecipeType.setSelection(index - 1)

                if (!recipe?.recipeImage.isNullOrEmpty()) {
                    Glide.with(this@EditRecipeActivity)
                        .load(imageUrl + recipe?.recipeImage)
                        .into(imageViewRecipe)
                }
            }

        })
    }

    private fun updateRecipe() {
        Toast.makeText(this@EditRecipeActivity,
            "Editing the recipe ...", Toast.LENGTH_SHORT).show()

        val recipeType = spinnerRecipeType.selectedItem.toString()
        val recipeTitle = editTextRecipeTitle.text.toString()
        val recipeIngredient = editTextRecipeIngredient.text.toString()
        val recipeStep = editTextRecipeStep.text.toString()
        var recipeImage = ""
        if (imageViewRecipe.drawable != null) {
            val selectedBitmap = imageViewRecipe.drawable.toBitmap()
            recipeImage = getStringImage(selectedBitmap)
        }

        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call: Call<HashMap<String, String>> = apiInterface.updateRecipe(recipeID, recipeType,
            recipeTitle, recipeIngredient, recipeStep, recipeImage)

        call.enqueue(object : Callback<HashMap<String, String>> {
            override fun onFailure(call: Call<HashMap<String, String>>, t: Throwable) {
                Toast.makeText(this@EditRecipeActivity,
                    "Failed to edit recipe!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
            }

            override fun onResponse(call: Call<HashMap<String, String>>,
                                    response: Response<HashMap<String, String>>) {
                val success = response.body()?.get("success") ?: "0"
                if (success == "1") {
                    val msg = response.body()?.get("message") ?: "Recipe edited successfully!"
                    Toast.makeText(this@EditRecipeActivity, msg, Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    val msg = response.body()?.get("message") ?: "Something went wrong!"
                    Toast.makeText(this@EditRecipeActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }

        })
    }

    private fun selectImage() {
        val options = arrayOf<CharSequence>("Choose Image from Gallery", "Clear", "Cancel")

        val builder = AlertDialog.Builder(this@EditRecipeActivity)
        builder.setTitle("Choose the recipe image")

        builder.setItems(options) { dialog, which ->
            when (options[which]) {
                "Choose Image from Gallery" -> {
                    if (ContextCompat.checkSelfPermission(this@EditRecipeActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        ActivityCompat.requestPermissions(this@EditRecipeActivity,
                            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), IMAGE_RESULT_REQUEST_CODE)
                    } else {
                        val intentImage = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intentImage, IMAGE_RESULT_REQUEST_CODE)
                    }
                }
                "Clear" -> imageViewRecipe.setImageResource(0)
                "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun getStringImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageByte = baos.toByteArray()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Base64.getEncoder().encodeToString(imageByte)
        } else {
            ""
        }
    }

}