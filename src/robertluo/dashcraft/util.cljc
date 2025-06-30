(ns ^:util robertluo.dashcraft.util
  "Utility functions")

(defn custom-event
  "Returns a js custom event with `event-name`, `detail` and `options` (default bubbles)"
  ([event-name detail]
   (custom-event event-name detail {:bubbles true}))
  ([event-name detail options]
   #?(:cljs (js/CustomEvent. (name event-name) (clj->js (merge options {:detail detail})))
      :clj {:event-name event-name :params detail :options options})))