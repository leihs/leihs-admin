require 'spec_helper'
require 'pry'

feature 'Breadcrumbs ', type: :feature do
  let(:name) { Faker::Company.name}
  let(:description) { Faker::Markdown.sandwich }
  let(:shortname) { Faker::Name.initials }
  let(:email) { Faker::Internet.email }

  context 'an admin and several pools ' do

    def create_delegation(pool)
      delegation = FactoryBot.create(:delegation)
      FactoryBot.create(:direct_access_right,
                        inventory_pool_id: pool.id,
                        user_id: delegation.id,
                        role: 'customer')
      FactoryBot.create(:direct_access_right,
                        inventory_pool_id: pool.id,
                        user_id: delegation.responsible_user.id,
                        role: 'customer')
      delegation
    end

    before :each do
      @admin = FactoryBot.create :system_admin
      @pools = 1.times.map { FactoryBot.create :inventory_pool }
      @users = 1.times.map { FactoryBot.create :user }
      @user = @users.sample
      @groups = 1.times.map{ FactoryBot.create :group}
      @group = @groups.sample
      @pool = @pools.sample
      @entitlement_group = FactoryBot.create :entitlement_group, inventory_pool_id: @pool.id
      @delegations = 1.times.map { create_delegation(@pool) }
      @delegation = @delegations.sample
    end

    context "an system admin via the UI" do
      before(:each){ sign_in_as @admin }

      scenario 'navigates to a user and back via breadcrumbs ' do
        visit '/admin/'

        within("aside nav") do
          click_on "Inventory Pools"
        end

        click_on(@pool.name)

        breadcrumbs = find("ol")
        breadcrumbs.should have_content("Inventory Pools")
        breadcrumbs.should have_content(@pool.name)
        count = page.all('ol > li').count
        count.should eq(2)

        within(".nav-tabs") do
          click_on "Users"
        end

        count = page.all('ol > li').count
        count.should eq(2)
        within("table") do
          first('tr td.user').click
        end

        count = page.all('ol > li').count
        count.should eq(3)
        name = within("article header") do first("h1").text end
        breadcrumbs.should have_content(name)

        within("ol.breadcrumb") do
          click_on(@pool.name)
        end

        count = page.all('ol > li').count
        count.should eq(2)
        within("ol.breadcrumb") do
          click_on("Inventory Pools")
        end

        page.should_not have_content(breadcrumbs)
      end

      scenario 'navigates to a group and back via breadcrumbs ' do
        visit '/admin/'

        within("aside nav") do
          click_on "Inventory Pools"
        end

        click_on(@pool.name)

        breadcrumbs = find("ol")
        breadcrumbs.should have_content("Inventory Pools")
        breadcrumbs.should have_content(@pool.name)
        count = page.all('ol > li').count
        count.should eq(2)

        within(".nav-tabs") do
          click_on "Groups"
        end

        count = page.all('ol > li').count
        count.should eq(2)

        select "(any role or none)", from: "role"
        click_on(@group.name)

        breadcrumbs.should have_content(@group.name)
        count = page.all('ol > li').count
        count.should eq(3)

        within(".nav-tabs") do
          click_on "Users"
        end

        count = page.all('ol > li').count
        count.should eq(3)

        select "members and non-members", from: "Membership"
        find("li", text: @user.email).click

        breadcrumbs.should have_content(@user.firstname + " " + @user.lastname)
        count = page.all('ol > li').count
        count.should eq(4)

        within("ol.breadcrumb") do
          click_on(@group.name)
        end

        count = page.all('ol > li').count
        count.should eq(3)

        within("ol.breadcrumb") do
          click_on(@pool.name)
        end

        count = page.all('ol > li').count
        count.should eq(2)

        within("ol.breadcrumb") do
          click_on("Inventory Pools")
        end

        page.should_not have_content(breadcrumbs)
      end


      scenario 'navigates to a delegation and back via breadcrumbs ' do
        visit '/admin/'

        within("aside nav") do
          click_on "Inventory Pools"
        end

        click_on(@pool.name)                                  
                                                              
        breadcrumbs = find("ol")                              
                                                              
        expect(breadcrumbs).to have_content("Inventory Pools")
        expect(breadcrumbs).to have_content(@pool.name)       
        count = page.all('ol > li').count                     
        expect(count).to eq(2)                                
                                                              
        within(".nav-tabs") do                                
          click_on "Delegations"                              
        end                                                   
                                                              
        count = page.all('ol > li').count                     
        expect(count).to eq(2)                                
        find(".name.text-left").click

        expect(breadcrumbs).to have_content(@delegation.firstname)       
        count = page.all('ol > li').count                     
        expect(count).to eq(3)

        within(".nav-tabs") do
          click_on "Users"
        end
        
        count = page.all('ol > li').count                     
        expect(count).to eq(3)
        
        find("td.user").click
        count = page.all('ol > li').count                     
        expect(count).to eq(4)

        name = within("article header") do first("h1").text end
        breadcrumbs.should have_content(name)

        within("ol.breadcrumb") do
          click_on(@delegation.firstname)
        end

        count = page.all('ol > li').count
        count.should eq(3)


        within("ol.breadcrumb") do
          click_on(@pool.name)
        end

        count = page.all('ol > li').count
        count.should eq(2)

        within("ol.breadcrumb") do
          click_on("Inventory Pools")
        end

        page.should_not have_content(breadcrumbs)
      end


      scenario 'navigates to a entitlement-group and back via breadcrumbs ' do
        visit '/admin/'

        within("aside nav") do
          click_on "Inventory Pools"
        end
        click_on(@pool.name)                                  
        breadcrumbs = find("ol")                              

        expect(breadcrumbs).to have_content("Inventory Pools")
        expect(breadcrumbs).to have_content(@pool.name)       
        count = page.all('ol > li').count                     
        expect(count).to eq(2)                                
                                                              
        within(".nav-tabs") do                                
          click_on "Entitlement-Groups"
        end

        count = page.all('ol > li').count                     
        expect(count).to eq(2)                                
        click_on @entitlement_group.name

        expect(breadcrumbs).to have_content(@entitlement_group.name)       
        count = page.all('ol > li').count                     
        expect(count).to eq(3)

        within(".nav-tabs") do
          click_on "Users"
        end
        
        count = page.all('ol > li').count                     
        expect(count).to eq(3)

        select "members and non-members", from: "Membership"
        
        first("td.user").click
        count = page.all('ol > li').count                     
        expect(count).to eq(4)

        name = within("article header") do first("h1").text end
        breadcrumbs.should have_content(name)

        within("ol.breadcrumb") do
          click_on(@entitlement_group.name)
        end

        count = page.all('ol > li').count
        count.should eq(3)

        within("ol.breadcrumb") do
          click_on(@pool.name)
        end

        count = page.all('ol > li').count
        count.should eq(2)

        within("ol.breadcrumb") do
          click_on("Inventory Pools")
        end

        page.should_not have_content(breadcrumbs)
      end
    end
  end
end
