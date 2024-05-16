package engine

import (
	"fmt"
	"log"
	"os"
	"strconv"
	"syscall"
	"time"

	"github.com/shirou/gopsutil/v3/process"
)

func RemovePidfile() {
	err := os.Remove("pid.txt")
	if err != nil {
		log.Println(err)
	}
}

func TermPidfile() error {
	data, err := os.ReadFile("pid.txt")
	if err == nil {
		pid, err := strconv.Atoi(string(data))
		if err != nil {
			log.Println(err)
		} else {
			os.Interrupt.Signal()
			proc, err := process.NewProcess(int32(pid))
			if err == nil {
				fmt.Println("terminating PID", pid)
				proc.SendSignal(syscall.SIGTERM)
			} else {
				for running, err := proc.IsRunning(); err == nil && running; {
					time.Sleep(200 * time.Millisecond)
				}	
			}
		}
	}
	return nil
}

func WritePidfile() error {
	err := os.WriteFile("pid.txt", []byte(fmt.Sprint(os.Getpid())), 0644)
	return err
}