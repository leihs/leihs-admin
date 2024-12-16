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
      all("li", text: "Parent Category").last.click
      expect(page).to have_content("Child Category")

      all("li", text: "Child Category").last.click
      expect(page).to have_content("Child Category")
      click_on "Child Category"

      expect(page).to have_content("Delete")

      click_on "Delete" # delete page
      within ".modal" do
        click_on "Delete" # submit / confirm
      end

      expect(page).to have_content("Parent Category")
      click_on "reset-tree"
      click_on "open all"

      expect(page).not_to have_content("Child Category")
    end
  end
end
