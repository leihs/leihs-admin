require "spec_helper"
require "pry"

feature "User's requests" do
  before :each do
    @system_admin = FactoryBot.create :system_admin
    sign_in_as @system_admin
    @user = FactoryBot.create :user
  end

  scenario "works" do
    visit "/admin/users/#{@user.id}"
    click_on "Edit"
    fill_in "firstname", with: "FooHans"
    click_on "Save"
    wait_until { current_path == "/admin/users/#{@user.id}" }

    visit "/admin/users/#{@system_admin.id}"
    click_on "Show Audit Requests"

    req = AuditedRequest.where(user_id: @system_admin.id).order(:created_at).last

    fill_in "txid", with: req.txid
    click_on "Request"
    expect(page).to have_content req.txid

    visit "/admin/audited/changes/"
    fill_in "txid", with: req.txid
    wait_until do
      all("table.audited-changes tbody tr").count == 1
    end
    click_on "Change"
    expect(page).to have_content req.txid
  end
end
