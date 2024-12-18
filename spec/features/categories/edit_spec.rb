require "spec_helper"
require "pry"

feature "Manage categories ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin

    @parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")
    @another_category = FactoryBot.create(:model_group, name: "Another Category")

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

      click_on @another_category.name
      expect(page).to have_content @another_category.name
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

      attach_file("user-image", "./spec/data/anon.jpg")
      click_on "Save"

      expect(page).to have_content("Parent Category <- Child Category <- #{name}")
      expect(page).to have_content name
      expect(page).to have_content label
      within ".category" do
        expect(page).to have_css("img[src]")
      end

      within("aside nav") do
        click_on "Categories"
      end

      click_on "reset-tree"
      expect(page).not_to have_content label
      expect(page).not_to have_content @another_category.name

      find('input[aria-label="Category search field"]').set(name)
      find('input[aria-label="Category search field"]').send_keys(:enter)

      all("li", text: "Parent Category").last.click
      all("li", text: "Child Category").last.click
      expect(page).to have_content label
    end
  end
end
