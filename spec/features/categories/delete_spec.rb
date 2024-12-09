require "spec_helper"
require "pry"

feature "Manage Categories ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin

    @parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")

    FactoryBot.create(:model_group_link, parent: @parent_category, child: child_category)
    FactoryBot.create(:model_group_link, parent: child_category, child: nthchild_category)
  end

  context "an admin " do
    before :each do
      sign_in_as @admin
    end

    scenario "deleting a category" do
      visit "/admin/"
      click_on "Categories"

      @buildings.each { |building| expect(page).to have_content building.name }

      click_on @buildings.first.name
      @building_path = current_path

      click_on "Delete" # delete page
      within ".modal" do
        click_on "Delete" # submit / confirm
      end

      wait_until { current_path == "/admin/buildings/" }

      @buildings.drop(1).each { |building| expect(page).to have_content building.name }

      expect(page).not_to have_content @buildings.first.name
    end
  end
end
