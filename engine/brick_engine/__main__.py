import uvicorn

from brick_engine import server


uvicorn.run(server.asgi)
