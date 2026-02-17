from fastapi import FastAPI
from handlers.analyze import router as health_router
from handlers.analyze import router as analyze_router

app = FastAPI(title="French Analyzer NLP Worker")

app.include_router(health_router)
app.include_router(analyze_router)