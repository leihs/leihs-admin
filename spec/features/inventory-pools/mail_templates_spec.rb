require 'spec_helper'
require 'pry'

feature 'Mail templates in pool', type: :feature do

  before :each do
    @pool = FactoryBot.create :inventory_pool
    @inventory_manager = FactoryBot.create :user
    FactoryBot.create :access_right, user: @inventory_manager,
      inventory_pool: @pool, role: 'inventory_manager'
  end

  scenario 'editing a mail template works' do

    sign_in_as @inventory_manager
    visit '/admin/'
    within('aside nav') do
      click_on 'Inventory Pools'
    end
    click_on @pool.name
    within('.nav-tabs') { click_on 'Mail Templates' }

    select "approved", from: "name"
    select "de-CH", from: "language_locale"

    click_on "approved"
  end
end
