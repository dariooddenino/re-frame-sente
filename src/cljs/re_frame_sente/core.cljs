(ns re-frame-sente.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as re-frame]
              [re-frame-sente.handlers]
              [re-frame-sente.subs]
              [re-frame-sente.views :as views]))

(enable-console-print!)

(defn mount-root []
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (mount-root))
