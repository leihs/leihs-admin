class Email < Sequel::Model
end

FactoryBot.define do
  factory :email do
    user_id { create(:user).id }
    from_address { Faker::Internet.email }
    to_address { Faker::Internet.email }
    subject { "Test email" }
    body { "Test body" }
    trials { 0 }

    trait :succeeded do
      trials { 1 }
      is_successful { true }
    end

    trait :failed do
      trials { 1 }
      is_successful { false }
      error_message { "SMTP_DISABLED: Message not sent because of disabled SMTP setting." }
    end
  end
end
