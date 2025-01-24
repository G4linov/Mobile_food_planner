//package com.example.nutrcouseproj.view
//
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageButton
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.nutrcouseproj.R
//import com.example.nutrcouseproj.model.Meal
//import androidx.core.content.ContextCompat
//import com.example.nutrcouseproj.activity.FindMealActivity
//
//class MealAdapter (private val meals: List<Meal>) : RecyclerView.Adapter<MealAdapter.MealViewHolder>() {
//
//    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val icon: ImageView = itemView.findViewById(R.id.meal_icon)
//        val title: TextView = itemView.findViewById(R.id.meal_title)
//        val calories: TextView = itemView.findViewById(R.id.meal_calories)
//        val addButton: ImageButton = itemView.findViewById(R.id.add_meal_button)
//        val additionalBlock: LinearLayout = itemView.findViewById(R.id.additional_block)
//        val itemCount: TextView = itemView.findViewById(R.id.item_count)
//        val expandButton: ImageButton = itemView.findViewById(R.id.expand_menu_button)
//        val mealMenu: LinearLayout = itemView.findViewById(R.id.meal_menu)
//        val productsContainer: LinearLayout = itemView.findViewById(R.id.products_container)
//        val noProductsText: TextView = itemView.findViewById(R.id.no_products_text)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_meal_block, parent, false)
//        return MealViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
//        val meal = meals[position]
//
//        // Установка данных основного блока
//        holder.title.text = meal.title
//        holder.calories.text = meal.calories
//        holder.itemCount.text = "${meal.details.size} шт"
//
//        // Очистка контейнера продуктов
//        holder.productsContainer.removeAllViews()
//
//        // Проверка, есть ли продукты
//        if (meal.details.isEmpty()) {
//            holder.noProductsText.visibility = View.VISIBLE
//        } else {
//            holder.noProductsText.visibility = View.GONE
//            for (product in meal.details) {
//                val productView = LayoutInflater.from(holder.itemView.context)
//                    .inflate(R.layout.item_product, holder.productsContainer, false)
//
//                val productName: TextView = productView.findViewById(R.id.product_name)
//                val productCalories: TextView = productView.findViewById(R.id.product_calories)
//                val productWeight: TextView = productView.findViewById(R.id.product_weight)
//
//                productName.text = product.name
//                productCalories.text = "${product.calories} ккал"
//                productWeight.text = "${product.weight} г"
//
//                holder.productsContainer.addView(productView)
//            }
//        }
//
//        // Обработка кнопки раскрытия/скрытия меню
//        holder.expandButton.setOnClickListener {
//            val isExpanded = holder.mealMenu.visibility == View.VISIBLE
//            if (isExpanded) {
//                holder.mealMenu.visibility = View.GONE
//                holder.expandButton.setImageResource(R.drawable.ic_expand_more)
//                holder.additionalBlock.setBackgroundResource(R.drawable.rounded_corner)// Изменить иконку на "свернуть"
//            } else {
//                holder.mealMenu.visibility = View.VISIBLE
//                holder.additionalBlock.setBackgroundResource(R.drawable.rounded_corner_top)
//                holder.expandButton.setImageResource(R.drawable.ic_expand_less) // Изменить иконку на "развернуть"
//            }
//        }
//
//        // Обработка кнопки добавления продукта
//        holder.addButton.setOnClickListener {
//            val context = holder.itemView.context
//            val intent = Intent(context, FindMealActivity::class.java)
//            context.startActivity(intent)
//        }
//    }
//
//    override fun getItemCount(): Int = meals.size
//}