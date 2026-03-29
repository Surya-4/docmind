# DocMind — AI-Powered Document Intelligence Platform

Chat with your PDF documents using AI. Upload any PDF and ask questions — DocMind finds the most relevant sections and answers using an LLM.

![DocMind Demo](demo.png)

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend Gateway | Spring Boot 3.x · Java 17 |
| AI Service | FastAPI · Python 3.11 |
| LLM | Groq (llama3-8b) |
| Embeddings | TF-IDF + FAISS |
| Frontend | React.js + Material UI |
| Database | PostgreSQL 15 |
| Cache | Redis 7 |
| File Storage | Supabase Storage |
| Containerisation | Docker + Docker Compose |

## Architecture
```
React (3000) → Spring Boot (8080) → FastAPI (8000)
                     ↓                    ↓
               PostgreSQL            FAISS Index
               Redis Cache           Groq LLM
               Supabase Storage
```

## Features

- JWT authentication with HttpOnly cookies
- PDF upload and storage on Supabase
- Automatic text extraction and chunking
- Vector similarity search with FAISS
- AI-powered answers using Groq LLaMA3
- Redis query caching (<10ms repeat answers)
- Full conversation history

## Quick Start

### Prerequisites
- Docker Desktop
- Java 17 + Maven
- Python 3.11
- Node.js 20

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/docmind.git
cd docmind
```

### 2. Set up environment variables
```bash
cp .env.example .env
# Fill in your API keys
```
```bash
cp backend/src/main/resources/application.properties.example \
   backend/src/main/resources/application.properties
# Fill in your values
```

### 3. Build Spring Boot jar
```bash
cd backend
mvn clean package -DskipTests
cd ..
```

### 4. Run with Docker Compose
```bash
docker-compose up --build
```

### 5. Set up database schema
Connect to PostgreSQL (localhost:5432) and run the SQL scripts in `database/schema.sql`

### 6. Open the app
Visit `http://localhost:3000`

## API Endpoints

### Auth
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login + set JWT cookie |
| POST | /api/auth/logout | Clear JWT cookie |

### Documents
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/documents/upload | Upload PDF |
| GET | /api/documents | List user's documents |
| GET | /api/documents/{id} | Get document status |

### Chat
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /conversations | Create chat session |
| POST | /conversations/{id}/query | Ask a question |
| GET | /conversations/{id}/messages | Get chat history |

## Environment Variables

### Root `.env`
```env
GROQ_API_KEY=your_groq_key
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
JWT_SECRET=your_jwt_secret
```

### AI Service `.env`
```env
GROQ_API_KEY=your_groq_key
SUPABASE_URL=your_supabase_url
SUPABASE_KEY=your_supabase_key
SPRING_BOOT_URL=http://localhost:8080
```

## Project Structure
```
docmind/
├── backend/          # Spring Boot — Auth, Documents, Chat
├── ai-service/       # FastAPI — PDF ingestion, RAG pipeline
├── frontend/         # React.js — Login, Dashboard, Chat UI
├── docker-compose.yml
└── README.md
```

## Free Deployment ($0/month)

| Service | Platform |
|---------|----------|
| React frontend | Vercel |
| Spring Boot | Render.com |
| FastAPI | Render.com |
| PostgreSQL | Neon.tech |
| Redis | Upstash |
| File Storage | Supabase |
| LLM | Groq free tier |
