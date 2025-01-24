from datetime import datetime, timedelta, timezone
from typing import List

import bcrypt
import jwt
from fastapi import FastAPI, Depends, HTTPException, status, Query
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from pydantic import BaseModel
from sqlalchemy import Column, Integer, String, Float, Date, ForeignKey, text
from sqlalchemy.ext.asyncio import AsyncSession, create_async_engine
from sqlalchemy.future import select
from sqlalchemy.orm import relationship, declarative_base, selectinload
from sqlalchemy.orm import sessionmaker
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

# Разрешить все домены (или ограничьте только теми, которые вам нужны)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # можно ограничить список, если нужно
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# Configurations
SECRET_KEY = "your_secret_key"
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 30

oauth2_scheme = OAuth2PasswordBearer(tokenUrl="/auth/login")

# Database setup
DATABASE_URL = "sqlite+aiosqlite:///./calorie_tracker.db"
Base = declarative_base()
engine = create_async_engine(DATABASE_URL, echo=True)
SessionLocal = sessionmaker(
    bind=engine, class_=AsyncSession, expire_on_commit=False
)

# Models
class User(Base):
    __tablename__ = "users"
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)


class Food(Base):
    __tablename__ = "foods"
    id = Column(Integer, primary_key=True, index=True)
    name = Column(String, unique=True, nullable=False)
    calories_per_100g = Column(Integer, nullable=False)


class Meal(Base):
    __tablename__ = "meals"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    date = Column(Date, nullable=False)
    food_id = Column(Integer, ForeignKey("foods.id"), nullable=False)
    amount = Column(Integer, nullable=False)
    meal_type = Column(String, nullable=False)

    user = relationship("User")
    food = relationship("Food")


class DiaryEntry(Base):
    __tablename__ = "diary_entries"
    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    date = Column(Date, nullable=False)
    weight = Column(Float, nullable=True)
    comment = Column(String, nullable=True)


# Pydantic schemas
class UserCreate(BaseModel):
    username: str
    password: str


class Token(BaseModel):
    access_token: str
    token_type: str


class FoodCreate(BaseModel):
    name: str
    calories_per_100g: int


class MealCreate(BaseModel):
    date: datetime
    food_name: str  # Мы заменяем food_id на food_name
    amount: int
    meal_type: str


class DiaryEntryCreate(BaseModel):
    date: datetime
    weight: int = None
    comment: str = None


# Utility functions
async def get_db():
    async with SessionLocal() as session:
        yield session


def hash_password(password: str) -> str:
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')


def verify_password(plain_password: str, hashed_password: str) -> bool:
    return bcrypt.checkpw(plain_password.encode('utf-8'), hashed_password.encode('utf-8'))


def create_access_token(data: dict, expires_delta: timedelta = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.now(timezone.utc) + expires_delta
    else:
        expire = datetime.now(timezone.utc) + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)


async def get_current_user(token: str = Depends(oauth2_scheme), db: AsyncSession = Depends(get_db)):
    try:
        # Декодируем токен
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        username: str = payload.get("sub")

        # Проверка, что имя пользователя есть в токене
        if username is None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid authentication credentials")

        # Запрос через ORM
        query = await db.execute(select(User).filter(User.username == username))
        user = query.scalars().first()

        # Если пользователь не найден
        if user is None:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="User not found")

        return user
    except jwt.PyJWTError as e:
        # Логирование ошибки декодирования
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Could not validate credentials")
    except Exception as e:
        # Логирование прочих ошибок
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal Server Error")


# FastAPI app
app = FastAPI()


# Routes
@app.post("/auth/register", response_model=Token)
async def register(user: UserCreate, db: AsyncSession = Depends(get_db)):
    hashed_password = hash_password(user.password)
    db_user = User(username=user.username, hashed_password=hashed_password)
    db.add(db_user)
    await db.commit()
    token = create_access_token({"sub": user.username})
    return {"access_token": token, "token_type": "bearer"}


@app.post("/auth/login", response_model=Token)
async def login(form_data: OAuth2PasswordRequestForm = Depends(), db: AsyncSession = Depends(get_db)):
    try:
        # Использование where вместо filter
        query = await db.execute(select(User).where(User.username == form_data.username))
        user = query.scalars().first()

        if not user:
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incorrect username or password")

        if not verify_password(form_data.password, user.hashed_password):
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Incorrect username or password")

        token = create_access_token({"sub": user.username})
        return {"access_token": token, "token_type": "bearer"}
    except Exception as e:
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR, detail="Internal Server Error")


@app.post("/foods/", response_model=FoodCreate)
async def create_food(food: FoodCreate, db: AsyncSession = Depends(get_db),
                      current_user: User = Depends(get_current_user)):
    db_food = Food(name=food.name, calories_per_100g=food.calories_per_100g)
    db.add(db_food)
    await db.commit()
    return food


