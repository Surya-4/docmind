import os
from google import genai
from tenacity import retry, wait_exponential, stop_after_attempt

client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

@retry(wait=wait_exponential(multiplier=1, min=2, max=10), stop=stop_after_attempt(3))
def call_gemini_llm(prompt: str) -> str:
    try:
        response = client.models.generate_content(
        model="gemini-3-flash-preview", 
        contents=prompt
    )
        return response.text
    except Exception as e:
        print(f"Gemini API Error: {e}")
        return "The AI is currently busy. Please try again in a few seconds."