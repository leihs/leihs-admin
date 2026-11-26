require "spec_helper"

feature "Manage pickup locations", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin
    @pool = FactoryBot.create :inventory_pool
    @pickup_location = FactoryBot.create :pickup_location, inventory_pool_id: @pool.id
  end

  context "an admin via the UI" do
    before(:each) { sign_in_as @admin }

    scenario "cannot deactivate a pickup location referenced by active reservations" do
      visit "/admin/inventory-pools/#{@pool.id}/pickup-locations/"
      click_on @pickup_location.name

      res = FactoryBot.create(:reservation, inventory_pool: @pool, status: "approved",
        pickup_location_id: @pickup_location.id)

      click_on "Edit"
      click_on_toggle "active"
      click_on "Save"

      expect(page).to have_content(/error.*422.*active reservations/mi)

      res.delete

      visit current_path
      click_on "Edit"
      click_on_toggle "active"
      click_on "Save"

      wait_until { all(".modal").empty? }
      find("tr.active .fa-toggle-off")
    end
  end
end