@app.post("/meals/", response_model=MealCreate)
async def add_meal(
    meal: MealCreate,
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Ищем продукт по названию
    product = await db.execute(
        select(Food).where(Food.name == meal.food_name)
    )
    product = product.scalar_one_or_none()

    if not product:
        raise HTTPException(status_code=404, detail="Продукт не найден")

    # Создаем новую запись приема пищи
    db_meal = Meal(
        user_id=current_user.id,
        date=meal.date,
        food_id=product.id,  # Используем food_id найденного продукта
        amount=meal.amount,
        meal_type=meal.meal_type  # Присваиваем meal_type
    )
    db.add(db_meal)
    await db.commit()
    return meal  # Возвращаем meal, чтобы подтвердить успешное добавление


@app.post("/diary/", response_model=DiaryEntryCreate)
async def create_diary_entry(entry: DiaryEntryCreate, db: AsyncSession = Depends(get_db),
                             current_user: User = Depends(get_current_user)):
    db_entry = DiaryEntry(user_id=current_user.id, date=entry.date, weight=entry.weight, comment=entry.comment)
    db.add(db_entry)
    await db.commit()
    return entry


@app.get("/meals/", response_model=List[dict])
async def get_meals_for_day(
    date: str = Query(..., description="Дата в формате yyyy-MM-dd"),
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Получение всех приёмов пищи пользователя за указанный день
    query = await db.execute(
        select(Meal)
        .filter(Meal.user_id == current_user.id, Meal.date == date)
        .options(selectinload(Meal.food))
    )
    meals = query.scalars().all()

    # Формирование ответа
    result = []
    for meal_type in set(meal.meal_type for meal in meals):
        meals_of_type = [meal for meal in meals if meal.meal_type == meal_type]
        total_calories = sum(meal.food.calories_per_100g * meal.amount / 100 for meal in meals_of_type)
        details = [
            {
                "name": meal.food.name,
                "calories": meal.food.calories_per_100g * meal.amount / 100,
                "weight": meal.amount
            }
            for meal in meals_of_type
        ]
        result.append({
            "title": meal_type,
            "calories": total_calories,
            "details": details
        })
    return result

@app.get("/products/", response_model=List[dict])
async def get_products(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    """
    Возвращает список продуктов. Доступ разрешён только авторизованным пользователям.
    """
    try:
        # Извлекаем все продукты из базы данных
        query = await db.execute(select(Food))
        foods = query.scalars().all()

        # Формируем ответ
        return [
            {"name": food.name, "caloriesPer100g": food.calories_per_100g}
            for food in foods
        ]
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ошибка получения списка продуктов"
        )

@app.get("/products/search")
async def search_products(
    db: AsyncSession = Depends(get_db),
    current_user: User = Depends(get_current_user),
    query: str = Query("", min_length=1)  # Параметр для поиска
):
    """
    Возвращает список продуктов, которые содержат строку запроса.
    Доступ разрешён только авторизованным пользователям.
    """
    try:
        # Извлекаем все продукты, если передан query, фильтруем по имени
        query_filter = f"%{query}%"  # Для поиска по частичному совпадению
        query_result = await db.execute(select(Food).filter(Food.name.ilike(query_filter)))
        foods = query_result.scalars().all()

        # Формируем ответ
        return [
            {"name": food.name, "caloriesPer100g": food.calories_per_100g}
            for food in foods
        ]
    except Exception as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Ошибка получения списка продуктов"
        )

@app.post("/setup/test3")
async def setup_test3(db: AsyncSession = Depends(get_db)):
    """Очистка базы данных и создание пользователя test3 с предварительно заполненными данными."""
    # Очистка базы данных
    await db.execute(text("DELETE FROM meals;"))
    await db.execute(text("DELETE FROM diary_entries;"))
    await db.execute(text("DELETE FROM foods;"))
    await db.execute(text("DELETE FROM users;"))
    await db.commit()

    # Создание пользователя test3
    hashed_password = hash_password("test3")
    db_user = User(username="test3", hashed_password=hashed_password)
    db.add(db_user)
    await db.commit()
    await db.refresh(db_user)

    # Добавление продуктов
    foods = [
        Food(name="Apple", calories_per_100g=52),
        Food(name="Banana", calories_per_100g=96),
        Food(name="Chicken Breast", calories_per_100g=165),
    ]
    db.add_all(foods)
    await db.commit()

    # Обновление ID продуктов
    food_ids = {food.name: food.id for food in foods}

    # Добавление записей о приёмах пищи
    meals = [
        Meal(user_id=db_user.id, date=datetime(2025, 1, 20), food_id=food_ids["Apple"], amount=200,
             meal_type="Breakfast"),
        Meal(user_id=db_user.id, date=datetime(2025, 1, 20), food_id=food_ids["Banana"], amount=150, meal_type="Snack"),
        Meal(user_id=db_user.id, date=datetime(2025, 1, 20), food_id=food_ids["Chicken Breast"], amount=250,
             meal_type="Dinner"),
        Meal(user_id=db_user.id, date=datetime(2025, 1, 19), food_id=food_ids["Apple"], amount=150,
             meal_type="Breakfast"),
        Meal(user_id=db_user.id, date=datetime(2025, 1, 19), food_id=food_ids["Chicken Breast"], amount=300,
             meal_type="Lunch"),
    ]
    db.add_all(meals)
    await db.commit()

    # Добавление записей в дневник
    diary_entries = [
        DiaryEntry(user_id=db_user.id, date=datetime(2025, 1, 20), weight=70.5, comment="Feeling good"),
        DiaryEntry(user_id=db_user.id, date=datetime(2025, 1, 21), weight=70.3, comment="Light workout"),
        DiaryEntry(user_id=db_user.id, date=datetime(2025, 1, 24), weight=70.0, comment="End of the week"),
    ]
    db.add_all(diary_entries)
    await db.commit()

    return {"message": "Setup complete for user test3"}
