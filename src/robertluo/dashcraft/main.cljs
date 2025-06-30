(ns robertluo.dashcraft.main
  (:require 
   [replicant.dom :as r]
   [replicant.alias :refer [defalias]]
   [robertluo.dashcraft.chart :as ch]
   [robertluo.dashcraft.form :as fm]
   [robertluo.dashcraft.loading :as ld]
   [robertluo.dashcraft.error-aware :as ew]))

(defalias login-form
  [{::keys [login-data update-to]}]
  (let [{:keys [error loading]} login-data
        schema [:map
                [:username :string]
                [:password {:input-type :password} :string]]]
    [::ew/error-aware-container
     {::ew/error error
      :on {:dismiss (fn [_evt] (update-to [] (fn [fm-data] (dissoc fm-data :error))))}}
     [::ld/loading-container
      {::ld/loading? loading}
      [::fm/form#login-form
       {::fm/schema schema
        ::fm/data login-data
        ::fm/button-label "Login"
        :on {:submit (fn [evt]
                       (.preventDefault evt)
                       (let [[data errors] (fm/data&errors schema (fm/form-data evt))]
                         (if errors
                           (update-to [] (fn [fm-data] (assoc fm-data :error :errors)))
                           (update-to [] (fn [fm-data] (-> fm-data (merge data) (assoc :loading true)))))))}}]]]))

(defn db->ui [db update-to]
  [:div
   (if (:username db)
     [login-form {::login-data (get-in db [:login-form])
                  ::update-to (fn [path f] (update-to (conj path :login-form) f))}]
     [::ch/chart {:id :chart-panel ::ch/data (:chart-panel db)}])])

(def init-data 
  {:chart-panel
   {:columns [:product "2015" "2016"],
    :rows [{:product "Shirts",    "2015" 15.3, "2016" 5}
           {:product "Cardigans", "2015" 9.1,  "2016" 20}
           {:product "Socks",     "2015" 4.8,  "2016" 25}]
    :xAxis {:type :category}
    :yAxis {}
    :series [{:type :bar} {:type :bar}]
    :legend {}
    :tooltip {}}})

(defonce store (atom {}))

(defn ^:export run [store]
  (let [app-div (js/document.getElementById "app")] 
    (add-watch store ::render 
               (fn [_ _ _ db]
                 (->> (db->ui db (fn [path f] (swap! store update-in path f)))
                      ((fn [d] (prn "db: "d) d))
                      (r/render app-div))))
    (reset! store init-data)))

(set! (.-onload js/window) #(run store))

(comment
  (run store)
  (swap! store assoc :username :doit)
  )