(ns say-hi.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce state (r/atom {:text "Hello world!"}))

(defn hello-world []
  [:div
   [:h1 (:text @state)]
   [:h3 "Edit this and watch it change!"]])

(r/render-component [hello-world]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
