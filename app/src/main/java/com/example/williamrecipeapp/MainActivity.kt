package com.example.williamrecipeapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.williamrecipeapp.model.CustomSpinnerAdapter
import com.example.williamrecipeapp.model.Recipe
import com.example.williamrecipeapp.model.RecipeRecyclerAdapter
import com.example.williamrecipeapp.model.UserSetting
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var userSetting: UserSetting
    private  lateinit var recipeRecyclerAdapter: RecipeRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Action Bar
        supportActionBar?.title = getString(R.string.myRecipes)

        // Share Preference
        userSetting = UserSetting(this)

        // Spinner
        val recipeTypeList = resources.getStringArray(R.array.recipeType).toList()
        val customSpinnerAdapter = CustomSpinnerAdapter(this@MainActivity, recipeTypeList)
        spinnerRecipeType.adapter = customSpinnerAdapter

        spinnerRecipeType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getRecipes(userSetting.getUserId() ?: "")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) { }
        }

        // Floating Action Button
        fabCreate.setOnClickListener {
            val intentCreate = Intent(this@MainActivity, CreateRecipeActivity::class.java)
            startActivity(intentCreate)
        }

        initRecyclerView()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.other_options_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.option_refresh -> {
                getRecipes(userSetting.getUserId() ?: "")
                return true
            }
            R.id.option_logout -> {
                val builder = AlertDialog.Builder(this@MainActivity)

                builder.setTitle(R.string.logout)
                builder.setMessage("Are you sure to logout?")
                builder.setPositiveButton("Yes") { dialog, which ->
                    userSetting.logout()
                    val loginIntent = Intent(this@MainActivity, LoginActivity::class.java)
                    startActivity(loginIntent)
                }
                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    dialog.dismiss()
                }

                val alertDialog = builder.create()
                alertDialog.show()

                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()

        if (!userSetting.isLoggedIn()) {
            val loginIntent = Intent(this, LoginActivity::class.java)
            startActivity(loginIntent)
        } else {
            getRecipes(userSetting.getUserId() ?: "")
        }
    }

    private fun initRecyclerView() {
        recyclerViewRecipe.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            recipeRecyclerAdapter = RecipeRecyclerAdapter()
            recipeRecyclerAdapter.setOnClickRecipeListener(object : RecipeRecyclerAdapter.OnRecipeListener {
                override fun onRecipeClick(recipe: Recipe) {
                    val intentView = Intent(this@MainActivity, ViewRecipeActivity::class.java)
                    intentView.putExtra("recipeID", recipe.recipeID)
                    startActivity(intentView)
                }

            })
            adapter = recipeRecyclerAdapter
        }
    }

    private fun getRecipes(userID: String) {
        recipeRecyclerAdapter.submitList(ArrayList())
        recipeRecyclerAdapter.notifyDataSetChanged()

        val apiInterface = ApiClient.getApiClient().create(ApiInterface::class.java)
        val call = apiInterface.selectRecipe(userID, spinnerRecipeType.selectedItem.toString())

        call.enqueue(object : Callback<List<Recipe>> {
            override fun onFailure(call: Call<List<Recipe>>, t: Throwable) {
                Toast.makeText(this@MainActivity,
                    "Failed to retrieve recipes!", Toast.LENGTH_SHORT).show()
                Log.i("TAG", t.localizedMessage ?: "Nothing")
            }

            override fun onResponse(call: Call<List<Recipe>>, response: Response<List<Recipe>>) {
                val recipes = response.body()
                if (!recipes.isNullOrEmpty()) {
                    recipeRecyclerAdapter.submitList(recipes)
                    recipeRecyclerAdapter.notifyDataSetChanged()
                    textViewNoRecipe.visibility = View.GONE
                } else {
                    textViewNoRecipe.visibility = View.VISIBLE
                }
            }

        })
    }

}