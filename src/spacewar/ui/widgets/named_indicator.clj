(ns spacewar.ui.widgets.named-indicator
  (:require [quil.core :as q]
            [spacewar.ui.protocols :as p]
            [spacewar.geometry :refer :all]
            [spacewar.ui.config :refer :all]
            [spacewar.ui.widgets.lights :as lights]))

(deftype named-indicator [state]
  p/Drawable
  (get-state [_] state)
  (clone [_ clone-state] (named-indicator. clone-state))
  (draw [_]
    (let [{:keys [x y name]} state]
      (p/draw-elements state)
      (apply q/fill black)
      (q/text-align :left :top)
      (q/text-font (:lcars (q/state :fonts)) 18)
      (q/text name (+ x 25) y)))

  (setup [_] (named-indicator.
               (assoc state
                 :indicator (p/setup
                              (lights/->indicator-light state))
                 :elements [:indicator])))

  (update-state [_ world]
    (let [state (p/change-elements state [[:indicator :level (:level state)]])
          [state events] (p/update-elements state world)]
      (p/pack-update
        (named-indicator. state)
        events))))