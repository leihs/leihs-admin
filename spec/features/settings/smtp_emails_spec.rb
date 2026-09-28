require "spec_helper"

feature "SMTP Email History" do
  before :each do
    @system_admin = FactoryBot.create :system_admin
    sign_in_as @system_admin
  end

  scenario "shows Sent/Failed status and error message correctly" do
    FactoryBot.create :email, :succeeded,
      from_address: "sender@example.com", to_address: "sent-recipient@example.com"
    failed_email = FactoryBot.create :email, :failed,
      from_address: "sender@example.com", to_address: "failed-recipient@example.com"

    visit "/admin/settings/smtp/?tab=test-history"

    wait_until { page.has_content? "Email History" }

    within find("tr", text: "sent-recipient@example.com") do
      expect(page).to have_content "Sent"
      expect(page).not_to have_content "Failed"
    end

    within find("tr", text: "failed-recipient@example.com") do
      expect(page).to have_content "Failed"
      expect(page).to have_content failed_email.error_message
    end
  end
end
