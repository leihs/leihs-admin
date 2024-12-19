require "spec_helper"
require "pry"

feature "Manage Categories ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin

    @parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")
    category_with_models = FactoryBot.create(:model_group, name: "Category with models")

    model1 = FactoryBot.create(:leihs_model)

    FactoryBot.create(:model_link, model_group: category_with_models, leihs_model: model1, quantity: 2)

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

    scenario "deleting category with models not possible" do
      visit "/admin/"
      click_on "Categories"
      all("li", text: "Category with models").last.click

      expect(page).to have_content("Category with models")
      expect(page).not_to have_content("Delete")
    end
  end
end
