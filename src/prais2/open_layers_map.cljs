(ns ^:figwheel-always prais2.open-layers-map
    (:require [rum.core :as rum]
;              [jayq.core :refer [$]]
              [cljsjs.jquery]
              [cljs.core.async :refer [<! put! timeout]]
              [prais2.core :as core :refer [event-bus]]
              [prais2.content :as content]
              [prais2.data :as data]
              [prais2.utils :refer [key-with]]
              [ol.Feature]
              [ol.Overlay]
              [ol.source.OSM]
              ;; This lot appears to be pulled in by the above anyway
              ;;              [ol.Map]
              ;;              [ol.layer.Tile]
              ;;              [ol.layer.Vector]
              ;;              [ol.View]
              ;;              [ol.proj]
              ;;              [ol.source.Vector]
              ;;              [ol.style.Style]
              ;;              [ol.style.Icon]
              ;;              [ol.geom.Point]
              ;;              [ol.animation]
              )
    (:require-macros [cljs.core.async.macros :refer [go]]))

;;;
;; See js example in http://openlayers.org/en/v3.9.0/examples/icon.html
;;;

(def iconStyle
  (new js/ol.style.Style
       (clj->js
        {:image (new js/ol.style.Icon
                     (clj->js {:anchor (clj->js [0.5 33])
                               :anchorXUnits "fraction"
                               :anchorYUnits "pixels"
                               :opacity 0.6
                               :src "/assets/icon.png"
                               }))
         })))

(defn map-point
  "Generate map coordinates given longitude and latitude in degrees"
  [lat lon]
  (.transform (.-proj js/ol)
              (clj->js [lon lat])  ; elsewhere it's [lat lon] !
              "EPSG:4326"
              "EPSG:3857"
              ))

(defn hospital-marker
  [row]
  (let [marker (new js/ol.Feature (clj->js
                                   {:geometry (new js/ol.geom.Point
                                                   (map-point (:h-lat row) (:h-lon row)))
                                    :code (keyword (:h-code row))
                                    :name (:h-name row)
                                    :count (:n-ops row)
                                    }))]
    (.setStyle marker iconStyle)
    marker))

(defn hospital-markers
  []
  (let [rows (rest ((data/table-data (:datasource @core/app))))]
    (clj->js
     (map hospital-marker rows))))

(defn vectorSource
  []
  (new js/ol.source.Vector
       (clj->js {:features (hospital-markers)})))

(defn vectorLayer
  []
  (new js/ol.layer.Vector
       (clj->js {:source (vectorSource)})))

(def tileLayer
  (new js/ol.layer.Tile
       (clj->js {:preload 4
                 :source (new js/ol.source.OSM)})))

(def mapView
  (new js/ol.View (clj->js {:center (map-point 53.7 -3.4)
                            :zoom 5.5})))
#_(defonce hospital-map
  (new js/ol.Map (clj->js {:target "open-map"
                           :layers [tileLayer (vectorLayer)]
                           :view mapView
                           :interactions [] ; to disable mouse wheel and pan
                           :loadTilesWhileAnimating true})))


;;
;; Rum mixin which initialises an openlayer map on a component once the React component has mounted
;;


(defn project [lat lon]
  (.fromLonLat js/ol.proj (clj->js [lon lat])))


(declare map-click-handler)

(defonce ol-db (atom {:hospital-map nil
                      :popup nil}))

(def map-view
  {:did-mount (fn [state]
                (let [new-state (-> state
                                    (assoc :hospital-map     ;def hospital-map
                                           (new js/ol.Map (clj->js {:target "open-map"
                                                                    :layers [tileLayer (vectorLayer)]
                                                                    :view mapView
                                                                    :interactions [] ; to disable mouse wheel and pan
                                                                    :loadTilesWhileAnimating true})))
                                    (assoc :popup
                                           (new js/ol.Overlay
                                                (clj->js {:element (core/el "popup")
                                                          :positioning "bottom-center"
                                                          :stopEvent false}))))]
                  (.addOverlay (new-state :hospital-map) (new-state :popup))
                  (.on (new-state :hospital-map) "click" map-click-handler)
                  (reset! ol-db new-state)
                  new-state))
   
   :should-update (fn [_ _] false)         ; prevent React messing with the component that uses this mixin
   })

