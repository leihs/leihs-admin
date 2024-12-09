require "spec_helper"
require "pry"

feature "Manage categories ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin

    @parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")

    FactoryBot.create(:model_group_link, parent: @parent_category, child: child_category)
    FactoryBot.create(:model_group_link, parent: child_category, child: nthchild_category)
    sign_in_as @admin
  end

  let(:name) { Faker::Company.name }
  let(:label) { Faker::Company.department }

  context "an admin via the UI " do
    scenario "edits a building" do
      visit "/admin/"

      within("aside nav") do
        click_on "Categories"
      end
      binding.pry

      click_on @parent_category.name
      expect(page).to have_content @parent_category.name
      @category_path = current_path

      click_on "Edit"
      expect(page).to have_content "Edit Category"
      fill_in "name", with: name

      click_on "Add parent category"
      within(".modal") do
        all("li", text: "Parent Category").last.click
        expect(page).to have_content("Child Category")

        all("li", text: "Child Category").last.hover
        click_on "Select"

        fill_in "label", with: label
      end

      click_on "Save"

      expect(page).to have_content name
      expect(page).to have_content label

      within("aside nav") do
        click_on "Buildings"
      end

      all("li", text: "Parent Category").last.click
      all("li", text: "Child Category").last.click
      expect(page).to have_content name
    end
  end
end
