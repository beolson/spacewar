(ns spacewar.ui.protocols
  (:require [clojure.spec.alpha :as s]))

;update-state returns [new-drawable [events]]
(defprotocol Drawable
  (draw [this])
  (setup [this])
  (update-state [this commands])
  (get-state [this])
  (clone [this state]))


(s/def ::elements (s/coll-of keyword?))
(s/def ::drawable-state (s/keys :opt-un [::elements]))
(s/def ::event keyword?)
(s/def ::event-map (s/keys :req-un [::event]))
(s/def ::updated-elements-and-events (s/tuple ::drawable-state (s/coll-of ::event-map)))
(s/def ::command keyword?)
(s/def ::global-state map?)
(s/def ::command-map (s/keys :req-un [::command]))
(s/def ::commands (s/coll-of ::command-map))
(s/def ::commands-and-state (s/keys :req-un [::commands ::global-state]))

(defn update-elements [container-state commands]
  {:pre [
         (s/valid? ::drawable-state container-state)
         (s/valid? ::commands-and-state commands)
         ]
   :post [
          (s/valid? ::updated-elements-and-events %)
          ]}
  (let [elements (:elements container-state)]
    (if (nil? elements)
      [container-state []]
      (loop [elements elements
             key-vals []
             cum-events []]
        (if (empty? elements)
          [(apply assoc container-state (flatten key-vals))
           (flatten cum-events)]
          (let [element-tag (first elements)
                element (element-tag container-state)
                [updated-drawable events] (update-state element commands)]
            (recur (rest elements)
                   (conj key-vals [element-tag updated-drawable])
                   (conj cum-events events))))))))

(defn draw-elements [state]
  (doseq [e (:elements state)]
    (draw (e state))))

(defn pack-update
  ([new-drawable]
   [new-drawable []])
  ([new-drawable event]
   (if (some? event)
     [new-drawable [event]]
     [new-drawable []])))

(defn get-command [command-id commands]
  (loop [commands commands]
    (if (empty? commands)
      nil
      (let [command (first commands)]
        (if (= command-id (:command command))
          command
          (recur (rest commands)))))))

(defn assoc-element [drawable-state element key value]
  (let [drawable-element (element drawable-state)
        element-state (get-state drawable-element)]
    (assoc drawable-state
      element
      (clone drawable-element (assoc element-state key value)))))