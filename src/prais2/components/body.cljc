(ns prais2.components.body
  (:require [rum.core :as rum]
            [prais2.components.analytics :as ana]))

;; watcha

(rum/defc body [#?@(:clj [content build-name]
                    :cljs [_ build-name])]
               [:body
                #?(:clj [:#app (content)]
                   :cljs [:#app                             ; the spinner is unused now!
                          [:i.fa.fa-spinner.fa-pulse
                           {:style {:font-size "100px"
                                    :position  "absolute"
                                    :top       "20%"
                                    :left      "calc(50% - 50px)"
                                    :color     "#000000"}}]])

                ;; goog.History needs this to make it reloadable in figwheel -->
                [:#dummy {:display "none"}]

                ;; Comment out following 3 lines to remove Google Analytics for cookie purposes
                ;; (ana/spa-analytics "UA-77529676-1")
                ;; [:script {:async true :src "https://www.google-analytics.com/analytics.js"}]
                ;; [:script {:async true :src "/js/autotrack.js"}]

                ;(browser-update)
                

                [:script {:src (str "/js/compiled/" build-name ".js")}]])
