require "spec_helper"
require "pry"

feature "Manage inventory-pools", type: :feature do
  context " an admin and several pools " do
    before :each do
      @admin = FactoryBot.create :admin
      @pools = 10.times.map { FactoryBot.create :inventory_pool }
      sign_in_as @admin
    end

    scenario "deleting an inventory pool" do
      visit "/admin/"
      click_on "Inventory Pools"

      @pools.each { |pool| expect(page).to have_content pool.name }

      click_on @pools.first.name
      @inventory_pool_path = current_path

      click_on "Delete" # delete page
      wait_until { page.has_css? ".modal" }
      find(".modal button", text: "Delete").click
      wait_until { page.has_no_css? ".modal" }
      wait_until { current_path == "/admin/inventory-pools/" }

      @pools.drop(1).each { |pool| expect(page).to have_content pool.name }

      expect(page).not_to have_content @pools.first.name
    end
  end

  context "an inventory manager via API" do
    before :each do
      @manager = FactoryBot.create :user
      @pool = FactoryBot.create :inventory_pool
      FactoryBot.create :access_right, user: @manager,
        inventory_pool: @pool, role: "inventory_manager"
      @http_client = plain_faraday_client
      @api_token = FactoryBot.create :api_token, user_id: @manager.id
      @http_client.headers["Authorization"] = "Token #{@api_token.token_secret}"
    end

    scenario "deleting the own pool is forbidden" do
      resp = @http_client.delete "/admin/inventory-pools/#{@pool[:id]}"
      expect(resp.status).to be == 403
      expect(InventoryPool.find(id: @pool[:id])).to be
    end
  end
end
