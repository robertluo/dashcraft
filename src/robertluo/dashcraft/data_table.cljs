(ns robertluo.dashcraft.data-table
  (:require
   [replicant.alias :refer [defalias]]
   [replicant.hiccup :as hiccup]))

(defn grouping-data
  [data grouping]
  (let [{g-clm :column aggregations :aggregations} grouping]
    (-> data
        (update :columns #(cons ::group %))
        (update :rows
                (fn [rows]
                  (->> rows
                       (group-by g-clm)
                       (mapcat
                        (fn [[v irows]]
                          (cons
                           (merge {::group v}
                                  (reduce
                                   (fn [acc row]
                                     (->> aggregations
                                          (reduce
                                           (fn [v [clm f]]
                                             (merge-with (or f (fnil + 0)) v (select-keys row [clm])))
                                           acc)))
                                   {}
                                   irows))
                           irows)))))))))

(comment
  (grouping-data
   {:columns [:name :sex :age :balance]
    :rows [{:name "Robert" :sex :male :age 23 :balance 1323442}
           {:name "Jane" :sex :female :age 15 :balance 61923}
           {:name "John" :sex :male :balance -456 :age 45}]}
   {:column :sex :aggregations [[:balance] [:age]]}))

(defn switch-sorting
  [sorting column]
  (-> sorting
      (assoc :column column)
      (update :order (fn [o] (case o :asc :desc :desc nil :asc)))))

(defalias th
  [{::keys [column data on-sort lable-of] :or {lable-of (fn [v] [:span (str v)])} :as attrs}]
  (let [{sort-clm :column order :order sortable :sortable :as sorting} (get data :sorting)
        sortable? ((set sortable) column)]
    [:div
     (lable-of column)
     [:span (cond-> (merge attrs {:class (cond-> [] sortable? (conj "clickable"))})
              sortable? (merge {:on {:click (fn [_] (on-sort (switch-sorting sorting column)))}}))
      (cond
        (= sort-clm column) (case order :asc " 🔺" :desc " 🔻" " ↕️")
        sortable? " ↕️"
        :else "")]]))

(defalias td
  "display cell of data table.
   special attributes:
    - `::column` the column of the cell
    - `::cell` the value of this cell
    - `::lable-of` an optional function accept column and cell returns a string as the content
    - `::class-of` an optional function accept column and cell returns the additional class vector"
  [{::keys [column cell lable-of class-of] :or {class-of (constantly []) lable-of (fn [_ v] v)} :as attrs} children]
  (let [classes (class-of column cell)]
    [:div (update attrs :class #(concat % classes))
     [:span
      (lable-of column cell)]
     children]))

(defn sort-rows
  "sort `rows` by `sorting` returns sorted rows"
  [rows sorting]
  (let [{sort-clm :column order :order} sorting]
    (cond->> rows (and sort-clm order) (sort-by sort-clm (if (= order :asc) < >)))))

(defalias table
  "Data table component.
   Special attributes:
     - `::data` data to be displayed.
       - `:columns` the columns of the data
       - `:rows` the rows of the data, each row is a map corresponding to columns, only fields specified in `:columns`
         will be displayed."
  [{::keys [data] :as attrs}
   [table-header table-cell]]
  (let [{:keys [columns rows]} data]
    [:div attrs
     [:table
      [:thead
       [:tr
        (for [clm columns]
          [:th (hiccup/update-attrs table-header assoc ::column clm ::data data)])]]
      [:tbody
       (map-indexed
        (fn [idx row]
          [:tr {:replicant/key idx}
           (for [clm columns
                 :let [cell (get row clm)]]
             [:td
              (hiccup/update-attrs table-cell assoc ::column clm ::cell cell)])])
        rows)]]]))