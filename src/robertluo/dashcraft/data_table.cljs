(ns robertluo.dashcraft.data-table
  (:require
   [replicant.alias :refer [defalias]]
   [replicant.hiccup :as hiccup]))

(defn grouping-data
  "
returns `data` specified by `grouping` spec:
   
  - `:column` on which column group by
  - `:aggregations` a vector specific summary data for columns, it is a pair of
   - column
   - function for summarize two data, default to `(fnil + 0)`
   "
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

(defn ^:no-doc switch-sorting
  [sorting column]
  (-> sorting
      (assoc :column column)
      (update :order (fn [o] (case o :asc :desc :desc nil :asc)))))

(defalias 
  ^{:doc "
A table header component.
Special attributes:
          
 - `::column`
 - `::label-of` an optional function accept the column returns a string.

Can have children which inherites `::column`
         "}
  th
  [{::keys [column label-of] :or {label-of (fn [v] (str v))} :as attrs}
   children]
  [:div attrs
   [:span (or (label-of column) " ")]
   (map #(hiccup/update-attrs % assoc ::column column) children)])

(defalias 
  ^{:doc 
    "
A sort button for `th`, generating sorting preference from the user.
Special attributes:
     
 - `column`
 - `on-sort` an event when user clicks the sort button
 - `sorting` a map contains the following keys:
   - `column` which column to sort
   - `order` the order of the sort, `:asc`, `:desc` and nil
   - `sortable` a predict accept a column and returns true if the column support sorting
"}
  sort-button 
  [{::keys [column on-sort sorting] :as attrs}]
  (let [{sort-clm :column order :order sortable :sortable :or {sortable (constantly true)}} sorting
        sortable? (sortable column)]
    [:span (cond-> (merge attrs {:class (cond-> [] sortable? (conj "clickable"))})
             sortable? (merge {:on {:click (fn [_] (on-sort (switch-sorting sorting column)))}}))
     (cond
       (= sort-clm column) (case order :asc " 🔺" :desc " 🔻" " ↕️")
       sortable? " ↕️"
       :else " ")]))

(defalias 
  ^{:doc "
Display cell of data table.
        
special attributes:
 - `::column` the column of the cell
 - `::cell` the value of this cell
 - `::label-of` an optional function accept column and cell returns a string as the content
 - `::class-of` an optional function accept column and cell returns the additional class vector.

Can have children who inherit `::column` and `::cell`.
      "}
  td
  [{::keys [column cell label-of class-of] 
    :or {class-of (constantly []) 
         label-of (fn [_ v] (str v))}
    :as attrs}
   children]
  (let [classes (class-of column cell)]
    [:div (update attrs :class concat classes)
     [:span
      (or (label-of column cell) " ")]
     (map #(hiccup/update-attrs % assoc ::column column ::cell cell) children)]))

(defn sort-rows
  "sort `rows` by `sorting` returns sorted rows"
  [rows sorting]
  (let [{sort-clm :column order :order} sorting]
    (cond->> rows (and sort-clm order) (sort-by sort-clm (if (= order :asc) < >)))))

(defalias 
  ^{:doc "
Data table component.

  - `::data` data to be displayed.
    - `:columns` the columns of the data
    - `:rows` the rows of the data, each row is a map corresponding to columns, only fields specified in `:columns`
           will be displayed.
     
Chidlren:
       
  - a table-header default to `th`
  - a table-cell default to `td`
"}
  table 
  [{::keys [data] :as attrs}
   [table-header table-cell]]
  (let [{:keys [columns rows]} data]
    [:div (merge {:class ["data-table"]} attrs)
     [:table
      [:thead
       [:tr
        (for [clm columns]
          [:th (hiccup/update-attrs (or table-header [th]) assoc ::column clm)])]]
      [:tbody
       (map-indexed
        (fn [idx row]
          [:tr {:replicant/key idx}
           (for [clm columns
                 :let [cell (get row clm)]]
             [:td
              (hiccup/update-attrs (or table-cell [td]) assoc ::column clm ::cell cell)])])
        rows)]]]))
