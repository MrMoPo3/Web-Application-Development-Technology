# PollCraft

PollCraft - Web-додаток для створення опитувань, додавання варіантів відповідей, голосування та перегляду статистики.

Проєкт реалізовано на React, React Router, Redux Toolkit і Bootstrap. Дані зберігаються локально через `localStorage`, а сторінка опитувань також уміє завантажувати список із REST API Django через `createAsyncThunk`.

## Запуск frontend

```bash
npm install
npm run dev
```

## Запуск backend

```bash
cd backend
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
```

Автор: Левчук Іван Володимирович, КВ-12, Telegram - @vanya_levch

Звіт Лаб1: https://docs.google.com/document/d/1GlpRH3PLHfjDhW8wR8g3nW4nq7YSsm2VN5OglJajA_k/edit?usp=sharing
