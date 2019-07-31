(ns say-hi.core
  (:require [reagent.core :as r]
            [clojure.string :refer [blank? split]]
            [cljs.core.async :refer [chan sliding-buffer timeout tap mult offer!]]
            [cljs.core.async :refer-macros [go go-loop alt!]]))

(enable-console-print!)

(def employees [{:name "Maciek Blim" :position [23 45] :project "Vizi"}
                {:name "Piotrek Łukomiak" :position [56 76] :project "Vizi"}])

(def timeout-ms 400)

;; It seems to be working
;; Is it good? Is it ugly?
;; Is it a proper way to solve that kind of problems in cljs/clj?
(defn debounce
  ([f]
   (debounce f timeout-ms))
  ([f ms]
   (let [lock (chan (sliding-buffer 1))
         stop (chan (sliding-buffer 1))]
     (go-loop [prev-lock nil]
       (when-let [new-lock (<! lock)]
         (if prev-lock (offer! prev-lock :stop))
         (recur new-lock)))
     (fn [val]
       (let [stop-chan (chan 1)]
         (go
           (offer! lock stop-chan)
           (alt!
             (timeout ms) ([] (f val))
             (tap (mult stop) stop-chan) ([]))))))))


(defn marker-details [{name :name
                       project :project}]
  [:div.marker-details [:div name] [:div project]])

(defn search-class-names [emp]
  (let [is-in-search (contains? emp :found)]
    (if is-in-search
      (let [is-found (not (nil? (get emp :found)))]
        (if is-found "searched-and-found" "searched-and-not-found")))))

;; TODO animation of size on find
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
                      :class (search-class-names info)
                      :on-mouse-over (fn [] (reset! !is-mouse-over true))
                      :on-mouse-out (fn [] (reset! !is-mouse-over false))}
         (if @!is-mouse-over [marker-details info])]))))

(defn get-bcr [el]
  (-> el
      .getBoundingClientRect))

(defn set-ref! [!ref]
  (fn [el]
    (if (and el (not (identical? el @!ref))) (reset! !ref el))))

(defn office-plan []
  (let [!wrapper (r/atom nil)]
    (fn [employees]
      [:div.office-plan-wrapper {:ref (set-ref! !wrapper)}
       (if-let [wrapper @!wrapper]
         (for [emp employees]
           (let [{:keys [name project] [pos-x pos-y] :position} emp
                 bcr (get-bcr wrapper)
                 w (.-width bcr)
                 h (.-height bcr)
                 x (/ (* w pos-x) 100)
                 y (/ (* h pos-y) 100)]
             ^{:key emp} [marker {:x x :y y :info emp}])))])))

(defn highlight [match text]
  (if (nil? match)
    text
    (let [parts (split (str text " ") (re-pattern (str "(?i)" match)))
          highlights (filter (fn [s] (not (blank? s)))
                             (interpose [:strong match] parts))]
      [:span.highlight-match highlights])))

;; TODO sort over found
;; TODO click highlight coresponding marker
(defn employee-list [employees]
  [:div.employees-list (for [emp employees]
                         (let [{:keys [name project found]} emp
                               highlighted-name (highlight found name)
                               highlighted-project (highlight found project)]
                           ^{:key emp} [:div.employees-list-item {:class (search-class-names emp)}
                                        [:div.employees-list-item-label highlighted-name]
                                        [:div.employees-list-item-legend highlighted-project]]))])

(defn employee-search [on-search]
  (let [on-change (debounce (fn [q] (on-search q)))]
    (fn []
      [:div.employee-search [:input {:type "text"
                                     :on-change (fn [e] (on-change (-> e .-target .-value)))}]])))

(defn match-query [query {:keys [name project]}]
  (let [pattern (re-pattern (str "(?i)" query))]
    (or (re-find pattern name) (re-find pattern project))))

(defn mark-found [query emp]
  (assoc emp :found (match-query query emp)))

(defn search-query [query employees]
  (map (fn [emp]
         (mark-found query emp)) employees))

(defn app [employees]
  (let [!query (r/atom "")]
    (fn []
      (let [query @!query
            emps (if (blank? query) employees (search-query query employees))
            on-search! (fn [query] (reset! !query query))]
        [:main
         [:section.content [office-plan emps]]
         [:aside.sidebar [employee-search on-search!] [employee-list emps]]]))))

(r/render-component [app employees]
                    (. js/document (getElementById "app")))

