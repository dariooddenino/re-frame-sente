(ns re-frame-sente.socket
  (:require [re-frame.core :refer [dispatch]]
            [cljs.core.async :as async :refer [<! >! put! chan]]
            [taoensso.sente :as sente])
  (:require-macros [cljs.core.async.macros :refer (go go-loop)]))

;; SENTE SOCKET

;; It will survive to Figwheel reload.
(defonce sente-socket
  (sente/make-channel-socket! "/chsk"
                              {:type :auto
                               :chsk-url-fn #(str "ws://localhost:3001" %) ;; Use the server url
                               }))

(let [{:keys [chsk ch-recv send-fn state]}
      sente-socket]
  (def chsk       chsk)
  (def ch-chsk    ch-recv) ; ChannelSocket's receive channel
  (def chsk-send! send-fn) ; ChannelSocket's send API fn
  (def chsk-state state)   ; Watchable, read-only atom
  )

(defmulti handle-event
  "Handle events based on the event Id."
  (fn [[ev-id ev-arg]] ev-id))

;; Print answer
(defmethod handle-event :test/reply
  [[_ msg]]
  (dispatch [:test/reply msg]))

;; Ignoring unknown events.
(defmethod handle-event :default
  [event]
  (println "UNKNOW EVENT" event))

(defn test-session
  "Ping the server."
  []
  (chsk-send! [:session/status]))

(defn event-loop
  "Handle inbound events."
  []
  (go (loop [[op arg] (:event (<! ch-chsk))]
        (println "-" op)
        (case op
          :chsk/recv (handle-event arg)
          (test-session))
        (recur (:event (<! ch-chsk))))))
