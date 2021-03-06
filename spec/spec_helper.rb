require 'active_support/all'
require 'pry'

require 'config/database'
require 'config/factories'

def base_url
  @base_url ||= ENV['LEIHS_ADMIN_HTTP_BASE_URL'].presence || 'http://localhost:3220'
end

def port
  @port ||= Addressable::URI.parse(base_url).port
end

require 'config/browser'
require 'config/http_client'

require 'helpers/global'
require 'helpers/user'

RSpec.configure do |config|

  config.include Helpers::Global
  config.include Helpers::User

  config.before :each do
    page.driver.browser.manage.window.resize_to(1200, 1200)
    srand 1
  end

  config.after(:each) do |example|
    # auto-pry after failures, except in CI!
    unless (ENV['CIDER_CI_TRIAL_ID'].present? or ENV['NOPRY_ON_EXCEPTION'].present?)
      unless example.exception.nil?
        binding.pry if example.exception
      end
    end
  end
end
