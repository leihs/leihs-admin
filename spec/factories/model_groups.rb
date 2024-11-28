class ModelGroup < Sequel::Model
  one_to_many :child_links, class: "ModelGroupLink", key: :parent_id
  many_to_many :children, class: "ModelGroup", join_table: :model_group_links, left_key: :parent_id, right_key: :child_id

  one_to_many :parent_links, class: "ModelGroupLink", key: :child_id
  many_to_one :parent, class: "ModelGroup", key: :parent_id
end

FactoryBot.define do
  factory :model_group do
    type { "Category" }
    name { "Default Category Name" }

    created_at { DateTime.now }
    updated_at { DateTime.now }
  end
end
