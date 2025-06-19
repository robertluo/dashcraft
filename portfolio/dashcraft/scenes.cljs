(ns dashcraft.scenes
  (:require
   [portfolio.replicant :refer [defscene]]
   [portfolio.ui :as portfolio]
   [robertluo.dashcraft.chart :as ch]
   [robertluo.dashcraft.data-table :as dt]
   [robertluo.dashcraft.edn-editor :as ee]))

(defscene simple-chart
  :params (atom {:columns [:product "2015" "2016"],
                 :rows [{:product "Shirts",    "2015" 15.3, "2016" 5}
                        {:product "Cardigans", "2015" 9.1,  "2016" 20}
                        {:product "Socks",     "2015" 4.8,  "2016" 25}]
                 :xAxis {:type :category}
                 :yAxis {}
                 :series [{:type :bar} {:type :bar}]
                 :legend {}
                 :tooltip {}})
  [state]
  [ch/chart
   {::ch/data @state 
    ::ch/notify [[:click {:seriesName "2015"}]]
    :on {:notify (fn [evt] 
                   (prn (-> evt .-detail)))}}])

(def table-data
  {:columns [:name :balance :sex :age]
   :rows
   [{:name "Robert" :sex :male :age 23 :balance 1323442}
    {:name "Jane" :sex :female :age 15 :balance 61923}
    {:name "John" :sex :male :balance -456 :age 45}]
   :sorting {:sortable #{:balance :age}} ;specify which columns can be sorted
   })

(defscene simple-data-table
  [dt/table
   {:class :data-table ::dt/data table-data}])

(defscene grouping-data-table
  :params (atom (dt/grouping-data table-data {:column :sex :aggregations [[:balance (fnil + 0)] [:age]]}))
  [state]
  [dt/table
   {:class :data-table ::dt/data @state}
   [dt/th {::dt/lable-of (fn [v] (case v ::ch/group "" (name v)))}
    [dt/sort-button {::dt/sorting (:sorting @state)
                     ::dt/on-sort (fn [st] (swap! state #(-> % (assoc :sorting st) (update :rows dt/sort-rows st))))}]]
   [dt/td {::dt/class-of (fn [column _] (cond-> [] (= column :balance) (conj "number-cell")))}]])

(defscene simple-edn-editor
  :params (atom {:name "Old Gaffer"
                 :address {:street "Bagshot Row"
                           :number 1
                           :additional-key "hello"}
                 :items [{:item :spade
                          :price 3.2
                          :in-stock true}
                         {:item :pipe
                          :price 2.7
                          :in-stock false}]
                 :instructions ["please" "send" "help"]
                 :foo [1 "hello" false]})
  [state]
  [:div
   [ee/editor
    {::ee/schema
     [:map
      [:name :string]
      [:address
       [:orn
        [:structured [:map
                      [:street [:string {:min 1}]]
                      [:number {:optional true} :int]]]
        [:raw :string]]]
      [:items
       [:vector
        [:map
         [:item [:enum :fork :spade :pipe]]
         [:price {:optional true} :double]
         [:in-stock {:optional true} :boolean]]]] 
      [:instructions [:or
                      :string
                      [:vector :string]]]
      [:metadata [:map-of :keyword :string]]
      [:foo [:tuple :int :string :boolean]]]
     ::ee/value @state
     ::ee/on-change (fn [v] (reset! state v))}]])

(defn main []
  (portfolio/start!
   {:config
    {:css-paths ["/css/styles.css"]
     :viewport/defaults
     {:background/background-color "#fdeddd"}}}))
