(ns robertluo.dashcraft.error-aware
  "A container component that displays error messages on top of content"
  (:require
   [replicant.alias :refer [defalias]]
   [phosphor.icons :as icons]))

(defalias
  ^{:doc "A container component that shows an error overlay when an error occurs.
            
          Properties:
            - `::error` - error message string to display (nil/false means no error)
            - `::on-dismiss` - callback function called when user clicks OK button
            - Children will be displayed underneath the error overlay"}
  error-aware-container 
  [{::keys [error on-dismiss] :as attrs} children]
  [:div (merge {:class ["error-aware-container"]} (dissoc attrs ::error ::on-dismiss))
   (when error
     [:div.error-overlay
      [:div.error-modal
       [:div.error-icon
        (icons/render :warning-circle {:size "24px"})]
       [:div.error-message error]
       [:button.error-dismiss-btn
        {:on {:click (fn [_] (when on-dismiss (on-dismiss error)))}}
        "OK"]]])
   [:div.error-content
    children]])

;; Ensure the warning-circle icon is loaded
(icons/load-icon! :warning-circle
  [:svg {:xmlns "http://www.w3.org/2000/svg" :width "256" :height "256" :viewBox "0 0 256 256"}
   [:path {:d "M128,24A104,104,0,1,0,232,128,104.11,104.11,0,0,0,128,24Zm0,192a88,88,0,1,1,88-88A88.1,88.1,0,0,1,128,216Zm-8-80V80a8,8,0,0,1,16,0v56a8,8,0,0,1-16,0Zm20,36a12,12,0,1,1-12-12A12,12,0,0,1,140,172Z"}]])
