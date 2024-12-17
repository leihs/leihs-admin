class ModelLink < Sequel::Model
  many_to_one(:leihs_model, key: :model_id)
  many_to_one(:model_group)
end

FactoryBot.define do
  factory :model_link do
    association :model_group
    association :leihs_model
    quantity { 1 }
  end
end
