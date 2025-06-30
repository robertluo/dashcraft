(ns robertluo.dashcraft.loading
  "A general purpose loading container component"
  (:require
   [replicant.alias :refer [defalias]]
   [phosphor.icons :as icons]))

(defalias
  ^{:doc "A container component that shows a loading spinner when loading.
            
          Properties:
          
            - `::loading?` - boolean indicating if loading state should be shown
            - Children will be displayed when not loading"}
  loading-container 
  [{::keys [loading?] :as attrs} children]
  [:div (merge {:class ["loading-container"]} attrs)
   (when loading?
     [:div.loading-spinner
      (icons/render :circle-notch {:size "32px"})])
   [:div {:class ["loading-content" (when loading? "blurred")]}
    children]])

;; Ensure the circle-notch icon is loaded
(icons/load-icon! :circle-notch
                  [:svg {:xmlns "http://www.w3.org/2000/svg" :width "256" :height "256" :viewBox "0 0 256 256"}
                   [:path {:d "M232,128a104,104,0,0,1-208,0c0-41,23.81-78.36,60.66-95.27a8,8,0,0,1,6.68,14.54C60.15,61.59,40,93.27,40,128a88,88,0,0,0,176,0c0-34.73-20.15-66.41-51.34-80.73a8,8,0,0,1,6.68-14.54C208.19,49.64,232,87,232,128Z"}]])
