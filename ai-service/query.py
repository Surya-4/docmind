import os
import faiss
import numpy as np
import pickle
from models import QueryResponse
from dotenv import load_dotenv
from google import genai

from gemini_client import call_gemini_llm

load_dotenv()

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

INDEXES_DIR = "./indexes"

_index_cache = {}
_chunks_cache = {}

def answer_question(document_id: str, question: str):
    index, chunks = load_index(document_id)
    
    question_embedding = embed_question(question)
    
    distances, indices = index.search(question_embedding, k=5)
    top_chunks = [chunks[i] for i in indices[0] if i < len(chunks)]
    
    context_text = "\n\n".join(top_chunks)
    prompt = f"""Use the following context to answer the question. 
    If you don't know, say you don't know.
    
    CONTEXT: {context_text}
    QUESTION: {question}"""

    answer = call_gemini_llm(prompt)
    return QueryResponse(answer=answer, sourceChunks=top_chunks)

def load_index(document_id:str):
    if document_id not in _index_cache:
        index_path = f"{INDEXES_DIR}/{document_id}.faiss"
        chunks_path = f"{INDEXES_DIR}/{document_id}.pkl"

        if not os.path.exists(index_path) or not os.path.exists(chunks_path):
            raise FileNotFoundError(f"Index or metadata missing for document: {document_id}")
        
        _index_cache[document_id] = faiss.read_index(index_path)

        with open(chunks_path,"rb") as f:
            data = pickle.load(f)
            _chunks_cache[document_id] = data["chunks"]

    return _index_cache[document_id], _chunks_cache[document_id]

def embed_question(question: str) -> np.ndarray:
    result = client.models.embed_content(
        model="models/gemini-embedding-2-preview",
        contents=question,
        config={'task_type': 'RETRIEVAL_QUERY'}
    )
    vector = result.embeddings[0].values
    return np.array([vector], dtype=np.float32)


def similarity_search(index, chunks: list, question_vector: np.ndarray, k: int = 4) -> list[str]:
    distances, indices = index.search(question_vector, k)
    return [chunks[i] for i in indices[0] if i < len(chunks)]