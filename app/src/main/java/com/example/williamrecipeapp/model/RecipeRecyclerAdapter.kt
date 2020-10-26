package com.example.williamrecipeapp.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.williamrecipeapp.R
import kotlinx.android.synthetic.main.recipe_entry_layout.view.*

class RecipeRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val imageUrl = "https://thetemp.000webhostapp.com/ReceipeApp/getRecipeImage.php?imageName=";
    private var items: List<Recipe> = ArrayList()
    private lateinit var onRecipeListener: OnRecipeListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RecipeViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.recipe_entry_layout, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is RecipeViewHolder -> {
                holder.bind(items[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun submitList(recipeList: List<Recipe>) {
        items = recipeList
    }

    fun setOnClickRecipeListener(onRecipeListener: OnRecipeListener) {
        this.onRecipeListener = onRecipeListener
    }

    inner class RecipeViewHolder constructor(itemView: View): RecyclerView.ViewHolder(itemView) {

        private val clRecipe = itemView.constaintLayoutRecipe
        private val recipeImage = itemView.imageViewRecipe
        private val recipeTitle = itemView.textViewRecipeTitle
        private val recipeType = itemView.textViewRecipeTypeTitle

        fun bind(recipe: Recipe) {
            this.recipeTitle.text = recipe.recipeTitle
            this.recipeType.text = recipe.recipeType

            if (!recipe.recipeImage.isNullOrBlank()) {
                recipeImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imageUrl + recipe.recipeImage)
                    .into(recipeImage)
            } else {
                recipeImage.visibility = View.GONE
            }
        }

        init {
            clRecipe.setOnClickListener {
                onRecipeListener.onRecipeClick(items[adapterPosition])
            }
        }

    }

    interface OnRecipeListener {
        fun onRecipeClick(recipe: Recipe)
    }

}