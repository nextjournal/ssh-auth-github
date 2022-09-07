#! /usr/bin/env bb

(require '[babashka.curl :as curl])
(require '[cheshire.core :as json])
(require '[clojure.tools.cli :as tools.cli])
(require '[clojure.string :as str])
(require '[babashka.fs :as fs])

(defn print-usage [summary]
  (println
   (->> ["Usage: " "ssh-auth-github.clj [options]"
        ""
        "Options:"
        summary]
        (str/join \newline))))

(defn read-config []
  (let [{:keys [options _arguments errors summary] :as _cli-options}
        (tools.cli/parse-opts
         *command-line-args*
         [["-h" "--help"]
          ["-t" "--token TOKEN" "Github token"]
          ["-o" "--organization ORGANIZATION" "Github organization"]
          ["-e" "--team TEAM" "Github team"]
          ["-c" "--config PATH" "Path to configuration file"]])]

    (when (:help options)
      (print-usage summary)
      (System/exit 1))

    (when errors
      (println errors)
      (print-usage summary)
      (System/exit 1))

    (cond-> {}
      (and (:config options)
           (fs/exists? (:config options)))
      (merge
       (read-string (slurp (:config options))))

      (fs/exists? "config.edn")
      (merge
       (read-string (slurp "config.edn")))

      true (merge (dissoc options :config)))))

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
                            {:body    (json/generate-string {"query" (query (:organization config)
                                                                            (:team config))})
                             :throw   false
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
