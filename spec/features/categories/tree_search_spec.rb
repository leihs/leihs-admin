require "spec_helper"
require "pry"

feature "Interact with TreeViewer ", type: :feature do
  before :each do
    @admin = FactoryBot.create :admin
    parent_category = FactoryBot.create(:model_group, name: "Parent Category")
    child_category = FactoryBot.create(:model_group, name: "Child Category")
    nthchild_category = FactoryBot.create(:model_group, name: "Nth Category")
    FactoryBot.create(:model_group_link, parent: parent_category, child: child_category)
    FactoryBot.create(:model_group_link, parent: child_category, child: nthchild_category)

    5.times do
      child = FactoryBot.create(:model_group, name: Faker::Commerce.product_name)
      FactoryBot.create(:model_group_link, parent: parent_category, child: child)
    end

    5.times do
      parent = FactoryBot.create(:model_group, name: Faker::Commerce.product_name)
      child = FactoryBot.create(:model_group, name: Faker::Commerce.product_name)
      FactoryBot.create(:model_group_link, parent: parent, child: child)
    end
  end

  context "an admin " do
    before(:each) {
      sign_in_as @admin
    }

    scenario "is unfolding specific category" do
      visit "/admin/"
      click_on "Categories"

      expect(page).to have_content("Parent Category")

      all("li", text: "Parent Category").last.click
      expect(page).to have_content("Child Category")

      all("li", text: "Child Category").last.click
      expect(page).to have_content("Nth Category")

      within(all("div", text: "Parent Category").last) do
        expect(find("span.badge").text).to eq("6")
      end
    end

    scenario "is opening and closing all categories" do
      visit "/admin/"
      click_on "Categories"

      click_on "open all"

      within("ul.tree") do
        expect(page).to have_selector("a", count: 18)
      end

      click_on "close all"

      within("ul.tree") do
        expect(page).to have_selector("a", count: 6)
      end
    end

    scenario "is searching for category" do
      visit "/admin/"
      click_on "Categories"

      within("ul.tree") do
        expect(page).to have_selector("a", count: 6)
      end

      find('input[aria-label="Category search field"]').set("nth")

      within("ul.tree") do
        expect(page).to have_selector("a", count: 1)
      end

      find('input[aria-label="Category search field"]').send_keys(:enter)

      within("ul.tree") do
        expect(page).to have_selector("a", count: 3)
      end

      within("ul.tree") do
        expect(page).to have_selector("strong u", text: "Nth")
      end

      click_on "button-addon2"

      within("ul.tree") do
        expect(page).to have_selector("a", count: 6)
      end
    end
  end
end
