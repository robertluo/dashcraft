(ns robertluo.dashcraft.chart
  (:require
   [replicant.alias :refer [defalias]] 
   [echarts]))

(defn data->chart 
  [{:keys [columns rows chart/x-axis chart/series]}]
  (let [x-axis (or x-axis (first columns))
        cell-rows (map (partial zipmap columns) rows)]
    {:xAxis {:data (map x-axis cell-rows)}
     :yAxis {}
     :series (for [{:keys [type y-axis] :or {type :line y-axis (second columns)}} series] 
               {:name y-axis :type type :data (map y-axis cell-rows)})}))

(comment
  (data->chart
   {:columns [:product :sales]
    :rows [["Shirts", 5]
           ["Cardigans", 20]
           ["Socks", 25]]
    :chart/x-axis :product
    :chart/series [{:type :bar :y-axis :sales}]}) ;=>
  {:xAxis {:data ["Shirts", "Cardigans", "Socks"]} 
   :yAxis {}
   :series [{:name "sales" :type "bar" :data [5, 20, 25]}]}
  )

(defn render-chart
  [attrs chart-data]
  (let [cht-js (clj->js chart-data)]
    [:div
     (merge attrs
            {:replicant/on-mount
             (fn [{:replicant/keys [node remember]}]
               (prn "Mounting chart: " (.-offsetWidth node) (.-offsetHeight node))
               (let [chart (echarts/init node)]
                 (.setOption chart cht-js)
                 (remember chart)))
             :replicant/on-update
             (fn [{:replicant/keys [memory]}]
               (.setOption memory cht-js))})]))

(defalias chart
  [{::keys [data] :as attrs}]
  (render-chart attrs (data->chart data)))
