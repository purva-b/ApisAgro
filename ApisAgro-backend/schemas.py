from pydantic import BaseModel, Field
from typing import Optional
from datetime import datetime

# ========== CHAT SCHEMAS ==========

class ChatCreate(BaseModel):
    message: str = Field(..., example="How do I protect my crops from aphids?")
    is_user: bool = Field(..., example=True)

class ChatResponse(BaseModel):
    id: int
    message: str
    is_user: bool
    timestamp: datetime

    class Config:
        from_attributes = True 

# class ChatResponse(BaseModel):
#     id: int
#     message: str
#     is_user: bool
#     timestamp: datetime

#     class Config:
#         from_attributes = True

# ========== BEE TRAFFIC SCHEMAS ==========

class BeeTrafficCreate(BaseModel):
    level: str = Field(..., example="high")

class BeeTrafficResponse(BaseModel):
    id: int
    level: str
    timestamp: datetime

    class Config:
        from_attributes = True

# ========== CROP ROTATION SCHEMAS ==========

class CropRotationCreate(BaseModel):
    crop: str = Field(..., example="corn")
    soil: str = Field(..., example="loamy")
    duration: str = Field(..., example="3 months")
    plan: str = Field(..., example="Rotate with legumes after 3 months.")

class CropRotationResponse(BaseModel):
    id: int
    crop: str
    soil: str
    duration: str
    plan: str
    timestamp: datetime

    class Config:
        from_attributes = True
