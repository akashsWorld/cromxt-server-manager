from fastapi import FastAPI
from typing import Union
import subprocess

app = FastAPI()

@app.get("/")
async def root():
    return {"message": "Hello Akash"}


@app.post("/api/v1/buckets/{bucketId}")
async def create_server(bucketId: str):
    if bucketId == None:
        return {"message": "Bucket ID is required"}
    
    print(f"Bucket ID: {bucketId}")
    stdout, stderr = subprocess.run(["tao","--version"], capture_output=True,text=True)
    
    if stderr:
        print(stderr)    
    return {"message": "Bucket created"}
