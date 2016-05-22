(ns ^:figwheel-always prais2.mugshots
  (:require [rum.core :as rum]
            [prais2.core :as core]
            [prais2.content :as content]
            [clojure.string :as str]))

(rum/defc
  mug < rum/static [name]
  [:.col-xs-6.col-sm-3
   [:img.img-responsive (core/isrc (str "assets/mugshots/" name "-med.jpg") :key 1)]
   [:p.text-center {:key 2} (str/capitalize name)]
   ])

(rum/defc
  mugshots []
  [:.row
   (map-indexed #(rum/with-key (mug %2) %1) (content/get-mugshots))
   ])



