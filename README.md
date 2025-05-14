# ApisAgro 🌾🐝

A mobile application for sustainable farming that combines AI-powered agricultural guidance with real-time bee activity tracking to optimize crop production and pollination.

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Installation](#installation)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## 🌟 Overview

ApisAgro is an innovative Android application designed to revolutionize sustainable farming practices. By leveraging modern technology, artificial intelligence, and real-time data tracking, it provides farmers with intelligent tools for crop rotation planning, bee activity monitoring, and agricultural guidance.

### 🎯 Key Objectives

- Facilitate sustainable farming through AI-powered recommendations
- Monitor and optimize bee pollination activities
- Provide real-time agricultural guidance via virtual assistant
- Create a data-driven ecosystem for modern farming

### 👥 Target Audience

- Small to medium-scale farmers
- Agricultural consultants
- Farming cooperatives
- Agricultural researchers

## ✨ Features

### 1. Crop Rotation Planner 🌱
- AI-powered crop rotation recommendations
- Historical data tracking
- Soil type compatibility analysis
- Custom duration planning

### 2. Bee Traffic Map 🗺️
- Real-time bee activity tracking
- GPS-based location mapping
- Activity level reporting (Low/Medium/High)
- Community data aggregation

### 3. Bee Sound Synthesizer 🔊
- Frequency generation (150-250 Hz)
- Attract pollinators using optimal sound waves
- Educational information about bee behavior
- Interactive frequency adjustment

### 4. Virtual Assistant 🤖
- 24/7 AI-powered agricultural guidance
- Natural language chat interface
- Context-aware responses
- Historical conversation tracking

## 🛠️ Technology Stack

### Frontend (Android)
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 35 (Android 15)
- **Key Libraries:**
  - Retrofit 2.9.0 (Networking)
  - Google Maps SDK 18.1.0
  - Compose Material 3
  - OkHttp 3 (HTTP Client)

### Backend
- **Language:** Python
- **Framework:** FastAPI
- **Database:** PostgreSQL
- **ORM:** SQLAlchemy
- **AI Integration:** Google Gemini AI
- **Environment:** python-dotenv

### External Services
- Google Maps API
- Google Gemini AI
- GPS Location Services

## 🏗️ Architecture

The application follows a three-tier architecture:

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Presentation   │────▶│  Application    │────▶│     Data        │
│     Layer       │     │     Layer       │     │     Layer       │
│  (Android App)  │     │  (FastAPI)      │     │  (PostgreSQL)   │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                        │                        │
        └────────────────────────┴────────────────────────┘
                             │
                    ┌─────────────────┐
                    │External Services│
                    │  (Google APIs)  │
                    └─────────────────┘
```

## 📦 Installation

### Prerequisites
- Android Studio Arctic Fox or later
- Python 3.8+
- PostgreSQL 12+
- Google Cloud Account (for Maps & Gemini API)

### Backend Setup

1. Clone the repository:
```bash
git clone https://github.com/yourusername/ApisAgro.git
cd ApisAgro/ApisAgro-backend
```

2. Create a virtual environment:
```bash
python -m venv venv
source venv/bin/activate  # On Windows: venv\Scripts\activate
```

3. Install dependencies:
```bash
pip install -r requirements.txt
```

4. Set up environment variables:
```bash
cp .env.example .env
# Edit .env with your configuration
```

5. Initialize the database:
```bash
alembic upgrade head
```

6. Run the backend server:
```bash
uvicorn main:app --reload
```

### Frontend Setup

1. Open Android Studio

2. Import the project:
   - File → Open → Select `ApisAgro-frontend` folder

3. Configure API keys:
   - In `AndroidManifest.xml`, replace `Your_Maps_api_key` with your Google Maps API key
   - Update `BASE_URL` in `RetrofitClient.kt` if needed

4. Build and run the application:
   - Select an emulator or device
   - Click "Run" (▶️)

## 📱 Usage

### First Time Setup
1. Launch the ApisAgro app
2. Grant location permissions when prompted
3. Explore the four main features from the dashboard

### Feature Guide

#### Crop Rotation Planner
1. Navigate to "Crop Rotation Planner"
2. Enter current crop, soil type, and duration
3. Click "Generate Plan" for AI recommendations
4. View and save rotation plans

#### Bee Traffic Map
1. Open "Bee Traffic Map"
2. Allow location access
3. Tap the map to select a location
4. Report bee activity level
5. View community bee reports

#### Bee Sound Synthesizer
1. Select "Bee Sound Synthesizer"
2. Adjust frequency slider (150-250 Hz)
3. Tap "Play Sound" to generate tones
4. Read educational information

#### Virtual Assistant
1. Open "Virtual Assistant"
2. Type your agricultural question
3. View AI-generated responses
4. Access chat history

## 📚 API Documentation

### Base URL
```
http://localhost:8000
```

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Health check |
| POST | `/chat` | Send message to virtual assistant |
| GET | `/chat` | Retrieve chat history |
| POST | `/bee-traffic` | Report bee activity |
| GET | `/bee-traffic` | Get all bee reports |
| POST | `/crop-rotation` | Create rotation plan |
| GET | `/crop-rotation` | Get all rotation plans |

### Example Request
```bash
curl -X POST "http://localhost:8000/crop-rotation" \
  -H "Content-Type: application/json" \
  -d '{
    "crop": "corn",
    "soil": "loamy",
    "duration": "3 months",
    "plan": "auto"
  }'
```



