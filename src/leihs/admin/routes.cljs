(ns leihs.admin.routes
  (:refer-clojure :exclude [str keyword])
  (:require-macros
   [reagent.ratom :as ratom :refer [reaction]])
  (:require
   [leihs.admin.paths :as paths :refer [paths]]
   [leihs.admin.resources.audits.changes.change.main :as audited-change]
   [leihs.admin.resources.audits.changes.main :as audited-changes]
   [leihs.admin.resources.audits.main :as audits]
   [leihs.admin.resources.audits.requests.main :as audited-requests]
   [leihs.admin.resources.audits.requests.request.main :as audited-request]
   [leihs.admin.resources.buildings.building.main :as building]
   [leihs.admin.resources.buildings.main :as buildings]
   [leihs.admin.resources.groups.group.create :as group-create]
   ;; [leihs.admin.resources.groups.group.del :as group-delete]
   ;; [leihs.admin.resources.groups.group.edit :as group-edit]
   [leihs.admin.resources.groups.group.main :as group]
   [leihs.admin.resources.groups.group.users.main :as group-users]
   [leihs.admin.resources.groups.main :as groups]
   [leihs.admin.resources.inventory-fields.inventory-field.main :as inventory-field]
   [leihs.admin.resources.inventory-fields.main :as inventory-fields]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.edit :as delegation-edit]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.groups.main :as delegation-groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.main :as delegation]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.suspension.main :as delegation-suspension]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.delegation.users.main :as delegation-users]
   [leihs.admin.resources.inventory-pools.inventory-pool.delegations.main :as delegations]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.groups.main :as inventory-pool-entitlement-group-groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.main :as inventory-pool-entitlement-group]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.entitlement-group.users.main :as inventory-pool-entitlement-group-users]
   [leihs.admin.resources.inventory-pools.inventory-pool.entitlement-groups.main :as inventory-pool-entitlement-groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.groups.group.roles.main :as inventory-pool-group-roles]
   [leihs.admin.resources.inventory-pools.inventory-pool.groups.main :as inventory-pool-groups]
   [leihs.admin.resources.inventory-pools.inventory-pool.main :as inventory-pool]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.main :as inventory-pool-users]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.create :as inventory-pool-user-create]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.direct-roles.main :as inventory-pool-user-direct-roles]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.edit :as inventory-pool-user-edit]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.main :as inventory-pool-user]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.roles.main :as inventory-pool-user-roles]
   [leihs.admin.resources.inventory-pools.inventory-pool.users.user.suspension.main :as inventory-pool-user-suspension]
   [leihs.admin.resources.inventory-pools.main :as inventory-pools]
   [leihs.admin.resources.inventory.main :as inventory]
   [leihs.admin.resources.leihs-root :as home]
   [leihs.admin.resources.mail-templates.mail-template.main :as mail-template]
   [leihs.admin.resources.mail-templates.main :as mail-templates]
   [leihs.admin.resources.main :as admin]
   [leihs.admin.resources.rooms.main :as rooms]
   [leihs.admin.resources.rooms.room.main :as room]
   [leihs.admin.resources.settings.languages.main :as languages-settings]
   [leihs.admin.resources.settings.main :as settings]
   [leihs.admin.resources.settings.misc.main :as misc-settings]
   [leihs.admin.resources.settings.smtp.main :as smtp-settings]
   [leihs.admin.resources.settings.syssec.main :as syssec-settings]
   [leihs.admin.resources.statistics.main :as statistics]
   [leihs.admin.resources.suppliers.main :as suppliers]
   [leihs.admin.resources.suppliers.supplier.main :as supplier]
   [leihs.admin.resources.system.authentication-systems.authentication-system.groups.main :as authentication-system-groups]
   [leihs.admin.resources.system.authentication-systems.authentication-system.main :as authentication-system]
   [leihs.admin.resources.system.authentication-systems.authentication-system.users.main :as authentication-system-users]
   [leihs.admin.resources.system.authentication-systems.main :as authentication-systems]
   [leihs.admin.resources.system.main :as system]
   [leihs.admin.resources.users.choose-main :as users-choose]
   [leihs.admin.resources.users.main :as users]
   [leihs.admin.resources.users.user.main :as user]
   [leihs.admin.resources.users.user.password-reset.main :as user-password-reset]
   [leihs.core.routing.front :as routing]))

