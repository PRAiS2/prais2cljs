(ns prais2.examples.local-state
  (:require
    [rum.core :as rum]
    [prais2.examples.core :as core]))


;; Local component state


(rum/defcs local-state < (rum/local 0)
  [state title]
  (let [*count (:rum/local state)]
    [:div
     {:style {"-webkit-user-select" "none"
              "cursor" "pointer"}
      :on-click (fn [_] (swap! *count inc)) }
     title ": " @*count]))


#?(:cljs
(defn mount! [mount-el]
  (rum/mount (local-state "Clicks count") mount-el)))
