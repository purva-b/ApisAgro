from . import models, schemas
from sqlalchemy.orm import Session

def save_chat(db: Session, chat: schemas.ChatCreate):
    db_chat = models.Chat(**chat.dict())
    db.add(db_chat)
    db.commit()

def get_chats(db: Session):
    return db.query(models.Chat).all()
