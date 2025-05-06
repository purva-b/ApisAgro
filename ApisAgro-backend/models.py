from sqlalchemy import Column, Integer, String, Boolean, DateTime, Text
from datetime import datetime
from database import Base

class Chat(Base):
    __tablename__ = "chats"

    id = Column(Integer, primary_key=True, index=True)
    message = Column(Text, nullable=False)
    is_user = Column(Boolean, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False)

class BeeTraffic(Base):
    __tablename__ = "bee_traffic"

    id = Column(Integer, primary_key=True, index=True)
    level = Column(String(50), nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False)

class CropRotation(Base):
    __tablename__ = "crop_rotations"

    id = Column(Integer, primary_key=True, index=True)
    crop = Column(String(100), nullable=False)
    soil = Column(String(100), nullable=False)
    duration = Column(String(100), nullable=False)
    plan = Column(Text, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow, nullable=False)
