from fastapi import FastAPI, BackgroundTasks
from models import IngestRequest, QueryRequest, QueryResponse
import ingestion
import query

app = FastAPI(title="DocMind AI Service")

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/ingest", status_code=202)
async def ingest_document(request: IngestRequest, background_tasks: BackgroundTasks):
    background_tasks.add_task(ingestion.process_document, request.documentId, request.filePath)
    return {"message": "Ingestion started"}

@app.post("/infer/query")
async def query_document(request: QueryRequest) -> QueryResponse:
    return query.answer_question(request.documentId, request.question)