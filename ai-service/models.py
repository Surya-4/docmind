from pydantic import BaseModel
from typing import List, Optional

class IngestRequest(BaseModel):
    documentId: str
    filePath: str

class QueryRequest(BaseModel):
    documentId: str
    question: str

class QueryResponse(BaseModel):
    answer: str
    sourceChunks: List[str]

class StatusUpdate(BaseModel):
    status: str