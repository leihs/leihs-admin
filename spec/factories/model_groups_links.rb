class ModelGroupLink < Sequel::Model
  many_to_one :parent, class: "ModelGroup", key: :parent_id
  many_to_one :child, class: "ModelGroup", key: :child_id
end

FactoryBot.define do
  factory :model_group_link do
    association :parent, factory: :model_group
    association :child, factory: :model_group
    label { "Default Label" }

    created_at { DateTime.now }
    updated_at { DateTime.now }
  end
end
