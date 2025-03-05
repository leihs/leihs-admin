require "spec_helper"
require "pry"

feature "Display extended info", type: :feature do
  context " an admin, a pool, and a user " do
    before :each do
      @admin = FactoryBot.create :admin
      @pool = FactoryBot.create :inventory_pool
      @user = FactoryBot.create :user
      sign_in_as @admin
    end

    scenario "check if extened_info is visible" do
      click_on "Inventory Pools"
      click_on @pool.name
      within(".nav-tabs") { click_on "Users" }
      select "any", from: "Role"
      expect(page).to have_selector("table tbody tr", count: 2)
      click_on_first_user @user

      expect(page).to have_content(@user.firstname)
      within(".extended-info") do
        expect(page).to have_content("{\"foo\":\"bar\"}")
      end
    end
  end
end
