import base64
import random
import cv2
import numpy as np
from fastapi import FastAPI, HTTPException
from fastapi.responses import JSONResponse
import uvicorn
from ultralytics import YOLO
from pydantic import BaseModel
from typing import List
from collections import Counter

app = FastAPI()
model = YOLO('best.pt')

# 1. Definiujemy model danych wejściowych JSON
class MatchRequest(BaseModel):
    images: List[str]  # Lista stringów w formacie Base64

def extract_gesture(results) -> str:
    if not results or len(results) == 0: return "none"
    result = results[0]
    if result.boxes is None or len(result.boxes) == 0: return "none"

    best_match_index = result.boxes.conf.cpu().numpy().argmax()
    best_class_id = int(result.boxes.cls.cpu().numpy()[best_match_index])
    return result.names[best_class_id].lower()

def determine_winner(player_gesture: str, robot_gesture: str) -> str:
    if player_gesture == robot_gesture: return "draw"
    wins = {"rock": "scissors", "paper": "rock", "scissors": "paper"}
    return "player_wins" if wins.get(player_gesture) == robot_gesture else "robot_wins"

# 2. Zmieniony endpoint przyjmujący czysty JSON
@app.post("/play")
async def play(data: MatchRequest):
    if not data.images:
        return JSONResponse({"error": "Nie przesłano żadnych klatek."}, status_code=400)

    detected_gestures = []
    valid_gestures = ["rock", "paper", "scissors"]

    # 3. Dekodowanie Base64 z powrotem do obrazu OpenCV
    for base64_string in data.images:
        try:
            # Odrzucamy nagłówek meta (np. "data:image/jpeg;base64,"), jeśli robot go doda
            if "," in base64_string:
                base64_string = base64_string.split(",")[1]

            img_bytes = base64.b64decode(base64_string)
            nparr = np.frombuffer(img_bytes, np.uint8)
            img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

            if img is not None:
                results = model(img, conf=0.6, verbose=False)
                gesture = extract_gesture(results)
                if gesture in valid_gestures:
                    detected_gestures.append(gesture)
        except Exception:
            # Ignorujemy uszkodzone klatki tekstu
            continue

    if not detected_gestures:
        return JSONResponse({
            "error": "Nie wykryto żadnego poprawnego gestu.",
            "player_gesture": "none",
            "robot_gesture": "none",
            "result": "no_detection",
            "debug_votes": {}
        })

    vote_counts = Counter(detected_gestures)
    final_gesture = vote_counts.most_common(1)[0][0]
    robot_gesture = random.choice(valid_gestures)
    result = determine_winner(final_gesture, robot_gesture)

    return JSONResponse({
        "player_gesture": final_gesture,
        "robot_gesture": robot_gesture,
        "result": result,
        "debug_votes": dict(vote_counts)
    })

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)