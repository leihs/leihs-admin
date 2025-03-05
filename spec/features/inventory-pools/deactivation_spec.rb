require "spec_helper"
require "pry"

feature "Manage inventory-pools", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin
  end

  let(:name) { Faker::Company.name }
  let(:description) { Faker::Markdown.sandwich }
  let(:shortname) { Faker::Name.initials }
  let(:email) { Faker::Internet.email }

  context "an admin via the UI" do
    before(:each) { sign_in_as @admin }

    scenario "creates, deactivates, activates a pool " do
      visit "/admin/"

      within("aside nav") do
        click_on "Inventory Pools"
      end

      expect(all("a, button", text: "Add Inventory Pool")).not_to be_empty
      first("button", text: "Add Inventory Pool").click

      click_on_toggle "is_active"
      fill_in "name", with: name
      fill_in "description", with: description
      fill_in "shortname", with: shortname
      fill_in "email", with: email
      click_on "Save"
      wait_until { all(".modal").empty? }
      wait_until { !page.has_content? "Add Inventory Pool" }
      @inventory_pool_path = current_path
      @inventory_pool_id = current_path.match(/.*\/([^\/]+)/)[1]

      find("tr.active .fa-toggle-off")
      expect(page.text).to have_content name
      expect(page.text).to have_content shortname
      expect(page.text).to have_content email
      expect(page.text).to have_content description

      # The inventory pools path includes the newly created inventory pool and
      # we can get to it via clicking its name

      within("aside nav") do
        click_on "Inventory Pools"
      end

      wait_until { current_path == "/admin/inventory-pools/" }
      wait_until { page.has_content? name }
      click_on name
      wait_until { current_path == @inventory_pool_path }

      click_on "Edit"
      click_on_toggle "is_active"
      click_on "Save"
      find("tr.active .fa-toggle-on")

      pool = InventoryPool.find(name: name)
      res = FactoryBot.create(:reservation, inventory_pool: pool, status: "approved")
      item = FactoryBot.create(:item, inventory_pool: pool)

      click_on "Edit"
      click_on_toggle "is_active"
      click_on "Save"

      expect(page).to have_content(/error.*422.*has unretired items/mi)

      item.update(retired: Date.today, retired_reason: "deactivated")

      visit current_path
      click_on "Edit"
      click_on_toggle "is_active"
      click_on "Save"

      expect(page).to have_content(/error.*422.*has active reservations/mi)

      res.delete

      visit current_path
      click_on "Edit"
      click_on_toggle "is_active"
      click_on "Save"

      wait_until { all(".modal").empty? }
      wait_until { !page.has_content? "Add Inventory Pool" }
      @inventory_pool_path = current_path
      @inventory_pool_id = current_path.match(/.*\/([^\/]+)/)[1]

      find("tr.active .fa-toggle-off")
    end
  end
end
