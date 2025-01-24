package com.example.nutrcouseproj.view


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutrcouseproj.R
import com.example.nutrcouseproj.model.DBProduct

class   FindMealAdapter(
    private val dpProducts: List<DBProduct>,
    private val onProductClick: (DBProduct) -> Unit
) : RecyclerView.Adapter<FindMealAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.product_name)
        val calories: TextView = itemView.findViewById(R.id.product_calories)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = dpProducts[position]
        holder.name.text = product.name
        holder.calories.text = "${product.caloriesPer100g} ккал / 100 г"
        holder.itemView.setOnClickListener { onProductClick(product) }
    }

    override fun getItemCount(): Int = dpProducts.size
}