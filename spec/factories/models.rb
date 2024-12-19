class Model < Sequel::Model
  many_to_one :model_group
  many_to_one :model_links
end

FactoryBot.define do
  factory :model do
    type { "Model" }
    manufacturer { "Example Manufacturer" }
    product { "Example Product" }
    version { "1.0" }
    info_url { "http://example.com" }
    rental_price { 100.00 }
    maintenance_period { 12 }
    is_package { false }
    hand_over_note { "Handle with care" }
    description { "Example description" }
    internal_description { "Internal use only" }
    technical_detail { "Technical details here" }
    created_at { Time.now }
    updated_at { Time.now }
    # cover_image_id { SecureRandom.uuid }

    # If you have an association with the Image model, you can add it like this:
    # association :cover_image, factory: :image
  end
end
