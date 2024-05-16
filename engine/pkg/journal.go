package engine

import (
	"bufio"
	"context"
	"fmt"
	"io"
	"log"
	"os"
	"path/filepath"
	"regexp"
	"strconv"
	"time"
)

type Journal struct {
	Name      string
	Path      string
	Timestamp time.Time
	Index     int
}

const JournalFormat = "2006-01-02T150405"

func FindJournal() (*Journal, error) {
	pth, err := os.UserHomeDir()
	if err != nil {
		return nil, err
	}
	pth = filepath.Join(
		pth,
		"Saved Games",
		"Frontier Developments",
		"Elite Dangerous",
		"Journal*.log",
	)
	journalSlice, err := filepath.Glob(pth)
	if err != nil {
		return nil, err
	}
	var lastJournal *Journal
	for _, v := range journalSlice {
		base := filepath.Base(v)
		matcher, err := regexp.Compile(`Journal\.([^\.]+)\.(\d+).log`)
		if err != nil {
			return nil, err
		}
		matches := matcher.FindStringSubmatch(base)
		if len(matches) < 2 {
			return nil, fmt.Errorf("invalid file name")
		}
		res, err := time.Parse(JournalFormat, matches[1])
		if err != nil {
			return nil, err
		}
		idx, err := strconv.ParseInt(matches[2], 10, 32)
		if err != nil {
			return nil, err
		}
		journal := &Journal{
			Name:      base,
			Path:      v,
			Timestamp: res,
			Index:     int(idx),
		}
		if lastJournal == nil {
			lastJournal = journal
			continue
		}
		switch lastJournal.Timestamp.Compare(journal.Timestamp) {
		case -1:
			lastJournal = journal
		case 0:
			if journal.Index > lastJournal.Index {
				lastJournal = journal
			}
		}
	}
	return lastJournal, nil
}

func (j *Journal) ReadFile(ctx context.Context) (chan string, error) {
	out := make(chan string)
	fd, err := os.Open(j.Path)
	if err != nil {
		return nil, err
	}
	file := bufio.NewReader(fd)
	go func() {
		defer fd.Close()
		for {
			select {
			case <-ctx.Done():
				return
			default:
				str, err := file.ReadString('\n')
				if err != nil {
					if err != io.EOF {
						log.Println(err)
					}
					return
				}
				select {
				case <-ctx.Done():
					return
				case out <- str:
				}
			}
		}
	}()
	return out, nil
}

func JournalChan(ctx context.Context) (chan *Journal, error) {
	interval := 5 * time.Second
	out := make(chan *Journal, 5)
	last, err := FindJournal()
	if err != nil {
		return nil, err
	}
	go func(ctx context.Context, last *Journal) {
		out <- last
		for {
			select {
			case <-ctx.Done():
				return
			case <-time.After(interval):
				found, err := FindJournal()
				if err != nil {
					log.Println(err)
					continue
				}
				if found.Path != last.Path {
					out <- found
					last = found
				}
			}
		}
	}(ctx, last)
	return out, nil
}

func Logs(ctx context.Context) (chan string, error) {
	flow, err := JournalChan(ctx)
	if err != nil {
		return nil, err
	}
	out := make(chan string, 5)
	go func(ctx context.Context, stream chan<- string) {
		for journal := range flow {
			lines, err := journal.ReadFile(ctx)
			if err != nil {
				log.Println(err)
				continue
			}
			for x := range lines {
				stream <- x
			}
		}
	}(ctx, out)
	return out, nil
}