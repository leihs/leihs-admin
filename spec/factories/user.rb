require "json"
class User < Sequel::Model
  attr_accessor :password

  many_to_many :groups, join_table: :groups_users
end

FactoryBot.modify do
  factory :user do
    extended_info do
      {foo: "bar"}.to_json
    end
  end
end
