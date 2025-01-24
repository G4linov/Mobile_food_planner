package com.example.nutrcouseproj.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nutrcouseproj.R
import com.example.nutrcouseproj.clients.ApiClient
import com.example.nutrcouseproj.clients.TokenManager
import com.example.nutrcouseproj.model.DBProduct
import com.example.nutrcouseproj.model.Meal
import com.example.nutrcouseproj.model.MealCreate
import com.example.nutrcouseproj.service.MealService
import com.example.nutrcouseproj.service.ProductService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class FindMealActivity : AppCompatActivity() {

    private val mealService: MealService = ApiClient.getClient().create(MealService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_find_meal)

        val mealType = intent.getStringExtra("MEAL_TYPE") ?: "Прием пищи"
        val colorResId = intent.getIntExtra("COLOR_RES_ID", R.color.default_color)
        val pickedDate = intent.getStringExtra("PICKED_DATE") ?: ""

        val scrollView = findViewById<ScrollView>(R.id.product_scroll_view)
        val productListLayout = scrollView.findViewById<LinearLayout>(R.id.product_list_layout)

        Log.e("API_ERROR", "Дата пришла в формате: ${pickedDate}")

        val topBar = findViewById<LinearLayout>(R.id.top_bar)
        val titleTextView = findViewById<TextView>(R.id.title_text_view)

        topBar.setBackgroundResource(colorResId) // Устанавливаем цвет фона
        titleTextView.text = mealType

        val backButton = findViewById<ImageView>(R.id.back_button)

        backButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }

        val searchButton = findViewById<ImageView>(R.id.search_button)
        val searchEditText = findViewById<EditText>(R.id.search_edit_text)

        val token = TokenManager.getToken(this)
        Log.e("API_ERROR", "Прекол: ${token}")
        if (token == null) {
            Toast.makeText(this, "Токен отсутствует", Toast.LENGTH_SHORT).show()
            return
        }

        var isButtonClicked = false

        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !isButtonClicked) {
                isButtonClicked = true
                searchButton.performClick()
                // Сброс флага после небольшой задержки, если нужно
                searchEditText.postDelayed({ isButtonClicked = false }, 200)
                true
            } else {
                false
            }
        }

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim() // Получаем введенную строку
            if (query.isNotEmpty()) {
                fetchProducts(query, token, productListLayout, pickedDate) // Вызываем метод для запроса продуктов
            } else {
                productListLayout.removeAllViews()
                loadProducts(token, productListLayout, pickedDate)
            }
        }

        loadProducts(token, productListLayout, pickedDate)
    }

    private fun fetchProducts(query: String,token:String, productListLayout: LinearLayout, pickedDate: String) {

        mealService.searchProducts("Bearer $token", query).enqueue(object : Callback<List<DBProduct>> {
            override fun onResponse(call: Call<List<DBProduct>>, response: Response<List<DBProduct>>) {
                if (response.isSuccessful) {
                    val products = response.body()!!
                    productListLayout.removeAllViews()
                    for (product in products) {
                        addProductToView(product, productListLayout, pickedDate)
                    }
                } else {
                    Log.e("APIError", "Error: ${response.code()}")
                    Toast.makeText(applicationContext, "Ошибка при получении продуктов", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<DBProduct>>, t: Throwable) {
                Log.e("APIError", "Error loading data: ${t.message}", t)
                Toast.makeText(applicationContext, "Ошибка соединения с сервером", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadProducts(token: String, productListLayout: LinearLayout, pickedDate: String) {
        val productService: ProductService =
            ApiClient.getClient().create(ProductService::class.java)
        productService.getProducts("Bearer $token").enqueue(object : Callback<List<DBProduct>> {
            override fun onResponse(
                call: Call<List<DBProduct>>,
                response: Response<List<DBProduct>>
            ) {
                Log.e("API_ERROR", "Прекол: ${response.code()}")
                if (response.isSuccessful) {
                    val products = response.body()!!
                    for (product in products) {
                        addProductToView(product, productListLayout, pickedDate)
                    }
                } else {
                    Log.e("API_ERROR", "Прекол:")
                    Toast.makeText(

                        this@FindMealActivity,
                        "Не удалось загрузить продукты",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<List<DBProduct>>, t: Throwable) {
                Log.e("API_ERROR", "Ошибка загрузки продуктов: ${t.message}")
                Toast.makeText(this@FindMealActivity, "Ошибка загрузки данных", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun addProductToView(
        product: DBProduct,
        parentLayout: LinearLayout,
        pickedDate: String
    ) {
        val inflater = LayoutInflater.from(this)
        val productView = inflater.inflate(R.layout.product_item, parentLayout, false)

        val nameTextView = productView.findViewById<TextView>(R.id.product_name)
        val caloriesTextView = productView.findViewById<TextView>(R.id.product_calories)
        val dropdownMenu = productView.findViewById<LinearLayout>(R.id.additional_block_for_product)
        val productBlock = productView.findViewById<LinearLayout>(R.id.product_block)
        val tabsBlock = findViewById<LinearLayout>(R.id.tabs_bar)

        val quantityEditText = productView.findViewById<EditText>(R.id.quantity_value)
        val totalTextView = productView.findViewById<TextView>(R.id.total_value)

        nameTextView.text = product.name
        caloriesTextView.text = "${product.caloriesPer100g} ккал"

        parentLayout.addView(productView)

        // Управление видимостью меню
        productBlock.setOnClickListener {
            if (dropdownMenu.visibility == View.GONE) {
                dropdownMenu.visibility = View.VISIBLE
                tabsBlock.visibility = View.GONE
            } else {
                dropdownMenu.visibility = View.GONE
                tabsBlock.visibility = View.VISIBLE
            }
        }

        // Настройка кнопок в меню
        val portionButton = productView.findViewById<Button>(R.id.portion_button)
        val addToMealButton = productView.findViewById<ImageButton>(R.id.add_to_meal_button)

        var isButtonClicked = false

        quantityEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && !isButtonClicked) {
                isButtonClicked = true
                addToMealButton.performClick()
                // Сброс флага после небольшой задержки, если нужно
                quantityEditText.postDelayed({ isButtonClicked = false }, 200)
                true
            } else {
                false
            }
        }

        quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Ничего не делаем
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Берем введенное значение и пересчитываем калории
                val quantity = s.toString().toDoubleOrNull() ?: 0.0
                val totalCalories = (product.caloriesPer100g * quantity) / 100

                // Обновляем TextView с итоговым значением
                totalTextView.text = "${totalCalories.toInt()} ккал"
            }

            override fun afterTextChanged(s: Editable?) {
                // Ничего не делаем
            }
        })

        portionButton.setOnClickListener {
            Toast.makeText(this, "Выбрана порция для ${product.name}", Toast.LENGTH_SHORT).show()
        }

        addToMealButton.setOnClickListener {
            val productName = product.name
            val quantity = quantityEditText.text.toString().toIntOrNull() ?: 0
            val mealType = findViewById<TextView>(R.id.title_text_view).text.toString()

            if (productName.isEmpty() || quantity == null || quantity <= 0) {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Определяем тип приема пищи
            val mealTypeEnglish = when (mealType) {
                "Завтрак" -> "Breakfast"
                "Обед" -> "Lunch"
                "Ужин" -> "Dinner"
                "Перекус" -> "Snack"
                else -> null
            }

            if (mealTypeEnglish == null) {
                Toast.makeText(this, "Неизвестный тип приема пищи", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Получаем токен
            val token = TokenManager.getToken(applicationContext)
            if (token == null) {
                Toast.makeText(this, "Необходимо авторизоваться", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Создаём объект MealCreate
            val meal = MealCreate(
                date = parseDate(pickedDate), // Преобразуем дату
                food_name = productName,
                meal_type = mealTypeEnglish,
                amount = quantity,
            )

            mealService.addMeal(token = "Bearer $token", meal = meal)
                .enqueue(object : Callback<Meal> {
                    override fun onResponse(
                        call: Call<Meal>,
                        response: Response<Meal>
                    ) {
                        if (response.isSuccessful) {
                            val meals = response.body()
                            Log.d("APIResponse", "Meals received: $meals")
                            Toast.makeText(
                                applicationContext,
                                "$productName добавлен",
                                Toast.LENGTH_SHORT
                            ).show()
                            dropdownMenu.visibility = View.GONE
                            tabsBlock.visibility = View.VISIBLE
                        } else {
                            Log.e(
                                "APIError",
                                "Error: ${response.code()} - ${response.errorBody()?.string()}"
                            )
                            Toast.makeText(
                                applicationContext,
                                "Ошибка добавления продукта",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onFailure(call: Call<Meal>, t: Throwable) {
                        Log.e("APIError", "Error loading data: ${t.message}", t)
                        Toast.makeText(
                            applicationContext,
                            "Ошибка соединения с сервером",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun parseDate(date: String): String {
        // Получаем текущий год
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)

        // Указываем формат для парсинга "день месяц"
        val inputFormat = SimpleDateFormat("dd MMMM", Locale("ru"))
        val parsedDate = inputFormat.parse(date)

        // Создаем объект Calendar и устанавливаем текущий год
        val calendar = Calendar.getInstance()
        calendar.time = parsedDate
        calendar.set(Calendar.YEAR, currentYear)  // Устанавливаем текущий год

        // Преобразуем в формат "yyyy-MM-dd"
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return outputFormat.format(calendar.time)
    }
}