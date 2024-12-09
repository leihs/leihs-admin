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
      binding.pry

      within(".modal") do
        all("li", text: "Parent Category").last.click
        expect(page).to have_content("Child Category")

        all("li", text: "Child Category").last.hover
        click_on "Select"
      end

      click_on "Save"

      wait_until { all(".modal").empty? }
      wait_until { !page.has_content? "Add Building" }
      @building_path = current_path

      expect(page.text).to have_content name

      within("aside nav") do
        click_on("Buildings")
      end
    end
  end
end
