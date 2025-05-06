import time
import os
import logging
from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List
from pydantic import ValidationError
from dotenv import load_dotenv
from google.api_core.exceptions import GoogleAPIError
from database import SessionLocal, engine
import models
import schemas
import json
import google.generativeai as genai

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("apisagro")

# Load environment and configure Gemini
load_dotenv()
genai.configure(api_key=os.getenv("GEMINI_API_KEY"))
model = genai.GenerativeModel("models/gemini-2.0-flash", generation_config={"temperature": 0.7})

# Create DB tables
models.Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="ApisAgro Backend",
    docs_url="/docs",
    redoc_url="/redoc"
)

@app.get("/")
def root():
    return {"status": "API is running"}

# DB Session Dependency
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ==================== CHAT ENDPOINTS ====================

def generate_gemini_reply(prompt: str, max_retries: int = 3) -> str:
    attempt = 0
    while attempt < max_retries:
        try:
            response = model.generate_content(prompt)
            return response.text.strip()
        except Exception as e:
            logger.warning(f"Gemini call failed: {e}")
            time.sleep(1 + attempt)
            attempt += 1
    raise HTTPException(status_code=502, detail="Gemini service failed after retries")

@app.post("/chat", response_model=schemas.ChatResponse)
def create_chat(chat: schemas.ChatCreate, db: Session = Depends(get_db)):
    try:
        # Validate input
        if not chat.message or not isinstance(chat.is_user, bool):
            raise HTTPException(status_code=400, detail="Invalid input")

        # Store user message
        user_chat = models.Chat(**chat.dict())
        db.add(user_chat)
        db.commit()
        db.refresh(user_chat)

        if chat.is_user:
            # Call Gemini and store response
            prompt = f"Provide a short and concise one-paragraph reply to: {chat.message}"
            reply = generate_gemini_reply(prompt)
            ai_chat = models.Chat(message=reply, is_user=False)
            db.add(ai_chat)
            db.commit()
            db.refresh(ai_chat)
            print(json.dumps({"id": ai_chat.id, "message": ai_chat.message, "is_user": ai_chat.is_user, "timestamp": ai_chat.timestamp.isoformat()}))  # Debugging line
            return ai_chat  # This will match ChatResponse schema

        return user_chat  # If not triggering Gemini, return just the user input

    except ValidationError as e:
        logger.error(f"Validation failed: {e}")
        raise HTTPException(status_code=422, detail=str(e))
    except Exception as e:
        logger.error(f"Internal error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

@app.get("/chat", response_model=List[schemas.ChatResponse])
def get_chats(db: Session = Depends(get_db)):
    chats = db.query(models.Chat).order_by(models.Chat.timestamp).all()
    return chats

# ==================== BEE TRAFFIC ENDPOINTS ====================

@app.post("/bee-traffic", response_model=schemas.BeeTrafficResponse, status_code=status.HTTP_201_CREATED)
def create_bee_traffic(report: schemas.BeeTrafficCreate, db: Session = Depends(get_db)):
    try:
        report_entry = models.BeeTraffic(**report.dict())
        db.add(report_entry)
        db.commit()
        db.refresh(report_entry)
        return report_entry
    except Exception as e:
        db.rollback()
        logger.exception("Bee traffic error")
        raise HTTPException(status_code=500, detail="Failed to save bee traffic report")

@app.get("/bee-traffic", response_model=List[schemas.BeeTrafficResponse])
def get_bee_traffic(db: Session = Depends(get_db)):
    reports = db.query(models.BeeTraffic).all()
    if not reports:
        raise HTTPException(status_code=404, detail="No bee traffic reports found")
    return reports

# ==================== CROP ROTATION ENDPOINTS ====================

@app.post("/crop-rotation")
def create_crop_rotation(plan: schemas.CropRotationCreate, db: Session = Depends(get_db)):
    try:
        # Fallback: Auto-generate plan if empty or keyword 'auto'
        if not plan.plan or plan.plan.strip().lower() == "auto":
            prompt = (
                f"Suggest a crop rotation plan for growing {plan.crop} "
                f"in {plan.soil} soil for {plan.duration} months. "
                "Provide a short and concise farmer-friendly explanation without using any symbols or bold text."
            )
            try:
                ai_response = model.generate_content(prompt)
                plan.plan = ai_response.text.strip()
            except GoogleAPIError as e:
                raise HTTPException(status_code=502, detail=f"Gemini error: {e.message}")
            except Exception as e:
                raise HTTPException(status_code=500, detail=f"Gemini unexpected error: {str(e)}")

        # Create DB entry
        plan_entry = models.CropRotation(**plan.dict())
        db.add(plan_entry)
        db.commit()
        db.refresh(plan_entry)

        return {
            "message": "Crop rotation plan saved successfully.",
            "data": {
                "id": plan_entry.id,
                "crop": plan_entry.crop,
                "soil": plan_entry.soil,
                "duration": plan_entry.duration,
                "plan": plan_entry.plan,
                "timestamp": plan_entry.timestamp,
            },
        }

    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database error: {str(e)}")

@app.get("/crop-rotation", response_model=List[schemas.CropRotationResponse])
def get_crop_rotation(db: Session = Depends(get_db)):
    plans = db.query(models.CropRotation).all()
    if not plans:
        raise HTTPException(status_code=404, detail="No crop rotation plans found")
    return plans
