require "spec_helper"
require "pry"
require "securerandom"

feature "SMTP-Emails" do
  context "a system_admin and some emails exist" do
    before :each do
      @system_admin = FactoryBot.create :system_admin
      @recipient = FactoryBot.create :user

      @reminder_email_id = SecureRandom.uuid
      @failed_email_id = SecureRandom.uuid
      @pending_email_id = SecureRandom.uuid

      database[:emails].insert(
        id: @reminder_email_id,
        user_id: @recipient[:id],
        subject: "Your loan is due soon",
        body: "Please return your items.",
        from_address: "noreply@example.com",
        to_address: @recipient[:email],
        template: "reminder",
        is_successful: true,
        trials: 1
      )

      database[:emails].insert(
        id: @failed_email_id,
        user_id: @recipient[:id],
        subject: "Deadline reminder",
        body: "Reminder body.",
        from_address: "noreply@example.com",
        to_address: @recipient[:email],
        template: "deadline_soon_reminder",
        is_successful: false,
        error_message: "SMTP connection timeout",
        trials: 3
      )

      database[:emails].insert(
        id: @pending_email_id,
        user_id: @recipient[:id],
        subject: "Welcome",
        body: "Welcome body.",
        from_address: "noreply@example.com",
        to_address: @recipient[:email],
        template: "welcome",
        trials: 0
      )
    end

    context "via the API" do
      before :each do
        @http_client = plain_faraday_client
        @api_token = FactoryBot.create :system_admin_api_token,
          user_id: @system_admin.id
        @http_client.headers["Authorization"] = "Token #{@api_token.token_secret}"
        @http_client.headers["Content-Type"] = "application/json"
      end

      scenario "lists all emails" do
        response = @http_client.get "/admin/settings/smtp/emails"
        expect(response).to be_success
        expect(response.body["total"]).to eq 3
        expect(response.body["emails"].size).to eq 3
      end

      scenario "filters by search term across from/to/subject/error" do
        response = @http_client.get "/admin/settings/smtp/emails?term=timeout"
        expect(response).to be_success
        expect(response.body["total"]).to eq 1
        expect(response.body["emails"].first["id"]).to eq @failed_email_id
      end

      scenario "filters by template" do
        response = @http_client.get "/admin/settings/smtp/emails?template=reminder"
        expect(response).to be_success
        expect(response.body["total"]).to eq 1
        expect(response.body["emails"].first["id"]).to eq @reminder_email_id
      end

      scenario "filters by state=failure" do
        response = @http_client.get "/admin/settings/smtp/emails?state=failure"
        expect(response).to be_success
        expect(response.body["total"]).to eq 1
        expect(response.body["emails"].first["id"]).to eq @failed_email_id
      end

      scenario "filters by state=success" do
        response = @http_client.get "/admin/settings/smtp/emails?state=success"
        expect(response).to be_success
        expect(response.body["total"]).to eq 1
        expect(response.body["emails"].first["id"]).to eq @reminder_email_id
      end

      scenario "shows a single email" do
        response = @http_client.get "/admin/settings/smtp/emails/#{@failed_email_id}"
        expect(response).to be_success
        expect(response.body["subject"]).to eq "Deadline reminder"
        expect(response.body["error_message"]).to eq "SMTP connection timeout"
      end

      scenario "returns 404 for an unknown email id" do
        response = @http_client.get "/admin/settings/smtp/emails/#{SecureRandom.uuid}"
        expect(response.status).to eq 404
      end
    end

    context "via the UI" do
      before(:each) { sign_in_as @system_admin }

      scenario "lists trimmed columns, filters, and links to the show page" do
        within "aside nav" do
          click_on "Settings"
          click_on "Email"
        end
        click_on "Test & History"

        wait_until { all("table.emails tbody tr").count == 3 }

        within "table.emails thead" do
          expect(page).to have_content "From"
          expect(page).to have_content "To"
          expect(page).to have_content "Template"
          expect(page).to have_content "Status"
          expect(page).to have_content "Created"
          expect(page).not_to have_content "Subject"
          expect(page).not_to have_content "Attempts"
        end

        fill_in "Search", with: "timeout"
        wait_until { all("table.emails tbody tr").count == 1 }
        within "table.emails tbody" do
          expect(page).to have_content @recipient[:email]
        end

        fill_in "Search", with: ""
        wait_until { all("table.emails tbody tr").count == 3 }

        select "reminder", from: "Template"
        wait_until { all("table.emails tbody tr").count == 1 }

        select "(any)", from: "Template"
        wait_until { all("table.emails tbody tr").count == 3 }

        select "Failure", from: "State"
        wait_until { all("table.emails tbody tr").count == 1 }

        within "table.emails tbody tr" do
          click_on "1"
        end

        wait_until { page.has_content? "Deadline reminder" }
        expect(page).to have_content "SMTP connection timeout"
        expect(page).to have_content "Target User Id"
        expect(page).to have_content "Target Pool Id"

        click_on "← Back to Email History"
        wait_until { page.has_content? "Email History" }
      end
    end
  end
end
