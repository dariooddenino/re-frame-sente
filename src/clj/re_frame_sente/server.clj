(ns re-frame-sente.server
  (:require [clojure.core.async :as async
             :refer [<! <!! chan go thread]]
            [clojure.core.cache :as cache]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (sente-web-server-adapter)]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes ANY GET POST routes]]
            [compojure.handler :as h]
            [compojure.route :as r]
            [org.httpkit.server :as kit]
            [clj-uuid :as uuid]))

;; SENTE COMMUNICATION

(defonce sente-socket
  (sente/make-channel-socket! sente-web-server-adapter {}))

(let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
              connected-uids]} sente-socket]
  (def ring-ajax-post                ajax-post-fn)
  (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
  (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
  (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
  (def connected-uids                connected-uids) ; Watchable, read-only atom
  )

;; ROUTES

(defn index
  [req]
  {:status 200
   :body "<html>Hello world!</html>"})

(def app-routes
  (routes
   (GET "/" req (#'index req))
   (GET "/chsk" req (#'ring-ajax-get-or-ws-handshake req))
   (POST "/chsk" req (#'ring-ajax-post req))))


;; MESSAGE HANDLER

(defn get-session-uid
  "Convenient to extract from the request the UID that Sente needs."
  [req]
  (get-in req [:session :uid]))

(defn unique-id
  "Return a unique ID (for an unsecured session ID)."
  []
  (uuid/v1))

(defmulti handle-event
  "Multimethod used to handle events coming from the client."
  (fn [[ev-id ev-arg] ring-req] ev-id))

(defmethod handle-event :test/send
  [[_ msg] req]
  (when-let [uid (get-session-uid req)]
    (chsk-send! uid [:test/reply (clojure.string/reverse msg)])))

(defmethod handle-event :chsk/ws-ping
  [_ req]
  nil)

(defmethod handle-event :default
  [event req]
  nil)

(defn event-loop
  "Handle inbound events."
  []
  (go (loop [{:keys [client-uuid ring-req event] :as data} (<! ch-chsk)]
        (println "-" event)
        (thread (handle-event event ring-req))
        (recur (<! ch-chsk)))))

;; SERVER AND MIDDLEWARES

(defn wrap-session
  [app]
  (fn [req]
    (if (get-session-uid req)
      (do  (println "uid ok")
           (app req))
      (app (assoc-in req [:session :uid] (unique-id))))))

(defroutes server
  (-> app-routes
      (wrap-cors :access-control-allow-origin [#"http://localhost:3449"]
                 :access-control-allow-methods [:post :get])
      wrap-session
      h/site))

(def server-reload (wrap-reload #'server))

(defn -main
  "Runs the server. If PORT isn't set a environment variable,
  3001 is used as default."
  [& args]
  (event-loop)
  (let [port (or (System/getenv "PORT") 3001)]
    (println "Starting server on port" port "...")
    (kit/run-server #'server-reload {:port port})))
