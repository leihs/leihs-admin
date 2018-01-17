require 'pry'
require 'capybara/rspec'
require 'selenium-webdriver'

def base_url
  @base_url ||= ENV['LEIHS_HTTP_BASE_URL'].presence || 'http://localhost:3211'
end

def port
  @port ||= Addressable::URI.parse(base_url).port
end

def plain_faraday_json_client
  @plain_faraday_json_client ||= Faraday.new(
    url: base_url,
    headers: { accept: 'application/json' }) do |conn|
      conn.adapter Faraday.default_adapter
      conn.response :json, content_type: /\bjson$/
    end
end

def set_capybara_values
  Capybara.app_host = base_url
  Capybara.server_port = port
end

def set_browser(example)
  Capybara.current_driver = \
    begin
      ENV['CAPYBARA_DRIVER'].presence.try(:to_sym) \
          || example.metadata[:driver] \
          || :selenium
    rescue
      :selenium
    end
end

RSpec.configure do |config|
  Capybara.current_driver = :selenium
  set_capybara_values

  if ENV['FIREFOX_ESR_45_PATH'].present?
    Selenium::WebDriver::Firefox.path = ENV['FIREFOX_ESR_45_PATH']
  end

  config.before :all do
    set_capybara_values
  end

  config.before :each do |example|
    set_capybara_values
    set_browser example
  end
end