package com.example.nutrcouseproj.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.nutrcouseproj.R
import com.example.nutrcouseproj.clients.ApiClient
import com.example.nutrcouseproj.clients.TokenManager
import com.example.nutrcouseproj.model.Meal
import com.example.nutrcouseproj.service.MealService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class HomeActivity : AppCompatActivity() {

    private lateinit var lunchCaloriesTextView: TextView
    private lateinit var lunchProductsContainer: LinearLayout
    private val mealService: MealService = ApiClient.getClient().create(MealService::class.java)
    private var selectedDate: String = getCurrentDate()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        val breakfastFindMealButton = findViewById<ImageView>(R.id.breakfast_findMeal_button)
        val lunchFindMealButton = findViewById<ImageView>(R.id.lunch_findMeal_button)
        val snackFindMealButton = findViewById<ImageView>(R.id.snack_findMeal_button)
        val dinnerFindMealButton = findViewById<ImageView>(R.id.dinner_findMeal_button)
        val pickedDayTextView = findViewById<TextView>(R.id.picked_date)
        val buttonWeightNotes = findViewById<ImageButton>(R.id.button_weight_notes)
        val settingsButton = findViewById<ImageButton>(R.id.settings_button)

        setupWeekDays()


        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        buttonWeightNotes.setOnClickListener {
            val intent = Intent(this, ProfileJournalActivity::class.java)
            startActivity(intent)
        }

        breakfastFindMealButton.setOnClickListener {
            navigateToFindMealScreen("Завтрак", R.color.orange)
        }
        lunchFindMealButton.setOnClickListener {
            navigateToFindMealScreen("Обед", R.color.green)
        }
        snackFindMealButton.setOnClickListener {
            navigateToFindMealScreen("Перекус", R.color.purple)
        }
        dinnerFindMealButton.setOnClickListener {
            navigateToFindMealScreen("Ужин", R.color.dark_blue)
        }

        val token = TokenManager.getToken(applicationContext)
        Log.d("TestToken", "Processing date: ${token}")

        mealService.getMealsForDay("Bearer $token", selectedDate)
            .enqueue(object : Callback<List<Meal>> {
                override fun onResponse(call: Call<List<Meal>>, response: Response<List<Meal>>) {
                    val something = response.body()
                    Log.d("APIResponse", "Meals received: $something")
                    if (response.isSuccessful) {
                        val meals = response.body()
                        Log.d("APIResponse", "Meals received: $meals")
                        updateUI(meals)
                    }
                }

                override fun onFailure(call: Call<List<Meal>>, t: Throwable) {
                    Log.e("APIError", "Error loading data: ${t.message}", t)
                    showError()
                }
            })
    }

    private fun setMealsForDay(token: String, selectedDate: Calendar){
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(selectedDate.time)
        mealService.getMealsForDay("Bearer $token", formattedDate)
            .enqueue(object : Callback<List<Meal>> {
                override fun onResponse(call: Call<List<Meal>>, response: Response<List<Meal>>) {
                    val something = response.body()
                    Log.d("APIResponse", "Meals received: $something")
                    if (response.isSuccessful) {
                        val meals = response.body()
                        Log.d("APIResponse", "Meals received: $meals")
                        updateUI(meals)
                    }
                }

                override fun onFailure(call: Call<List<Meal>>, t: Throwable) {
                    Log.e("APIError", "Error loading data: ${t.message}", t)
                    showError()
                }
            })
    }

    private fun updateUI(meals: List<Meal>?) {
        val defaultMeals = listOf("Breakfast", "Lunch", "Dinner", "Snack")

        if (meals == null) {
            return
        } else {

            val mealMap = meals.associateBy { it.title }

            defaultMeals.forEach { mealType ->
                // Найти соответствующие элементы
                val mealBlock = when (mealType) {
                    "Breakfast" -> findViewById<LinearLayout>(R.id.breakfast_block)
                    "Lunch" -> findViewById<LinearLayout>(R.id.lunch_block)
                    "Dinner" -> findViewById<LinearLayout>(R.id.dinner_block)
                    "Snack" -> findViewById<LinearLayout>(R.id.snack_block)
                    else -> null
                }
                val mealCaloriesTextView = when (mealType) {
                    "Breakfast" -> findViewById<TextView>(R.id.breakfast_calories)
                    "Lunch" -> findViewById<TextView>(R.id.lunch_calories)
                    "Dinner" -> findViewById<TextView>(R.id.dinner_calories)
                    "Snack" -> findViewById<TextView>(R.id.snack_calories)
                    else -> null
                }
                val productContainer = when (mealType) {
                    "Breakfast" -> findViewById<LinearLayout>(R.id.breakfast_products_container)
                    "Lunch" -> findViewById<LinearLayout>(R.id.lunch_products_container)
                    "Dinner" -> findViewById<LinearLayout>(R.id.dinner_products_container)
                    "Snack" -> findViewById<LinearLayout>(R.id.snack_products_container)
                    else -> null
                }

                if (mealBlock == null || mealCaloriesTextView == null || productContainer == null) return@forEach

                // Получить данные для текущего типа приёма пищи
                val meal = mealMap[mealType]

                if (meal != null) {
                    mealCaloriesTextView.text = "${meal.calories} калорий"
                    productContainer.removeAllViews() // Очистить старые данные

                    // Добавить продукты в контейнер
                    meal.details.forEach { detail ->
                        val productRow = LinearLayout(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(10, 2, 10, 2)
                            }
                            orientation = LinearLayout.HORIZONTAL
                        }

                        // Название продукта
                        val productNameTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f // Заполнение оставшегося пространства
                            )
                            text = "• ${detail.name}"
                            textSize = 16f
                            setPadding(10, 2, 10, 2)
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.meal_block_text
                                )
                            )
                        }

                        // Калории и вес
                        val productInfoTextView = TextView(this).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            text = "${detail.calories} ккал / ${detail.weight} г."
                            textSize = 16f
                            setPadding(10, 2, 10, 2)
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.meal_block_text
                                )
                            )
                        }

                        // Добавляем оба TextView в строку
                        productRow.addView(productNameTextView)
                        productRow.addView(productInfoTextView)

                        // Добавляем строку в контейнер
                        productContainer.addView(productRow)
                    }

                    // Сделать контейнер видимым/скрытым при клике на блок
                    mealBlock.setOnClickListener {
                        val isVisible = productContainer.visibility == View.VISIBLE
                        productContainer.visibility = if (isVisible) View.GONE else View.VISIBLE
                    }
                } else {
                    // Если данных нет, оставить "0 калорий"
                    mealCaloriesTextView.text = "0 калорий"
                    productContainer.visibility = View.GONE

                    // Отключить возможность раскрытия
                    mealBlock.setOnClickListener(null)
                }
            }
        }
    }

    private fun navigateToFindMealScreen(mealType: String, colorResId: Int) {
        val pickedDate = findViewById<TextView>(R.id.picked_date).text.toString()
        // Передаем данные в новое Activity
        val intent = Intent(this, FindMealActivity::class.java).apply {
            putExtra("MEAL_TYPE", mealType)
            putExtra("COLOR_RES_ID", colorResId)
            putExtra("PICKED_DATE", pickedDate)
        }
        startActivity(intent)
    }

    private fun showError() {
        Toast.makeText(this, "Ошибка загрузки данных. Попробуйте позже.", Toast.LENGTH_SHORT).show()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun setPickedDate() {
        // Найти TextView
        val pickedDateTextView = findViewById<TextView>(R.id.picked_date)

        // Форматировать текущую дату
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("d MMMM", Locale("ru"))

        // Установить дату в TextView
        pickedDateTextView.text = dateFormat.format(currentDate)
    }

    private fun setupWeekDays() {
        val days = listOf(
            findViewById<TextView>(R.id.circle_day_1),
            findViewById<TextView>(R.id.circle_day_2),
            findViewById<TextView>(R.id.circle_day_3),
            findViewById<TextView>(R.id.circle_day_4),
            findViewById<TextView>(R.id.circle_day_5),
            findViewById<TextView>(R.id.circle_day_6),
            findViewById<TextView>(R.id.circle_day_7)
        )

        val pickedDayTextView = findViewById<TextView>(R.id.picked_date)
        val calendar = Calendar.getInstance()

        // Перемещаем календарь к началу недели (понедельник)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val mondayOffset = if (dayOfWeek == Calendar.SUNDAY) -6 else Calendar.MONDAY - dayOfWeek
        calendar.add(Calendar.DAY_OF_MONTH, mondayOffset)

        // Создаём массив дат недели, начиная с понедельника
        val weekDates = (0..6).map { offset ->
            (calendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_MONTH, offset)
            }
        }

        // Определяем текущий индекс (сегодняшний день)
        val todayIndex =
            (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2).let { if (it < 0) 6 else it }

        // Устанавливаем активный день при старте
        days.forEachIndexed { index, textView ->
            textView.isSelected = index == todayIndex
            if (index == todayIndex) {
                val token = TokenManager.getToken(applicationContext)
                if (token != null) {
                    setMealsForDay(token, weekDates[index])
                }
                val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
                val formattedDate = dateFormat.format(weekDates[index].time)
                pickedDayTextView.text = formattedDate
            }
        }

        days.forEachIndexed { index, day ->
            day.setOnClickListener {
                days.forEach {
                    it.isSelected = false
                    it.setTypeface(null, Typeface.NORMAL) // Сбрасываем стиль
                }
                day.isSelected = true
                day.setTypeface(null, Typeface.BOLD) // Устанавливаем жирный текст для выбранного

                val token = TokenManager.getToken(applicationContext)
                if (token != null) {
                    setMealsForDay(token, weekDates[index])
                }
                val dateFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
                val formattedDate = dateFormat.format(weekDates[index].time)
                pickedDayTextView.text = formattedDate
            }
        }
    }
}

