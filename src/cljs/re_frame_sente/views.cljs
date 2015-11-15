(ns re-frame-sente.views
  (:require [re-frame.core :as re-frame :refer [dispatch]]
            [reagent.core :as reagent]
            [re-frame-sente.socket :as socket]))

(defn input
  []
  [:input {:type "text"
           :placeholder "Write here."
           :on-change #(dispatch [:test/send (-> % .-target .-value)])}])

(defn message
  []
  (let [message (re-frame/subscribe [:message])]
    [:div
     [:span "> "]
     [:span @message]]))

(defn main-panel []
  (let [name (re-frame/subscribe [:name])]
    (reagent/create-class
     {:component-will-mount socket/event-loop
      :reagent-render (fn []
                        [:div
                         [:div "Hello from " @name]
                         [input]
                         [message]])})))
