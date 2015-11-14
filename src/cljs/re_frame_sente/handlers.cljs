(ns re-frame-sente.handlers
  (:require [re-frame.core :as re-frame :refer [register-handler]]
            [re-frame-sente.socket :refer [chsk-send!]]
            [re-frame-sente.db :as db]))

(register-handler
 :initialize-db
 (fn  [_ _]
   db/default-db))

(register-handler
 :test/reply
 (fn [db msg]
   (assoc db :message msg)))

(register-handler
 :test/send
 (fn [db [_ msg]]
   (chsk-send! [:test/send msg])
   db))
