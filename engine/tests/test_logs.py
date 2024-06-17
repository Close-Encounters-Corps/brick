import asyncio
from pathlib import Path

import pytest

from brick_engine import journal



@pytest.mark.asyncio
async def test_latest_journal(tmp_path: Path):
    journals = ["Journal.2024-01-04T174704.01.log", "Journal.2024-01-05T174704.01.log", "Journal.2024-01-05T174704.02.log"]
    for x in journals:
        tmp_path.joinpath(x).write_text("{}")
    j = journal.find_journal(tmp_path)
    assert j.name == "Journal.2024-01-05T174704.02.log"

@pytest.mark.asyncio
async def test_empty_path(tmp_path):
    j = journal.find_journal(tmp_path)
    assert not j

@pytest.mark.asyncio
async def test_logs(tmp_path):
    name = "Journal.2024-01-05T174704.02.log"
    with tmp_path.joinpath(name).open("w") as file:
        file.write("""{ "timestamp":"2023-06-07T17:31:53Z", "event":"Fileheader", "part":1, "language":"English/UK", "Odyssey":true, "gameversion":"4.0.0.1502", "build":"r294054/r0 " }
{ "timestamp":"2023-06-07T17:32:18Z", "event":"Friends", "Status":"Online", "Name":"Kelyf" }
{ "timestamp":"2023-06-07T17:32:21Z", "event":"Commander", "FID":"F542799", "Name":"Kamish" }
{ "timestamp":"2023-06-07T18:41:38Z", "event":"Shutdown" }
""")
    j = journal.find_journal(tmp_path)
    count = 0
    async for x in j.logs():
        count += 1
    assert count == 4

@pytest.mark.asyncio
async def test_logs_stream(tmp_path):
    name_first = "Journal.2023-06-07T193158.01.log"
    lines_first = [
        '{ "timestamp":"2023-06-07T17:31:53Z", "event":"Fileheader", "part":1, "language":"English/UK", "Odyssey":true, "gameversion":"4.0.0.1502", "build":"r294054/r0 " }',
        '{ "timestamp":"2023-06-07T17:32:21Z", "event":"Commander", "FID":"F542799", "Name":"Kamish" }',
        '{ "timestamp":"2023-06-07T18:41:38Z", "event":"Shutdown" }'
    ]
    lines_second = [
        '{ "timestamp":"2024-01-04T14:46:59Z", "event":"Fileheader", "part":1, "language":"English/UK", "Odyssey":true, "gameversion":"4.0.0.1701", "build":"r298704/r0 " }',
        '{ "timestamp":"2024-01-04T14:48:33Z", "event":"Commander", "FID":"F542799", "Name":"Kamish" }',
        '{ "timestamp":"2024-01-04T15:07:53Z", "event":"Shutdown" }'
    ]
    name_second = "Journal.2024-01-05T174704.02.log"
    async def writer(pth: Path, lines, delay=0):
        if delay > 0:
            await asyncio.sleep(delay)
        with pth.open("w") as file:
            for line in lines:
                file.write(line)
                file.write("\n")
                if delay > 0:
                    await asyncio.sleep(delay)
        print("finished writing", pth.name)
    await writer(pth=tmp_path / name_first, lines=lines_first)
    writer_task = asyncio.create_task(writer(pth=tmp_path / name_second, lines=lines_second, delay=0.5), name="writer delayed")
    count = 0
    async def counter():
        nonlocal count
        logs = await journal.iter_logs(tmp_path)
        while count != 6:
            item = await logs.get()
            print(item)
            count += 1
    counter_task = asyncio.create_task(counter())
    await asyncio.gather(writer_task, counter_task)
    assert count == 6
