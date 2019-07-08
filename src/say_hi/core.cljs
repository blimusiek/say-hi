(ns say-hi.core
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
      .getBoundingClientRect))

(defn set-ref! [!ref]
  (fn [el]
    (if (and el (not (identical? el @!ref))) (reset! !ref el))))

(defn office-plan [employees]
  (let [!wrapper (r/atom nil)]
    (fn []
      [:div.office-plan-wrapper {:ref (set-ref! !wrapper)}
       (if-let [wrapper @!wrapper]
         (for [emp employees]
           (let [pos (:position emp)
                 [pos-x pos-y] pos
                 bcr (get-bcr wrapper)
                 w (.-width bcr)
                 h (.-height bcr)
                 x (/ (* w pos-x) 100)
                 y (/ (* h pos-y) 100)]
             ^{:key emp} [marker [x y]])))])))

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
