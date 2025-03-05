require "spec_helper"
require "pry"

feature "Manage inventory-fields", type: :feature do
  before :each do
    @admin = FactoryBot.create(:admin, password: "password")
    @fields = Field.all
  end

  let(:label) { Faker::Lorem.words(number: 2).join(" ") }

  context "an admin via the UI" do
    before(:each) { sign_in_as @admin }

    scenario "edits a required core field" do
      @required_core_field = @fields.detect do |f|
        !f.dynamic && f.data["required"]
      end

      visit "/admin/"
      click_on "Fields"
      click_on @required_core_field.id
      @field_path = current_path

      check_values_and_defaults(@required_core_field)

      click_on "Edit"
      fill_in "Label", with: label
      expect(find("input#active")).to be_disabled
      click_on "Save"
      wait_until { all(".modal").empty? }
      wait_until { current_path == @field_path }
      wait_until { all(".wait-component").empty? }

      input_values = all("input").map(&:value).join(" ")
      expect(page.text + input_values).to have_content label

      within find("aside nav", match: :first) do
        click_on "Fields"
      end
      wait_until { current_path == "/admin/inventory-fields/" }
      expect(page).to have_content label

      click_on @required_core_field.id
      check_values_and_defaults(@required_core_field.reload)
    end

    scenario "edits a not required active core field" do
      @not_required_active_core_field = @fields.detect do |f|
        !f.dynamic && !f.data["required"] && f.active
      end

      visit "/admin/"
      click_on "Fields"
      click_on @not_required_active_core_field.id
      @field_path = current_path

      check_values_and_defaults(@not_required_active_core_field)

      click_on "Edit"
      fill_in "Label", with: label
      find("input#active").click
      click_on "Save"
      wait_until { all(".modal").empty? }
      wait_until { current_path == @field_path }
      wait_until { all(".wait-component").empty? }

      input_values = all("input").map(&:value).join(" ")
      expect(page.text + input_values).to have_content label
      expect(find("tr.active")).to have_content "false"
      expect(page).to have_content label

      check_values_and_defaults(@not_required_active_core_field.reload)
    end

    scenario "edits a dynamic field" do
      @dynamic_field = @fields.detect { |f| f.id == "properties_mac_address" }

      visit "/admin/"
      click_on "Fields"
      click_on @dynamic_field.id
      @field_path = current_path

      check_values_and_defaults(@dynamic_field)

      click_on "Edit"

      find("input#active").click
      fill_in "Label", with: label
      find(:xpath, "//input[@id='data:forPackage']").click
      find(:xpath, "//input[@id='data:permissions:owner']").click
      select("inventory_manager", from: "Minimum role required for view")
      choose("Inventory")
      select("License", from: "Target")
      select("Select", from: "Type")
      click_on("+")

      label_1 = Faker::Lorem.word
      value_1 = label_1.downcase
      label_2 = Faker::Lorem.word
      value_2 = label_2.downcase

      find(".form-group", text: "data:type").all(".col-5 input")[0].set label_1
      find(".form-group", text: "data:type").all(".col-4 input")[0].set value_1
      find(".form-group", text: "data:type").all(".col-5 input")[1].set label_2
      find(".form-group", text: "data:type").all(".col-4 input")[1].set value_2

      find(".form-group", text: "data:type").all(".row input[type='radio']")[1].click

      click_on "Save"

      wait_until { all(".modal").empty? }
      wait_until { current_path == @field_path }
      wait_until { all(".wait-component").empty? }

      click_on "Edit"

      expect(find("input#active")).not_to be_checked
      expect(find(:xpath, "//input[@id='data:label']").value).to eq label
      expect(find(:xpath, "//input[@id='data:forPackage']")).to be_checked
      expect(find(:xpath, "//input[@id='data:permissions:owner']")).not_to be_checked
      expect(find(:xpath, "//select[@id='data:permissions:role']").value).to eq "inventory_manager"
      expect(find("#Inventory")).to be_checked
      expect(find(:xpath, "//select[@id='data:target_type']").value).to eq "license"
      expect(find(:xpath, "//select[@id='data:type']").value).to eq "select"

      expect(
        find(".form-group", text: "data:type").all(".row input[type='radio']").map(&:checked?)
      ).to eq [false, true]

      click_on "Save"
      wait_until { all(".modal").empty? }

      expect(page).to have_content label

      check_values_and_defaults(@dynamic_field.reload)
    end
  end

  def check_values_and_defaults(field)
    within "tbody" do
      expect(all("tr.active td")[1].text).to eq field.active.to_s
      expect(all("tr.label td")[1].text).to eq field.data["label"]
      expect(all("tr.dynamic td")[1].text).to eq field.dynamic.to_s
      expect(all("tr.required td")[1].text).to eq ( field.data["required"] || false).to_s
      expect(all("tr.attribute td")[1].text).to eq field.data["attribute"].to_s.delete(",")
      expect(all("tr.forPackage td")[1].text).to eq ( field.data["forPackage"] || false).to_s
      expect(all("tr.owner td")[1].text).to eq ( field.data["permissions"]["owner"] || false).to_s
      expect(all("tr.role td")[1].text).to eq(field.data["permissions"]["role"] || "inventory_manager")
      expect(all("tr.field-group td")[1].text).to eq(field.data["group"] || "None")
      expect(all("tr.target-type td")[1].text).to eq(field.data["target_type"] || "License+Item")
      expect(all("tr.type td")[1].text).to eq field.data["type"]
    end
  end
end
