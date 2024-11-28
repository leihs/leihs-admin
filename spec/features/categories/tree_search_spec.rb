require "spec_helper"
require "pry"

feature "Manage Categories", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin
    @parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    # @child_category = FactoryBot.create(:model_group, name: "Child Category")

    # FactoryBot.create(:model_group_link, parent: parent_category, child: child_category)
  end

  context "an admin" do
    before(:each) {
      sign_in_as @admin
    }

    scenario "creating a category with a parent-child relationship" do
      binding.pry
      visit "/admin/categories"

      expect(page).to have_content("Parent Category")
      expect(page).to have_content("Child Category")

      within("#category-#{parent_category.id}") do
        expect(page).to have_content("Child Category")
      end
    end
  end
end
