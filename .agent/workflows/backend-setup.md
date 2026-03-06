---
description: Create and save Database and Medication (Tablets) data flow
---

# Backend & Database Development Workflow

This workflow guides you through setting up a Database (DB) and implementing the "Tablets" (Medication/Inventory) persistence layer for the LifeFlow app.

## 1. Project Initialization
Set up the backend environment using FastAPI and SQLite/PostgreSQL.

// turbo
```powershell
mkdir lifeflow-backend
cd lifeflow-backend
python -m venv venv
./venv/Scripts/activate
pip install fastapi uvicorn sqlalchemy pydantic
```

## 2. Database (DB) Configuration
Define the core database engine and session management.

*   Create `database.py`: Configure SQLAlchemy `create_engine` and `sessionmaker`.
*   Create `models.py`: Define your database tables.

### Data Models
- **User Table**: `id`, `name`, `email`, `password_hash`.
- **Tablets Table**: `id`, `user_id`, `tablet_name`, `dosage`, `frequency`, `last_taken`.

## 3. Implement Create/Save Endpoints
Implement the logic to save "Tablets" into the DB.

1.  **Create Tablet Entry**:
    - Endpoint: `POST /tablets`
    - Logic: Receive JSON, validate with Pydantic, save to SQLAlchemy session.
2.  **Retrieve Tablets**:
    - Endpoint: `GET /tablets/{user_id}`
    - Logic: Query the DB for all tablets associated with a user.

## 4. Android Frontend Integration (Retrofit)
Connect the Android app to the new backend.

### Update `ApiService.kt`
```kotlin
interface ApiService {
    // ... existing auth endpoints ...

    @POST("/tablets")
    fun saveTablet(@Body tablet: TabletRequest): Call<BaseResponse>

    @GET("/tablets/{userId}")
    fun getTablets(@Path("userId") userId: Int): Call<List<TabletResponse>>
}
```

## 5. Deployment & Testing
Run the backend and test it using the Android emulator.

// turbo
```powershell
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
*Note: Use `http://10.0.2.2:8000` in Android code to reach the local server.*
