(ns ^:figwheel-always prais2.chrome
    (:require
      #?(:cljs [goog.string :refer [unescapeEntities]])
      [rum.core :as rum]
      #?(:cljs [cljsjs.jquery])
      #?(:cljs [cljsjs.bootstrap])
      [prais2.utils :refer [key-with]]
      #?(:cljs [prais2.core :as core :refer [event-bus]])
      #?(:clj [prais2.core :as core])

      [prais2.components.data-selector :refer [data-selector]]
      ;[prais2.logger :as logger]
      ;[prais2.data :as data]
      ))

(defn rgba-string
  "return CSS rgba string"
  [[r g b a]]
  (str "rgba(" r "," g "," b "," a)
  )


(defrecord  Nav-item [long-title short-title class icon])

#?(:cljs (def nbsp (unescapeEntities "&nbsp;")))
#?(:clj (def nbsp " "))                  ;; not yet sure how to handle entities in java rum hiccup

(def what-why (str "What," nbsp "why," nbsp "how?"))
(def everything-else (str "Everything" nbsp "else"))

(def nav-items {
                :home (Nav-item. "Home" "Home" "nav-item home" "home")
                :intro (Nav-item. what-why what-why "nav-item intro" "question")
                :data  (Nav-item. "Data" "Explore the data" "nav-item data" "table")
                :faqs  (Nav-item. everything-else everything-else "nav-item faqs" "info")})


(rum/defc bs-nav-link [active? nav-item click-handler]
  [:li
   [:a.navbar-btn {:on-click click-handler
                   :class (str (if active? " active " " ") (:class nav-item))}
    [:i.fa {:class (str "fa-" (:icon nav-item))}]
    (str " " (:short-title nav-item))]])


(rum/defc bs-fixed-navbar  [active-key]
  (let [nav-item (active-key nav-items)]
    [:nav.navbar.navbar-simple.navbar-fixed-top
     [:.navbar-inner
      [:.container
       [:.navbar-header {:key 1}
        [:button.navbar-toggle.collapsed {:key 1
                                          :type "button"
                                          :data-toggle "collapse"
                                          :data-target "#navbar"
                                          :aria-expanded "false"
                                          :aria-controls "navbar"}
         [:span.sr-only {:key 1} "Toggle navigation"]
         [:span.icon-bar {:key 2}]
         [:span.icon-bar {:key 3}]
         [:span.icon-bar {:key 4}]]]
       [:#navbar.navbar-collapse.collapse {:key 2}
        [:ul.nav.navbar-nav.navbar-right {:key 1}

         #?(:cljs                                           ;only supply real button click handlers once we're loaded
            (map-indexed #(key-with %1 (bs-nav-link
                                         (= active-key %2)
                                         (%2 nav-items)
                                         (fn [e] (core/click->event-bus e %2 (if (= %2 :data) :map :top)))))
                         (keys nav-items)))
         ]]]]]))


(rum/defc header < rum/reactive [& deep]
  [:div
   (bs-fixed-navbar (:page (rum/react core/app)))

   [:.chrome-head-rel
    [:div {:key 1}]                                         ;;styled logo
    [:h3.main-title {:key 2} "UNDERSTANDING CHILDREN’S HEART SURGERY OUTCOMES"]]

   (data-selector)
   ])

(rum/defc footer []
  [:.container-fluid.partners
   [:.row.visible-xs-block.center-block
    {:style {:width "300px" :color "white"}}
    ;.col-xs-1.col-md-offset-1.col-md-11 {:style {:vertical-align "middle" }}

    [:h4 [:a (core/href "faq/4/0") "Project partners:"]]
    [:p "University College, London"]
    [:p "University of Cambridge"]
    [:p "King's College, London"]
    [:p "Sense about Science"]
    [:p "Children's Heart Federation"]
    ]
   [:.row.hidden-xs
    ;.col-xs-1.col-md-offset-1.col-md-11 {:style {:vertical-align "middle" }}
    [:.col-sm-offset-1.col-sm-11.collab-logo
     [:a (core/href "faq/4/0") "About us"]]
    [:.col-sm-offset-1.col-sm-11.collab-logo
     [:img
      (core/isrc "assets/ucl-logo.png" :style {:width "170px"})]
     [:img
      (core/isrc "assets/camlogo.png" :style {:width "200px"})]
     [:img
      (core/isrc "assets/KCLlogo.gif" :style {:width "110px"})]
     [:img
      (core/isrc "assets/sas-logo.png" :style {:width "80px"})]
     [:img
      (core/isrc "assets/chf-logo.png" :style {:width "10px"})]]]
   #_[:.row
      [:.col-md-8
       (data/option-menu event-bus)]]
   [:.row.footer
    [:.acks.col-md-offset-1
     ;[:.pull-right (logger/playback-controls)]             ;;WARNING - likely to break clj compilation
     [:h3
      "Funding acknowledgement"]
     [:p
      "This project was funded by the National Institute for Health Research Health Services and Delivery Research Programme\n(project number 14/19/13)"]
     [:h3
      "Department of Health disclaimer"]
     [:p
      "The views and opinions expressed therein are those of the authors and do not necessarily reflect those of the Health Services and Delivery Research Programme, NIHR, NHS or the Department of Health."]]]])



