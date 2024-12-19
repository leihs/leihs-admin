require "spec_helper"
require "pry"

feature "Manage Categories ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin

    parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")

    FactoryBot.create(:model_group_link, parent: parent_category, child: child_category)
    FactoryBot.create(:model_group_link, parent: child_category, child: nthchild_category)
  end

  let(:name) { Faker::Company.name }
  let(:label) { Faker::Company.name }

  context "an admin via the UI " do
    before(:each) {
      sign_in_as @admin
    }

    scenario "creates a new category with parents" do
      visit "/admin/"
      click_on "Categories"

      click_on "Add Category"
      wait_until { page.has_css?(".modal", text: "Add a Category") }
      fill_in "name", with: name

      click_on "Add parent category"

      within(".modal") do
        all("li", text: "Parent Category").last.click
        expect(page).to have_content("Child Category")

        all("li", text: "Child Category").last.hover
        click_on "Select"
      end

      fill_in "label", with: label
      attach_file("user-image", "./spec/data/anon.jpg")
      click_on "Save"

      expect(page).to have_content("Parent Category <- Child Category <- #{name}")
      expect(page.text).to have_content name
      expect(page.text).to have_content label
      within ".category" do
        expect(page).to have_css("img[src]")
      end

      within("aside nav") do
        click_on("Categories")
      end

      find('input[aria-label="Category search field"]').set(name)
      find('input[aria-label="Category search field"]').send_keys(:enter)
      expect(page.text).to have_content label
    end
  end
end
