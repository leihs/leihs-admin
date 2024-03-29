require 'spec_helper'
require 'pry'

feature 'Manage rooms', type: :feature do

  before :each do
    @admin = FactoryBot.create :admin
    @buildings = 10.times.map { FactoryBot.create(:building) }
    @building = @buildings.sample
  end

  let(:name) { Faker::Company.name}
  let(:description) { Faker::Markdown.sandwich }

  context 'an admin via the UI' do

    before(:each){ sign_in_as @admin }

    scenario ' creates a new room ' do

      visit '/admin/'
      click_on 'Rooms'
      expect(all("a, button", text: 'Create')).not_to be_empty
      click_on 'Create'
      fill_in 'name', with: name
      fill_in 'description', with: description
      select(@building.name, from: 'Building')
      click_on 'Create'
      wait_until { all(".modal").empty? }
      wait_until { not page.has_content? "Create Room" }
      @room_path = current_path
      @inventory_pool_id = current_path.match(/.*\/([^\/]+)/)[1]
      input_values = all("input").map(&:value).join(" ")
      input_values += all("select").map(&:value).join(" ")
      expect(page.text + input_values).to have_content name
      expect(page.text + input_values).to have_content description
      expect(page.text + input_values).to have_content @building.name

      # The inventory pools path includes the newly created inventory pool and
      # we can get to it via clicking its name
      within find(".nav-component nav", match: :first) do
        click_on "Rooms"
      end
      wait_until { current_path == "/admin/rooms/" }
      wait_until { page.has_content? name }
      click_on name
      wait_until { current_path == @room_path }

    end

  end

end
