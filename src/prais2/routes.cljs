(ns ^:figwheel-always prais2.routes
  (:require                                                 ;[secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [cljs.reader :refer [read-string]]
            [cljs.core.async :refer [put!]]
            [bidi.bidi :as bidi]
            [prais2.core :as core]
            )
  (:import goog.History)
  )


(enable-console-print!)

;;;
;; basic hashbang routing to configure some options
;; :todo. If you change this, you must also change prais2.core/irl
;;;
;(secretary/set-config! :prefix "#")

(comment                                                    ; secretary routing
  ;;;
  ;; client-side routes
  ;;;
  (defroute faqs "/faqs" []
            (put! core/event-bus [:faqs :top])
            )

  (defroute faq "/faq/:section/:id" [section id]
            (let [s (.parseInt js/Number section)
                  f (.parseInt js/Number id)
                  ap @core/app]
              (if (not (and
                         (= (:page ap) :faqs)
                         (= (:section ap) [s f])))
                (do
                  (put! core/event-bus [:show-faq [s f]])
                  (prn "faq match" s f))))
            )

  (defroute homes "/home" []
            (put! core/event-bus [:home :top])
            )

  (defroute home "/home/:id" [id]
            (put! core/event-bus [:home id])
            (prn "home :id match")
            )

  (defroute intros "/intro" []
            (put! core/event-bus [:intro :top])
            )

  (defroute intro "/intro/:id" [id]
            (put! core/event-bus [:intro id])
            (prn "intro :id match")
            )

  (defroute datas "/data" []
            (put! core/event-bus [:data :animation]))

  (defroute data "/data/:id" [id]
            (prn (str "data " id " match"))
            (put! core/event-bus [:data (keyword id)])
            ;(put! core/event-bus [:data id])
            )

  (defroute index "/" []
            (prn "index match")
            (put! core/event-bus [:home :top])
            ))

#_(defroute other "*" []
            (prn "* match")
            )



;; history configuration.
;;
;; The invisible element "dummy" is needed to make goog.History reloadable by
;; figwheel. Without it we see
;; Failed to execute 'write' on 'Document':
;; It isn't possible to write into a document from an
;; asynchronously-loaded external script unless it is explicitly
;;
;; Note that this history handling must happen after route definitions for it
;; to kick in on initial page load.
;;
(def history (let [h (History. false false "dummy")]
               (goog.events/listen h EventType/NAVIGATE #(do
                                                          (js/console.log %)
                                                          (prn "Navigate event " (.-isNavigation %))
                                                          ;(secretary/dispatch! (.-token %))
                                                          (js/window.scrollTo 0 0)))
               (doto h (.setEnabled true))
               h))

;;
;; pushy config
;;


;;
;; accountant
;;
#_(accountant/configure-navigation!
  {:nav-handler  (fn [path] (secretary/dispatch! path))
   :path-exists? (fn [path] (secretary/locate-route path))})

;;
;; When the user presses the back or forwards button, onpopstate is fired.
;; We should use this to dispatch the new URL in javascript.
;;
(set! (.-onpopstate js/window) #(do
                                 (prn "popstate " (.. js/window -location -hash))
                                 ;(js/console.log %)
                                 ;(swap! core/app assoc :need-a-push false)
                                 ;(secretary/dispatch! (.. js/window -location -pathname))
                                 ))

