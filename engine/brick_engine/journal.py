import asyncio
import asyncio.locks
from datetime import datetime
import json
from pathlib import Path

import re
import typing as ty

from pydantic import BaseModel
import aiofiles

class Cookie(BaseModel):
    journal: str
    value: str

class Line(BaseModel):
    timestamp: datetime
    event: str
    cookie: ty.Optional[Cookie]
    data: dict[str, ty.Any]

class Journal(BaseModel):
    name: str
    path: str
    timestamp: datetime
    index: int

    async def logs(self, cookie=None):
        async with aiofiles.open(self.path, encoding="utf-8") as file:
            if cookie is not None:
                await file.seek(cookie)
            async for row in file:
                row = json.loads(row)
                yield Line(timestamp=row.pop("timestamp"), event=row.pop("event"), cookie=None, data=row)

pattern = re.compile(r"Journal\.([^\.]+)\.(\d+).log")

def find_journal(base: Path):
    latest: ty.Optional[Journal] = None
    for pth in base.glob("Journal*.log"):
        if not pth.is_file():
            continue
        match = pattern.match(pth.name)
        if not match:
            continue
        ts = match.group(1)
        idx = match.group(2)
        dt = datetime.strptime(ts, "%Y-%m-%dT%H%M%S")
        j = Journal(name=pth.name, path=str(pth), timestamp=dt, index=idx)
        if latest is None:
            latest = j
        elif latest.timestamp < j.timestamp:
            latest = j
        elif latest.timestamp == j.timestamp and latest.index < j.index:
            latest = j
    return latest

async def iter_journals(base: Path):
    interval = 5.0 # seconds
    last = find_journal(base=base)
    yield last
    while True:
        found = find_journal(base=base)
        if found.name != last.name:
            last = found
            yield last
        await asyncio.sleep(interval)

async def iter_logs(base: Path, cookie=None):
    queue = asyncio.Queue(maxsize=1000)
    # task 1: yields new journals
    # when task 1 triggers new journal, switch to it and forget the current
    async def update_journals():
        producer_task = None
        async for journal in iter_journals(base=base):
            current = journal.logs()
            if producer_task is not None:
                producer_task.cancel()
            producer_task = asyncio.create_task(producer(current))
    async def producer(journal):
        async for line in journal:
            await queue.put(line)
    asyncio.create_task(update_journals())
    return queue
