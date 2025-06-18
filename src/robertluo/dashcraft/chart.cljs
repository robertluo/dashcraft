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
    :series [{:type :bar} {:type :bar}]})
  )

(defalias 
  ^{:doc "
A chart component using ECharts as underlying.

## Special attributes

 - `::data` contains `:columns` and `:rows` as in data-table
 - `::on-event` event handler spec, it is a vector of tuples as following:
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
  [{::keys [data on-event] :as attrs}]
  (let [cht-js (-> data data->chart clj->js)]
    [:div
     (merge attrs
            {:replicant/on-mount
             (fn [{:replicant/keys [node remember]}]
               (prn "Mounting chart: " (.-offsetWidth node) (.-offsetHeight node))
               (let [chart (echarts/init node)]
                 (.setOption chart cht-js)
                 (doseq [[event-name query func] on-event]
                   (.on chart (name event-name)
                        (clj->js query)
                        (fn [params] (func (js->clj params :keywordize-keys true)))))
                 (remember chart)))
             :replicant/on-update
             (fn [{:replicant/keys [memory]}]
               (.setOption memory cht-js))
             :replicant/on-unmount
             (fn [{:replicant/keys [memory]}]
               (.dispose memory))})]))
