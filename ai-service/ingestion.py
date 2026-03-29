import os
import fitz
import faiss
import numpy as np
import requests
import pickle
from langchain_text_splitters import RecursiveCharacterTextSplitter
from supabase import create_client
from dotenv import load_dotenv
from google import genai

load_dotenv()

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))
supabase = create_client(os.getenv("SUPABASE_URL"), os.getenv("SUPABASE_KEY"))

INDEXES_DIR = "./indexes"
os.makedirs(INDEXES_DIR, exist_ok=True)

def process_document(document_id: str, file_path: str):
    try:
        print(f"=== INGESTION STARTED for {document_id} ===")
        
        pdf_bytes = download_pdf(file_path)
        print(f"=== DOWNLOAD DONE — {len(pdf_bytes)} bytes ===")
        
        text = extract_text(pdf_bytes)
        print(f"=== TEXT EXTRACTED — {len(text)} chars ===")
        
        chunks = chunk_text(text)
        print(f"=== CHUNKED — {len(chunks)} chunks ===")
        
        embeddings = embed_chunks(chunks)
        print(f"=== EMBEDDED — {len(embeddings)} embeddings ===")
        
        save_faiss_index(document_id, embeddings, chunks)
        print(f"=== FAISS SAVED ===")
        
        notify_spring_boot(document_id, "READY")
        print(f"=== SPRING BOOT NOTIFIED ===")
        
    except Exception as e:
        print(f"=== INGESTION FAILED: {e} ===")
        notify_spring_boot(document_id, "FAILED")
    finally:
        print("=== PROCESS DOCUMENT DONE ===")

def download_pdf(file_path: str) -> bytes:
    filename = file_path.split("/")[-1]
    supabase_url = os.getenv("SUPABASE_URL")
    supabase_key = os.getenv("SUPABASE_KEY")
    
    url = f"{supabase_url}/storage/v1/object/public/docmind-pdfs/{filename}"
    
    print(f"Downloading from: {url}")
    
    response = requests.get(
        url,
        headers={"Authorization": f"Bearer {supabase_key}"}
    )
    
    print(f"Download status: {response.status_code}")
    print(f"Content length: {len(response.content)}")
    print(f"First 20 bytes: {response.content[:20]}")
    
    if response.status_code != 200:
        raise Exception(f"Failed to download: {response.text}")
    
    return response.content

def extract_text(pdf_bytes:bytes) -> str:
    doc = fitz.open(stream=pdf_bytes,filetype="pdf")
    text = ""
    for page in doc:
        text += page.get_text()
    doc.close()
    return text

def chunk_text(text: str) -> list[str]:
    splitter = RecursiveCharacterTextSplitter(
        chunk_size=700,
        chunk_overlap=100,
        length_function=len
    )
    return splitter.split_text(text)

def embed_chunks(chunks: list[str]):
    embeddings = []
    
    for i in range(0, len(chunks), 90):
        batch = chunks[i : i + 90]
        result = client.models.embed_content(
            model="models/gemini-embedding-2-preview",
            contents=batch,
            config={'task_type': 'RETRIEVAL_DOCUMENT'}
        )
        for emb in result.embeddings:
            embeddings.append(emb.values)
    return embeddings

def save_faiss_index(document_id: str, embeddings: list, chunks: list[str]):
    vectors = np.array(embeddings, dtype=np.float32)
    dimension = vectors.shape[1]

    index = faiss.IndexFlatL2(dimension)
    index.add(vectors)

    faiss.write_index(index, f"{INDEXES_DIR}/{document_id}.faiss")

    with open(f"{INDEXES_DIR}/{document_id}.pkl", "wb") as f:
        pickle.dump({"chunks": chunks}, f)

def notify_spring_boot(document_id: str, status: str):
    spring_url = os.getenv("SPRING_BOOT_URL")
    try:
        requests.patch(
            f"{spring_url}/documents/{document_id}/status",
            params={"status": status},
            timeout=10
        )
    except Exception as e:
        print(f"Failed to notify Spring Boot: {e}")