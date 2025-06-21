(ns robertluo.dashcraft.chart
  (:require
   [replicant.alias :refer [defalias]] 
   [echarts]))

(defn data->chart
  "turns clojure data into echart data"
  [{:keys [columns rows] :as data}]
  (-> data 
      (merge {:dataset {:dimensions columns :source (map #(map (fn [c] (get % c)) columns) rows)}})
      (dissoc :columns :rows)))

(comment
  (data->chart
   {:columns [:product "2015" "2016"],
    :rows [{:product "Shirts",    "2015" 15.3, "2016" 5}
           {:product "Cardigans", "2015" 9.1,  "2016" 20}
           {:product "Socks",     "2015" 4.8,  "2016" 25}]
    :xAxis {:type :category}
    :yAxis {}
    :series [{:type :bar} {:type :bar}]}))
  

(defalias 
  ^{:doc "
A chart component using ECharts as underlying.

## Special attributes

 - `::data` contains `:columns` and `:rows` as in data-table
 - `::on-event` event handler spec, it is a map of notification name to tuples as following:
   - event type, in keyword. e.g. `:click`
   - event query, a map corresponding to mouse event query. e.g. `:seriesName \"2015\"
   - a handle function accept params data
         
See https://apache.github.io/echarts-handbook/en/concepts/event for details.
          
## Data options

Except for `:column` and `:rows` which will be set to echarts' dataset,
the rest of data will send to echarts as options.

See https://echarts.apache.org/en/option.html for details.
   "}
  chart
  [{::keys [data notify] :as attrs}]
  (let [cht-js (-> data data->chart clj->js)]
    [:div.echart
     (merge attrs
            {:replicant/on-mount
             (fn [{:replicant/keys [node remember]}]
               (let [chart (echarts/init node)]
                 (.setOption chart cht-js)
                 (doseq [[notify-name [chart-evt query]] notify] 
                   (.on chart (name chart-evt)
                        (clj->js query)
                        (fn [params]
                          (let [evt (js/CustomEvent. (name notify-name) (clj->js {:detail {:data params}}))]
                            (.dispatchEvent node evt)))))
                 (remember chart)))
             :replicant/on-update
             (fn [{:replicant/keys [memory]}]
               (.setOption memory cht-js))
             :replicant/on-unmount
             (fn [{:replicant/keys [memory]}]
               (.dispose memory))})]))

(defn inc-take [col]
  (->> (iterate inc 0) (take-while #(<= % (count col))) (map #(vec (take % col)))))

(comment
  (inc-take [0 1 0])) ;=> [] [0 1] [0 1 1] 
  

(defalias
  ^{:doc 
"
A bread crumb component to display path like data.

Special attributes:
 
  - `::items` to display
  - `::on-click` event triggered when user clicked an item
  - `::label-of` function takes an item returns a string
"}
  bread-crumb
  [{::keys [items on-click label-of] :or {label-of str} :as attrs}]
  [:ul (merge {:class ["breadcrumb"]} attrs)
   (for [item items]
     [:li [:a {:href "#" :on {:click (fn [_] (on-click item))}} (or (label-of item) "#")]])])

(defn get-current 
  [drill-down rows path]
  (->>  (map (fn [p] #(get % p)) path)
        (interpose drill-down)
        (reduce (fn [r f] (f r)) rows)))

(defalias 
  ^{:doc "
A chart can be drill down on `:path` inside `::data`.
Special attributes:
          
  - `::data` full data
    - `:drill-down` a function drill down to same shape data (default to `:children`)
  - `::on-drill` an event when `:path` changed by user interaction
  - `::label-of` a function accept a record and return the navigation bar label
   "}
  drill-down
  [{::keys [data on-drill label-of] :as attrs}]
  (let [{:keys [path drill-down]
         :or   {path [] drill-down :children}} data 
        get-current (partial get-current drill-down)]
    [:div attrs
     [bread-crumb {::items (inc-take path)
                   ::on-click (fn [idx] (on-drill idx))
                   ::label-of (fn [p] (when label-of (label-of (get-current (:rows data) p))))}]
     [chart {::data (update data :rows #(cond-> (get-current % path) (seq path) drill-down))
             ::notify {:notify [:click {}]}
             :on {:notify (fn [evt] 
                            (when-let [idx (-> evt .-detail (js->clj :keywordize-keys true) :data :dataIndex)]
                              (let [new-p (conj path idx)
                                    children (-> data :rows (get-current new-p) drill-down)]
                                (when (seq children) (on-drill new-p)))))}}]]))
