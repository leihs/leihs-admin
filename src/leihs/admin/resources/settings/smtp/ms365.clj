(ns leihs.admin.resources.settings.smtp.ms365
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]
   [honey.sql :refer [format] :rename {format sql-format}]
   [honey.sql.helpers :as sql]
   [leihs.admin.paths :refer [path]]
   [leihs.admin.utils.jdbc :as utils-jdbc]
   [next.jdbc.sql :refer [query] :rename {query jdbc-query}]
   [ring.util.response :as response]
   [taoensso.timbre :refer [error info]]))

(defn- get-ms365-settings [tx]
  (-> (sql/select :ms365_tenant_id :ms365_client_id :ms365_client_secret)
      (sql/from :smtp_settings)
      sql-format
      (->> (jdbc-query tx) first)))

(defn- exchange-code-for-tokens [code redirect-uri {:keys [ms365_tenant_id ms365_client_id ms365_client_secret]}]
  (let [token-url (str "https://login.microsoftonline.com/" ms365_tenant_id "/oauth2/v2.0/token")]
    (try
      (let [response (http-client/post token-url
                                       {:form-params {:client_id ms365_client_id
                                                      :client_secret ms365_client_secret
                                                      :code code
                                                      :redirect_uri redirect-uri
                                                      :grant_type "authorization_code"
                                                      :scope "https://graph.microsoft.com/Mail.Send offline_access"}
                                        :as :json})]
        (:body response))
      (catch Exception e
        (let [response-body (some-> e ex-data :body)]
          (error "Token exchange failed:" response-body)
          (throw e))))))

(defn- parse-state [state]
  (try
    (json/parse-string state true)
    (catch Exception _
      nil)))

(defn callback [{tx :tx query-params :query-params-raw settings :settings}]
  (let [code (:code query-params)
        state-str (:state query-params)
        error-param (:error query-params)
        error-desc (:error_description query-params)]
    (if error-param
      (do
        (error "MS365 OAuth error:" error-param "-" error-desc)
        (response/redirect (str (path :smtp-settings) "?tab=test-history&ms365_error=" error-param)))
      (try
        (let [state (parse-state state-str)
              email-address (:email state)
              external-base-url (:external_base_url settings)
              redirect-uri (str external-base-url (path :smtp-ms365-callback))
              ms365-settings (get-ms365-settings tx)
              tokens (exchange-code-for-tokens code redirect-uri ms365-settings)
              access-token (:access_token tokens)
              refresh-token (:refresh_token tokens)
              expires-in (:expires_in tokens)
              expires-at (java.sql.Timestamp. (+ (System/currentTimeMillis) (* expires-in 1000)))]
          (info "MS365 OAuth successful for:" email-address)
          (utils-jdbc/insert-or-update! tx :ms365_mailboxes
                                        ["id = ?" email-address]
                                        {:id email-address
                                         :access_token access-token
                                         :refresh_token refresh-token
                                         :token_expires_at expires-at})
          (response/redirect (str (path :smtp-settings) "?tab=test-history&ms365_success=true")))
        (catch Exception e
          (error "MS365 OAuth callback failed:" (.getMessage e))
          (response/redirect (str (path :smtp-settings) "?tab=test-history&ms365_error=token_exchange_failed")))))))

(comment
 (require '[ring.util.codec :as codec])

 (let [tenant-id ":tenant-id"
       client-id ":client-id"
       email ":email"
       redirect-uri "http://localhost:3220/admin/settings/smtp/ms365-callback"
       scope "https://graph.microsoft.com/Mail.Send offline_access"
       state (json/generate-string {:email email
                                    :csrf "random-secure-token-stored-in-session"})]
   (str "https://login.microsoftonline.com/" tenant-id "/oauth2/v2.0/authorize?"
        "client_id=" client-id
        "&response_type=code"
        "&redirect_uri=" (codec/url-encode redirect-uri)
        "&scope=" (codec/url-encode scope)
        "&state=" (codec/url-encode state))))
