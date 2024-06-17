from pathlib import Path

from fastapi import FastAPI
import socketio

from brick_engine import journal

base = Path.home().joinpath(
    "Saved Games",
    "Frontier Developments",
    "Elite Dangerous"
)

app = FastAPI()
sio = socketio.AsyncServer(cors_allowed_origins=[], async_mode='asgi')

@app.get("/")
def get_root():
    return "Ok."

asgi = socketio.ASGIApp(sio, app)

@sio.event
async def stream(sid):
    queue = await journal.iter_logs(base)
    while True:
        event: journal.Line = await queue.get()
        await sio.emit("event", event.model_dump_json(), to=sid)
