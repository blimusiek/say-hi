(ns say-hi.core
  (:import goog.object)
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce state (r/atom {:employees [{:name "Maciek Blim" :position [23 45]}
                                    {:name "Piotrek Łukomiak" :position [56 76]}]}))

(defn marker [[x y]]
  [:div.marker {:style {:position "absolute"
                        :top (str y "px")
                        :left (str x "px")}}])

(defn get-bcr [el]
  (-> el
      r/dom-node
      .getBoundingClientRect))

(defn office-plan [employees]
  (let [!wrapper (r/atom nil)]
    (prn @!wrapper) ;; something is no yes
    [:div.office-plan-wrapper {:ref (fn [el]
                                      (if-not (.is goog.object el @!wrapper) (reset! !wrapper el)))}
     (if @!wrapper [(let [wrapper @!wrapper]
                      (map (fn [emp]
                             (let [pos (:position emp)
                                   [pos-x pos-y] pos
                                   bcr (get-bcr wrapper)
                                   w (:width bcr)
                                   h (:height bcr)
                                   x (/ (* w pos-x) 100)
                                   y (/ (* h pos-y) 100)]
                               [marker [x y]])) employees))])]))

(defn app []
  (let [employees (get @state :employees)]
    [:main
     [:section.content [office-plan employees]]
     [:aside.sidebar "List search projects itp .."]]))

(r/render-component [app]
                    (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
  )