#_(def map-view
  {:did-mount (fn [state]
;; once?
                (swap! ol-db assoc :hospital-map     ;def hospital-map
                       (new js/ol.Map (clj->js {:target "open-map"
                                                :layers [tileLayer (vectorLayer)]
                                                :view mapView
                                                :interactions [] ; to disable mouse wheel and pan
                                                :loadTilesWhileAnimating true})))
;; once?
                (swap! ol-db assoc :popup
                  (new js/ol.Overlay
                       (clj->js {:element (core/el "popup")
                                 :positioning "bottom-center"
                                 :stopEvent false
                                 })))
;; once?
                (def once-only
                  (do
                    (.addOverlay (@ol-db :hospital-map) (@ol-db :popup))
                    (.on (@ol-db :hospital-map) "click" map-click-handler)))

                state)})




(defn map-click-handler
  [event]
  (let [the-map (@ol-db :hospital-map) ;(:map @core/app)
        feature (.forEachFeatureAtPixel the-map
                                         (.-pixel event)
                                         (fn [a _] a))]
    (when feature
      (put! event-bus [:click-on-map-marker (keyword (.get feature "code")) event])
      )))

; map zoom animation time
(def zoom-time 1000)

(defn zoom-to-location
  ([lat lon]
   (zoom-to-location lat lon 4))

  ([lat lon resolution]
   (prn (str "resolution = " resolution))
   (let [pan (.pan js/ol.animation (clj->js {:duration zoom-time
                                          :source (.getCenter mapView)
                                          :resolution (.getResolution mapView)
                                          }))
         zoom (.zoom js/ol.animation (clj->js {:duration zoom-time
                                               :source (.getCenter mapView)
                                               :resolution (.getResolution mapView)
                                               }))
         ]
     (.setCenter mapView (project lat lon))
     (.setResolution mapView resolution)
     ;(.beforeRender (:map @core/app) pan zoom)
     (.beforeRender (@ol-db :hospital-map) pan zoom)
     )))

(defn go-home
  "revert to map home location and zoom"
  []
  (zoom-to-location 54.5 -3 4000))

(defn go-london
  "revert to map home location and zoom"
  []
  (zoom-to-location 51.52 -0.15 40))

(defn zoom-to-feature
  "Handle a click on a map feature.
  This should behave like a react render even though it uses open-layers map software and so
  bypasses React. We should render from map-state stored in the app atom."

  []
  (let [ap @core/app
        h-code (:map-h-code ap)
        ; the-popup (:popup ap)
        element (js/$ "#popup")
        the-datasource (:datasource ap)
        row (h-code ((data/rows-by-code the-datasource)))
        lat (:h-lat row)
        lon (:h-lon row)
        coord (map-point lat lon)
        ]

    (zoom-to-location lat lon)
    (go (<! (timeout zoom-time))

        (.setPosition (@ol-db :popup) (clj->js [(first coord) (+ 150 (second coord))]))
        (.popover element (clj->js
                           {:placement "top"
                            :html true
                            :content (str "<h4>"(:h-name row) "</h4>" (:n-ops row) " operations")
                            }))
        (.popover element "show")
        (go (<! (timeout 2500))
            (.popover element "destroy")))
    (.popover element "hide")))


(rum/defc london-button []
  [:button.btn-info.london-button
   {:on-click #(core/click->event-bus % :just-london nil nil)
    :on-touch-start #(core/click->event-bus % :just-london nil nil)
    :tab-index 0}
   "Just London"])

(rum/defc home-button []
  [:button.btn-info.h-button
   {:on-click #(core/click->event-bus % :reset-map-to-home nil nil)
    :on-touch-start #(core/click->event-bus % :reset-map-to-home nil nil)
    :tab-index 0}
   "All UK"])

(rum/defc hospital-item [row]
  [:li [:a
        {:tab-index 0
         :on-click #(put! event-bus [:click-on-map-menu-item (keyword (:h-code row))])}
        (:h-name row)]])

(rum/defc hospital-list < rum.core/reactive []
  (let [rows ((:datasource (rum.core/react core/app)) (:hosp-data (rum.core/react core/app)))]
    [:ul.dropdown-menu
     {:aria-labelled-by "drop1"}
     (map-indexed key-with (map hospital-item rows))]))

#_(rum/defc hospital-dropdown []
  [:.dropdown
   (map-indexed key-with [(menu-button) (hospital-list)])
   ])

(rum/defc hospitals < map-view rum.core/reactive []
  [:#open-map.hospital-map {:tab-index 0 :key 1}
     (london-button)
     (home-button)
     [:.col-xs-12 {:key 2}
      [:#popup {:key 2}]
      ]
     ]
  #_[:div.row
])
