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
    click_on "Edit"
    body = Faker::Lorem.paragraph
    fill_in "body", with: body
    click_on "Save"
    expect(page).to have_content body
  end

  scenario 'permissions validation works' do
    other_pool = FactoryBot.create :inventory_pool
    sign_in_as @inventory_manager
    visit "/admin/inventory-pools/#{other_pool.id}/mail-templates/"
    expect(page).to have_content "Not authorized"
  end

end
