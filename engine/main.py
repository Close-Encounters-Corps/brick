import logging

from brick_engine import server
import uvicorn

logging.basicConfig(filename="engine.log")

uvicorn.run(server.asgi, port=4500)
