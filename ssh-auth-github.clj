#! /usr/bin/env bb

(require '[babashka.curl :as curl])
(require '[cheshire.core :as json])

(defn read-config []
  (read-string (slurp "config.edn")))

(defn query [organization team]
  (format "{organization(login: \"%s\") {
                  team(slug: \"%s\") {
                    members {
                      nodes {
                        login
                        publicKeys(first: 100) {
                          nodes {
                            key
                          }
                        }
                      }
                    }
                  }
                }
              }"
          organization team))


(defn retrieve-keys [config]
  (let [resp (curl/post "https://api.github.com/graphql"
                        {:body (json/generate-string {"query" (query (:organization config)
                                                                     (:team config))})
                         :throw false
                         :headers {"Authorization" (format "bearer %s" (:token config))}})
        body (-> resp
                 (:body)
                 (json/parse-string true))]

    (cond
      (not= 200 (:status resp))
      (do (clojure.pprint/pprint body)
          (System/exit 1))

      (:errors body)
      (do
        (clojure.pprint/pprint (:errors body))
        (System/exit 1))

      :else
      body)))


(defn parse-response [data]
  (when-not (seq (get-in data [:data :organization :team]))
    (println "Error: Team is empty!")
    (System/exit 1))
  (->> (get-in data [:data :organization :team :members :nodes])
       (mapcat (fn [n] (map #(str (:key %) " " (:login n))
                            (get-in n [:publicKeys :nodes]))))))

(doseq [l (->> (read-config)
               (retrieve-keys)
               (parse-response))]
  (println l))
