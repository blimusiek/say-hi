(ns say-hi.core
  (:require [reagent.core :as r]))

(enable-console-print!)

;; TODO get rid of this atom
(defonce state (r/atom {:employees [{:name "Maciek Blim" :position [23 45] :project "Vizi"}
                                    {:name "Piotrek Łukomiak" :position [56 76] :project "Vizi"}]}))


(defn marker-details [{name :name
                       project :project}]
  [:div.marker-details [:div name] [:div project]])

(defn marker [{x :x y :y info :info}]
  (let [!is-mouse-over (r/atom false)]
    (fn []
      (let [size 10
            half-size (/ size 2)
            top (str (- y half-size) "px")
            left (str (- x half-size) "px")
            size-px (str size "px")]
        [:div.marker {:style {:position "absolute"
                              :top top
                              :left left
                              :height size-px
                              :width size-px}
                      :on-mouse-over (fn [] (reset! !is-mouse-over true))
                      :on-mouse-out (fn [] (reset! !is-mouse-over false))}
         (if @!is-mouse-over [marker-details info])]))))

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
                 y (/ (* h pos-y) 100)
                 name (:name emp)
                 project (:project emp)]
             ^{:key emp} [marker {:x x :y y :info {:name name
                                                   :project project}}])))])))

(defn employee-list [employees]
  [:div.employees-list (for [emp employees]
                         (let [name (:name emp)
                               project (:project emp)]
                           ^{:key emp} [:div.employees-list-item [:div.employees-list-item-label name]
                                        [:div.employees-list-item-legend project]]))])

(defn employee-search [on-search]
  [:div.employee-search [:input {:type "text"
                                 :on-change (fn [e] (on-search (-> e .-target .-value)))}]])

(defn search [query item])

(defn app []
  (let [employees (get @state :employees)
        !query (r/atom "")]
    (fn []
      (let [emps (map (fn [emp]
                        (let [name (:name emp)
                              project (:project emp)
                              pattern (re-pattern (str "(?i)" @!query))]
                          (assoc emp :found (or (re-find pattern name) (re-find pattern project))))) employees)]
        (prn emps)
        [:main
         [:section.content [office-plan emps]]
         [:aside.sidebar [employee-search (fn [query] (reset! !query query))] [employee-list emps]]]))))

(r/render-component [app]
                    (. js/document (getElementById "app")))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

