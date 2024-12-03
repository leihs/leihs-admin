class ModelGroupLink < Sequel::Model
  plugin :timestamps, update_on_create: true

  many_to_one :parent, class: "ModelGroup", key: :parent_id
  many_to_one :child, class: "ModelGroup", key: :child_id
end

FactoryBot.define do
  factory :model_group_link do
    parent { association :model_group }
    child { association :model_group }
  end
end
