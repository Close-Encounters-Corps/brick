package main

import (
	engine "brick-engine/pkg"
	"context"
	"flag"
	"fmt"
	"log"
	"net/http"
	"os"
	"time"

	"github.com/zishang520/engine.io/v2/types"
	socktypes "github.com/zishang520/engine.io/v2/types"
	socketio "github.com/zishang520/socket.io/v2/socket"
)

var (
	listen      = flag.String("listen", "localhost:4500", "server host:port")
	logpath     = flag.String("logpath", "engine.log", "log path")
	stopCmd     = flag.Bool("stop", false, "stop service instead of run")
	corsDisable = flag.Bool("cors-disable", true, "disable CORS")
)

func AllowedOrigins(r *http.Request) bool {
	if *corsDisable {
		return true
	}
	return r.Header.Get("Access-Control-Allow-Origin") == *listen
}

func main() {
	logfile, err := os.OpenFile(*logpath, os.O_CREATE|os.O_APPEND, 0644)
	if err != nil {
		log.Fatalln(err)
	}
	defer logfile.Close()
	log.SetOutput(logfile)
	flag.Parse()
	if (*stopCmd) {
		http.Post(fmt.Sprintf("http://%s/shutdown", *listen), "text/html", nil)
		os.Exit(0)
	}
	err = engine.WritePidfile()
	if err != nil {
		log.Fatalln(err)
	}
	ctx, stop := context.WithCancel(context.Background())
	defer stop()
	opts := socketio.DefaultServerOptions()
	opts.SetCors(&types.Cors{
		Origin: "*",
		Credentials: true,
	})
	mux := socktypes.NewServeMux(nil)
	server := socktypes.NewWebServer(mux)
	io := socketio.NewServer(server, opts)
	mux.HandleFunc("/shutdown", func(w http.ResponseWriter, r *http.Request) {
		if r.Method == http.MethodPost {
			stop()
		}
		w.WriteHeader(http.StatusAccepted)
	})
	io.On("connection", func(clients ...any) {
		client := clients[0].(*socketio.Socket)
		client.On("stream", func(a ...any) {
			logs, err := engine.Logs(ctx)
			if err != nil {
				client.Emit("err", err.Error())
			}
			for v := range logs {
				client.Emit("event", v)
			}
		})
	})
	httpsrv := server.Listen(*listen, nil)
	exitCtx, exit := context.WithCancel(context.Background())
	go func() {
		defer exit()
		defer engine.RemovePidfile()
		<-ctx.Done()
		log.Println("exiting gracefully")
		shutdownCtx, cancel := context.WithTimeout(exitCtx, 5*time.Second)
		go func() {
			defer cancel()
			<-shutdownCtx.Done()
			if shutdownCtx.Err() == context.DeadlineExceeded {
				log.Fatalln("graceful shutdown timed out - forcing exit")
			}
		}()
		err := httpsrv.Close()
		if err != nil {
			log.Println(err)
		}
	}()
	log.Println("server listening at", *listen)
	<-exitCtx.Done()
}
