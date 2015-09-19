(ns ^:figwheel-always prais2.data
    (:require [rum :as r]
              [jayq.core :refer ($)]
              [cljs.core.async :refer [put!]]
              [prais2.core :as core]
              [prais2.content :as content
               ])
    (:require-macros [jayq.macros :refer [ready]]))

;;;
;;
;; Sample jQuery usage:
;;  Attach the jQuery plugin only after React has mounted the selected element and jQuery has loaded.
;;
;; This indicates how we can use an off-the shelf DataTable fro jQuerey if it does what we want. It's likely
;; that we will create our own however.
;;
;;;

(defn data-table-on
  "returns a rum mixin to add jQuery DataTable to a table element once it is mounted"
  [selector]
  {:did-mount (fn [state]
                (ready
                 (.DataTable ($ selector)
                             (clj->js {:paging false
                                       :autoWidth false
                                       :columnDefs [{
                                                 :width "50%"
                                                 :target "0"
                                                 }]
                                       })))
                state)})


(defn sort-on-column
  "sort a column"
  [app column-key]
  (let [ap @app
        sort-column (:sort-by ap)
        sort-mode (:sort-ascending ap)]
    (prn ap)
    (if (= sort-column column-key)
      (swap! app #(assoc % :sort-ascending (not sort-mode)))
      (swap! app #(assoc % :sort-ascending true :sort-by column-key)))))

(defn handle-sort
  "handle sort click"
  [event app column-key]
  (let [ap @app]
    (prn column-key)
    (sort-on-column app column-key)))

(def chart-width 100)

(def min-outer-low  (apply min (map :outer-low (rest content/table1-data))))
;;  "the minimum outer-low value across all rows"
;; => 94.8


(defn bar-scale
  "value to pixel-width scale-factor controlled by slider in [0-1]"
  [slider]
  (/ chart-width (- 100 (* min-outer-low slider))))

(defn percent->screen
  "percent-value to slider compensated value"
  [slider value]
  (let [origin (* min-outer-low slider)]
    (* 100 (/ (- value origin) (- 100 origin))))
  )

(defn bar-width
  "return percentage-width for a bar"
  [slider value]
  (* value (bar-scale slider))
  )

(def colour-map-options
  {:brewer-RdYlBu
   {:low "#91bfdb"
    :inner "#fc8d59"
    :outer-low "#ffffbf"
    :outer-high "#ffffbf"
    :high "#91bfdb"
    :dot "white"
    }
   :brewer-RdYl
   {:low "white"
    :inner "#fc8d59"
    :outer-low "#ffffbf"
    :outer-high "#ffffbf"
    :high "white"
    :dot "black"}
   :brewer-YlRd
   {:low "white"
    :inner "#ffffbf"
    :outer-low "#fc8d59"
    :outer-high "#fc8d59"
    :high "white"
    :dot "black"}
   :brewer-BuGn
   {:low "white"
    :inner "#7fcdbb"
    :outer-low "#2c7fb8"
    :outer-high "#2c7fb8"
    :high "white"
    :dot "black"}
   :brewer-GnBu
   {:low "white"
    :inner "#3c8fc8"
    :outer-low "#7fcdbb"
    :outer-high "#7fcdbb"
    :high "white"
    :dot "black"}
   :christina
   {:low "white"
    :inner "#8FB4E1"
    :outer-low "#578FD2"
    :outer-high "#578FD2"
    :high "white"
    :dot "black"
    }
   :anitsirch
   {:low "white"
    :inner "#578FD2"
    :outer-low "#8FB4E1"
    :outer-high "#8FB4E1"
    :high "white"
    :dot "black"
    }
   })



(def colour-map (:anitsirch colour-map-options))


(r/defc bar < r/static [slider value fill]
  [:div.bar {
         :style {:background-color (str fill " !important") ;
                 :width (str (bar-width slider value) "%")}
         }])

;;
;;
;;
(r/defc slider < r/static [event-bus value min max step]
  [:.slider
   [:input {:type "range"
            :value value
            :min min
            :max max
            :step step
            :on-change #(put! event-bus [:slider-axis-value (.. % -target -value)])}
    ]])


(r/defc dot < r/static [slider size value]
  (let [px-size (str size "px")
        ]
    [:div.dot
     {:style {:background-color (str (:dot colour-map) " !important")
              :width px-size
              :height px-size
              :top (str (/ (- 25 size) 2) "px")
              :left (str "calc("
                         (percent->screen slider value)
                         "% - "
                         (/ size 2)
                         "px)"
                         )}}])  )

(r/defc chart-cell < r/static [row slider]
  [:td.chart-cell
   [:div.bar-chart
    (r/with-key (bar slider (- (:outer-low row) (* min-outer-low slider)) (:low colour-map)) :bar1)
    (r/with-key (bar slider (- (:inner-low row) (:outer-low row)) (:outer-low colour-map)) :bar2)
    (r/with-key (bar slider (- (:inner-high row) (:inner-low row)) (:inner colour-map)) :bar3)
    (r/with-key (bar slider (- (:outer-high row) (:inner-high row)) (:outer-high colour-map)) :bar4)
    (r/with-key (bar slider (- 100 (:outer-high row)) (:high colour-map)) :bar5)
    (r/with-key (dot slider 10 (:survival-rate row)) :dot)
    ]
   ]
  )

(defn px
  "value to pixel string"
  [value]
  (str value "px")
  )

(r/defc table-head < r/static
  [app headers column-keys event-bus slider-axis-value]
  (let [ap @app]
    [:thead {:key :thead}
     [:tr
      (for [column-key column-keys :when (-> headers column-key :shown)]
        (let [header (column-key headers)
              sortable (:sortable header)]
          [:th {:key [column-key "head"]
                :on-click (when sortable #(handle-sort % app column-key))
                :style {:width (px (:width header))
                        :vertical-align "top"
                        :cursor "pointer"
                        :background-color (str (:outer-low colour-map) "!important")
                        :color "#ffffff !important"
                        }}
           (when sortable [:i {:key :icon
                               :class (str  "fa fa-sort right"
                                            (if (= column-key (:sort-by ap))
                                              (if (:sort-ascending ap) "-asc" "-desc") ""))
                               :style {:pointer-events "none"}}])
           [:span {:key :text
                   :style {:pointer-events "none"}}
            (:title header)]]))
      [:th
       {:style {:width "auto"
                :background-color (str (:outer-low colour-map) "!important")
                :color "#ffffff !important"
                }}
       [:.axis-container
        {:key :axis
         :style {:height (px (:height (:observed headers)))}}
        [:p "Observed survival rate %"]
        [:div.slider-label
         [:span.left [:i.fa.fa-long-arrow-left] " full range"]
         [:span.right "full detail " [:i.fa.fa-long-arrow-right]]]
        (slider event-bus slider-axis-value 0 1 0.01)
        [:.tick
         [:div {:style {:height (px 0)}}]
         [:span.tick-label "50%"]]]]]]))


(r/defc table1 < r/reactive [app data event-bus]
  (let [ap (r/react app)
        sort-key (:sort-by ap)
        sort-direction (:sort-ascending ap)
        headers (first data)
        rows  (if sort-key
                (let [sorted (sort-by sort-key (rest data))]
                  (if sort-direction sorted (reverse sorted)))
                (rest data))
        column-keys (keys headers)
        slider-axis-value (:slider-axis-value ap)  ]
    [:div

     [:div.screenable
      ;; fixed table header for @media screen, hidden in print
      [:table.table.table-striped.table-bordered {:cell-spacing "0"}
       (table-head app headers column-keys event-bus slider-axis-value)]]
     [:div.printable
      ;; print header, hidden on screen
      [:table.table.table-striped.table-bordered {:cell-spacing "0"}
       (table-head app headers column-keys event-bus slider-axis-value)

       ;; body for print and screen
       [:tbody {:key :tbody}
        (for [row rows]
          [:tr {:key (:h-code row)}
           (for [column-key column-keys
                 :let [column-header (column-key headers)]
                 :when (:shown column-header)]
             [:td {:key [column-key "r"]
                   :style {:width (px (:width column-header))
                           :height (px (:height column-header))}}
              (column-key row)])
           (r/with-key (chart-cell row slider-axis-value) :bars)])]]]]

    )
  )
