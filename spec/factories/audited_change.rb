class AuditedChange < Sequel::Model
end

FactoryBot.define do
  factory :audited_change do
    txid { SecureRandom.uuid }
    tg_op { "UPDATE" }
    table_name { "test_changes" }
    changed { {} }
    created_at { Time.now }
  end
end