(def resolve-table
  {:admin #'admin/page
   :audited-change #'audited-change/page
   :audited-changes #'audited-changes/page
   :audited-request #'audited-request/page
   :audited-requests #'audited-requests/page
   :audits #'audits/page
   :authentication-system #'authentication-system/page
   ;; :authentication-system-create #'authentication-system/create-page
   ;; :authentication-system-delete #'authentication-system/delete-page
   ;; :authentication-system-edit #'authentication-system/edit-page
   :authentication-system-groups #'authentication-system-groups/page
   :authentication-system-users #'authentication-system-users/page
   :authentication-systems #'authentication-systems/page
   :building #'building/page
   ;; :building-create #'building/create-page
   ;; :building-delete #'building/delete-page
   ;; :building-edit #'building/edit-page
   :buildings #'buildings/page
   :inventory-field #'inventory-field/page
   ;; :inventory-field-create #'inventory-field/create-page
   ;; :inventory-field-delete #'inventory-field/delete-page
   ;; :inventory-field-edit #'inventory-field/edit-page
   :inventory-fields #'inventory-fields/page
   :group #'group/page
   ;; :group-create #'group-create/page
   ;; :group-delete #'group-delete/page
   ;; :group-edit #'group-edit/page
   :group-users #'group-users/page
   :groups #'groups/page
   :home #'home/page
   :inventory #'inventory/page
   :inventory-pool #'inventory-pool/page
   :inventory-pool-delegation #'delegation/page
   ;; :inventory-pool-delegation-create #'delegation-edit/new-page
   :inventory-pool-delegation-edit #'delegation/page
   :inventory-pool-delegation-groups #'delegation-groups/page
   :inventory-pool-delegation-suspension #'delegation-suspension/page
   :inventory-pool-delegation-users #'delegation-users/page
   :inventory-pool-delegations #'delegations/page
   :inventory-pool-entitlement-group #'inventory-pool-entitlement-group/page
   :inventory-pool-entitlement-group-groups #'inventory-pool-entitlement-group-groups/page
   :inventory-pool-entitlement-group-users inventory-pool-entitlement-group-users/page
   :inventory-pool-entitlement-groups #'inventory-pool-entitlement-groups/page
   :inventory-pool-group-roles #'inventory-pool-group-roles/page
   :inventory-pool-groups #'inventory-pool-groups/page
   :inventory-pool-user #'inventory-pool-user/page
   ;; :inventory-pool-user-create #'inventory-pool-user-create/page
   :inventory-pool-user-edit #'inventory-pool-user-edit/page
   :inventory-pool-user-direct-roles #'inventory-pool-user-direct-roles/page
   :inventory-pool-user-roles #'inventory-pool-user-roles/page
   :inventory-pool-user-suspension #'inventory-pool-user-suspension/page
   :inventory-pool-users #'inventory-pool-users/page
   :inventory-pools #'inventory-pools/page
   :languages-settings #'languages-settings/page
   :misc-settings #'misc-settings/page
   :mail-template #'mail-template/page
   ;; :mail-template-edit #'mail-template/edit-page
   :mail-templates #'mail-templates/page
   :room #'room/page
   ;; :room-create #'room/create-page
   ;; :room-delete #'room/delete-page
   ;; :room-edit #'room/edit-page
   :rooms #'rooms/page
   :settings #'settings/page
   :smtp-settings #'smtp-settings/page
   :statistics #'statistics/page
   :supplier #'supplier/page
   ;; :supplier-create #'supplier/create-page
   ;; :supplier-delete #'supplier/delete-page
   ;; :supplier-edit #'supplier/edit-page
   :suppliers #'suppliers/page
   :syssec-settings #'syssec-settings/page
   :system #'system/page
   :user #'user/page
   ;; :user-delete #'user-delete/page
   :user-password-reset #'user-password-reset/page
   :users #'users/page
   :users-choose #'users-choose/page})

(defn init []
  (routing/init paths resolve-table paths/external-handlers))
