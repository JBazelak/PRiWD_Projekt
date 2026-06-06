import random
import cv2
import numpy as np
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import JSONResponse
import uvicorn
from ultralytics import YOLO

from typing import List
from collections import Counter

app = FastAPI()


print("Ładowanie modelu YOLO...")
model = YOLO('best.pt')
print("Model załadowany pomyślnie!")

def extract_gesture(results) -> str:
    result = results[0]

    if len(result.boxes) == 0:
        return "none"

    confidences = result.boxes.conf.cpu().numpy()
    classes = result.boxes.cls.cpu().numpy()

    best_match_index = confidences.argmax()
    best_class_id = int(classes[best_match_index])
    
    # Pobranie nazwy gestu
    gesture_name = result.names[best_class_id].lower()
    

    return gesture_name

def determine_winner(player_gesture: str, robot_gesture: str) -> str:
    if player_gesture == robot_gesture:
        return "draw"
    
    wins = {
        "rock": "scissors",
        "paper": "rock",
        "scissors": "paper"
    }
    
    if wins.get(player_gesture) == robot_gesture:
        return "player_wins"
    else:
        return "robot_wins"

@app.post("/play")
async def play(files: List[UploadFile] = File(...)):

    #sprawdzenie czy robot wysłał jakieś klatki
    if not files or len(files) == 0:
        return JSONResponse({"error": "Nie przesłano żadnych klatek."}, status_code=400)

    detected_gesture = []

    #przechodzenie po każdej klatce
    for file in files:
        contents = await file.read()
        nparr = np.frombuffer(contents, np.uint8)
        img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

        if img is not None:
            #dopalanie modelu dla klatki
            results = model(img, conf=0.6)
            gesture = extract_gesture(results)

            #zliczamy klatki na których model coś znalazł
            if gesture != "none":
                detected_gesture.append(gesture)
        
    valid_gestures = ["rock", "paper", "scissors"]

    # odrzucanie błędnych klas
    detected_gesture = [g for g in detected_gesture if g in valid_gestures]

    # GŁOSOWANKO - wybieramy gest, który pojawił się najczęściej
    vote_counts = Counter(detected_gesture)
    final_gesture = vote_counts.most_common(1)[0][0]

    #losownie i werdykt
    robot_gesture = random.choice(valid_gestures)
    result = determine_winner(final_gesture, robot_gesture)
    
    #Odsyłamy wynik do robota
    return JSONResponse(
        {
        "player_gesture": final_gesture,
        "robot_gesture": robot_gesture,
        "result": result,
        "debug_votes": dict(vote_counts)  # Dodajemy debugowe informacje o głosowaniu
    })

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
