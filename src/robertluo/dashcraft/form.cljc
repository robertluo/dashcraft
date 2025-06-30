(ns robertluo.dashcraft.form
  "A general form component"
  (:require
   [replicant.alias :refer [defalias]]
   [replicant.hiccup :as hiccup]
   [malli.core :as m]
   [malli.transform :as mt]
   #?(:clj [clojure.pprint :as pp]
      :cljs [cljs.pprint :as pp])))

(defn extract-entries [schema]
  (let [schema (m/schema schema)]
    (for [[key val-schema] (m/entries schema)]
      (let [props (m/properties val-schema)
            inner (first (m/children val-schema))]
        {:name      key
         :type      inner
         :attributes props}))))

^:rct/test
(comment
  (extract-entries [:map [:username {:description "hello"} :string] [:balance :int]]) ;=>>
  [{:name :username}])
  
(defn input-type-of [type]
  ({:string :text} type))

(defn form-data
  [submit-evt]
  #?(:cljs 
     (->> submit-evt .-target .-elements array-seq
          (map (fn [elem] [(keyword (.-name elem)) (.-value elem)]))
          (filter (fn [[_ v]] (not (#{"" nil} v))))
          (into {}))
     :clj submit-evt))

(defn data&errors
  [schema data]
  (let [v (m/coerce schema data mt/string-transformer identity #(assoc % ::has-error true))] 
    (if (::has-error v)
      [(:value v) 
       (some->> v :explain :errors 
                (map (juxt #(get-in % [:in 0]) #(or (:type %) ::m/string-transform)))
                (into {}))]
      [v])))

^:rct/test
(comment
  (def scm
    [:map
     [:username :string]
     [:password {:input-type :password} :string]
     [:balance {:optional true} :int]])
  (data&errors scm {:username "some" :balance "siw"}) ;=>>
  [{} {:password ::m/missing-key :balance ::m/string-transform}]
  (data&errors scm {:username "some" :password "pwd" :balance "322"}) ;=>>
  [{:balance 322}])
  

(defalias ^{:doc
            "
A component of a HTML form creating from `schema` with current `data`.
   
## Properties

  - `::data` Current data of the form.
  - `::schema` The schema for the data, support simple `:map` schema only. Each fields can have special
    properties:
    - `:optional` the field is optional.
    - `:input-type` specific the type of the input, e.g. `:password`
    - `:placeholder` value of the input.
    - `:description` a tooltip for the label
  - `::title` A function receive all `attrs` returns hiccup, can be nil.
  - `::label` A function receive a field's name and returns a string as the label for the input, default
       captalization the name of the field
  - `::button-label` The label for the submit button, default \"Submit\"

## Events
   
   - `::on-submit` A function receives coerced data and errors of raw data (`::data`), returns a truthy value
     will prevent the default event handler (refresh)"}
  form
  [{::keys [schema data on-submit label button-label] :as attrs
    :or {label #(pp/cl-format nil "~:(~a~)" (name %))
         button-label "Submit"}}
   children] 
  (let [[data errors] (data&errors schema data)
        entries (extract-entries schema)]
    [:div (merge {:class ["form"]} attrs)
     (map #(hiccup/update-attrs % assoc ::schema schema ::data data) children)
     [:form {:on {:submit (fn [evt] (when (apply on-submit (data&errors schema (form-data evt)))
                                      (.preventDefault evt)))}}
      (for [{fname :name :keys [type attributes]} entries
            :let [err (and errors (get errors fname))]]
        [:div.group
         [:label {:for fname :title (:description attributes)} (label fname)]
         [:input {:class (cond-> [] (not (:optional attributes)) (conj "required"))
                  :type (or (:input-type attributes) (input-type-of type))
                  :placeholder (:placeholder attributes)
                  :name fname
                  :value (get data fname)}]
         [:span {:class (cond-> ["error"] err (conj "show-error"))} err]])
      [:button.submit-btn {:type :submit :name :submit} button-label]]]))

