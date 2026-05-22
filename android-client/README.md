# PollCraft Mobile

Android client for the REST API server from lab 1.

## Implemented requirements

- Authentication page: `POST /api/auth/login/`
- Registration page: `POST /api/auth/register/`
- Web application workspace page:
  - app information from `GET /api/about/`
  - current user profile from `GET /api/auth/profile/`
  - polls list from `GET /api/polls/`
  - poll creation through `POST /api/polls/`
  - voting through `POST /api/polls/{id}/vote/`

## Technology stack

- Android Studio
- Kotlin
- Jetpack Compose
- Retrofit
- OkHttp
- kotlinx.serialization

## Run backend

From the repository root:

```powershell
cd .\lab1\backend
python manage.py runserver 0.0.0.0:8000
```

## Run Android app

1. Open the `android-client` folder in Android Studio.
2. Set `Settings -> Build, Execution, Deployment -> Build Tools -> Gradle -> Gradle JDK` to `Embedded JDK`.
3. Wait for Gradle Sync.
4. Select an emulator or a physical Android device.
5. Run the `app` configuration.

For Android Emulator use:

```text
http://10.0.2.2:8000/
```

For a physical Android device use the computer IP address in the same network, for example:

```text
http://192.168.1.10:8000/
```

The application allows cleartext HTTP because the local Django backend runs without HTTPS.
