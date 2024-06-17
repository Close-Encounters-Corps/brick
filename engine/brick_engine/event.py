from datetime import datetime
import typing as ty

from pydantic import BaseModel, Field

if ty.TYPE_CHECKING:
    from brick_engine import journal


E = ty.TypeVar("E")

class SimpleEvent(BaseModel):
    timestamp: datetime
    log: 'journal.Line'
    event: str

    @classmethod
    def parse(self):
        pass

class Music(BaseModel):
    event = "Music"
    track: str = ""
    def parse(self):
        self.track = self.raw["MusicTrack"]

class BuyAmmo(BaseModel):
    event = "BuyAmmo"
    cost: int
    def parse(self):
        self.track = self.raw["Cost"]

class Repair(BaseModel):
    event = "Repair"
    items: list[str]
    cost: int
    def parse(self):
        self.items = self.raw["Items"]
        self.cost = self.raw["Cost"]

class Shutdown(BaseModel):
    event = "Shutdown"
