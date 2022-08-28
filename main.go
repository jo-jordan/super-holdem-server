package main

import (
	"encoding/json"
	"flag"
	"log"
	"net/http"

	"github.com/gorilla/websocket"
	"github.com/mitchellh/mapstructure"
)

var addr = flag.String("addr", "localhost:8080", "http service address")

var upgrader = websocket.Upgrader{} // use default options

func game(w http.ResponseWriter, r *http.Request) {
	upgrader.CheckOrigin = func(r *http.Request) bool { return true }

	c, err := upgrader.Upgrade(w, r, nil)
	if err != nil {
		log.Print("upgrade:", err)
		return
	}
	defer c.Close()

	for {
		mt, message, err := c.ReadMessage()

		if err != nil {
			log.Println("read:", err)
			break
		}

		messageBody := MessageBody{}

		err = json.Unmarshal(message, &messageBody)
		if err != nil {
			log.Println("Unmarshal:", err)
			break
		}

		// {"api_type": 1, "data": {"call": 100}}

		switch messageBody.ApiType {
		case Join:
			result := JoinMessageBody{}
			err = mapstructure.Decode(messageBody.Data, &result)
			if err != nil {
				log.Println("mapstructure:", err)
				break
			}

			err = ApiJoin(c, mt, &result)
		case Sit:
			result := CallMessageBody{}
			err = mapstructure.Decode(messageBody.Data, &result)
			if err != nil {
				log.Println("mapstructure:", err)
				break
			}

			// err = ApiSit(c, &result)
		}

		if err != nil {
			log.Println("write:", err)
			break
		}
	}
}

func ApiJoin(c *websocket.Conn, mt int, msg *JoinMessageBody) error {
	log.Printf("recv: %s", msg)

	resp := MessageBody{
		ApiType: Join,
		Data:    nil,
	}

	result, err := json.Marshal(resp)

	err = c.WriteMessage(mt, result)

	return err
}

type ApiType int

const (
	Join  ApiType = 1
	Sit   ApiType = 2
	Bet   ApiType = 3
	Call  ApiType = 4
	Raise ApiType = 5
	Fold  ApiType = 6
	Check ApiType = 7
)

type MessageBody struct {
	ApiType ApiType                `json:"api_type"`
	Data    map[string]interface{} `json:"data"`
}

type JoinMessageBody struct {
	Num int `json:"num"`
}

type JoinResponseBody struct {
}

type CallMessageBody struct {
	Num int `json:"num"`
}

func main() {
	flag.Parse()
	log.SetFlags(0)

	http.HandleFunc("/game", game)
	log.Fatal(http.ListenAndServe(*addr, nil))
}
