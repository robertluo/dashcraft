(ns dashcraft.scenes
  (:require 
   [portfolio.replicant :refer [defscene]] 
   [portfolio.ui :as portfolio]
   [robertluo.dashcraft.chart :as ui]
   [robertluo.dashcraft.data-table :as dt]))

(defscene simple-chart
  [ui/chart 
   {:class :echart
    ::ui/data
    {:columns [:product :price :sales]
     :rows [["Shirts", 15.3, 5]
            ["Cardigans", 9.1, 20]
            ["Socks", 4.8, 25]]
     :chart/x-axis :product
     :chart/series [{:type :bar :y-axis :sales}
                    {:type :line :y-axis :price}]}}])

(def table-data 
  {:columns [:name :balance :sex :age]
   :rows
   [{:name "Robert" :sex :male :age 23 :balance 1323442}
    {:name "Jane" :sex :female :age 15 :balance 61923}
    {:name "John" :sex :male :balance -456 :age 45}]
   :sorting {:sortable [:balance]}})

(defscene simple-data-table
  :params (atom table-data)
  [state]
  [dt/table
   {:class :data-table ::dt/data @state}
   [dt/th {::dt/on-sort (fn [st] (swap! state #(-> % (assoc :sorting st) (update :rows dt/sort-rows st))))
           ::dt/lable-of (fn [v] (case v ::ui/group "" (name v)))}]
   [dt/td {::dt/class-of (fn [column _] (cond-> [] (= column :balance) (conj "number-cell")))}]])

(defscene grouping-data-table
  :params (atom (dt/grouping-data table-data {:column :sex :aggregations [[:balance (fnil + 0)] [:age]]}))
  [state]
  [dt/table
   {:class :data-table ::dt/data @state}
   [dt/th {::dt/on-sort (fn [st] (swap! state #(-> % (assoc :sorting st) (update :rows dt/sort-rows st))))
           ::dt/lable-of (fn [v] (case v ::ui/group "" (name v)))}]
   [dt/td {::dt/classes-of (fn [column _] (cond-> [] (= column :balance) (conj "number-cell")))}]])

(defn main []
  (portfolio/start!
   {:config
    {:css-paths ["/css/styles.css"]
     :viewport/defaults
     {:background/background-color "#fdeddd"}}}))
