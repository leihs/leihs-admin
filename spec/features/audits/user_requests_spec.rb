require "spec_helper"
require "pry"

feature "User's audited requests" do
  before :each do
    @system_admin = FactoryBot.create :system_admin
    @user = FactoryBot.create(:user)
    sign_in_as @system_admin
    @user = FactoryBot.create :user
  end

  let(:today) { Date.today }

  def expect_changes_count(txid, start_date, expected_count)
    formatted_date = start_date.is_a?(Date) ? start_date.strftime("%Y-%m-%d") : start_date.to_s
    visit "/admin/audited/changes/"
    fill_in "txid", with: txid
    fill_in "start-date", with: formatted_date
    select "test_changes", from: "table"

    wait_until do
      all("table.audited-changes tbody tr").count == expected_count
    end

    actual_count = all("table.audited-changes tbody tr").count
    expect(actual_count).to eq(expected_count),
      "Expected #{expected_count} rows, got #{actual_count} for start_date '#{formatted_date}'"
  end

  def except_changes_counts(txid, today)
    expect_changes_count(txid, today - 14, 2)   # 2 weeks ago
    expect_changes_count(txid, today << 1, 3)   # 1 month ago
    expect_changes_count(txid, today << 12, 4)  # 1 year ago
    expect_changes_count(txid, today << 60, 5)  # 5 years ago
  end

  def create_changes_test_data(txid)
    [3.days.ago,
      10.days.ago,
      20.days.ago,
      5.months.ago,
      3.years.ago].each do |time|
      AuditedChange.create(
        txid: txid,
        tg_op: "UPDATE",
        table_name: "test_changes",
        changed: {},
        created_at: time
      )
    end
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

  scenario "filters audited changes correctly by time range" do
    visit "/admin/users/#{@user.id}"
    click_on "Edit"
    fill_in "firstname", with: "FooHans"
    click_on "Save"
    wait_until { current_path == "/admin/users/#{@user.id}" }

    visit "/admin/users/#{@system_admin.id}"
    click_on "Show Audit Requests"
    req = AuditedRequest.where(user_id: @system_admin.id).order(:created_at).last
    txid = req.txid
    create_changes_test_data(txid)

    expect_changes_count(txid, today - 7, 1)   # 1 week ago
    except_changes_counts(txid, today)
  end

  scenario "filters audited changes after init by time range" do
    visit "/admin/users/#{@user.id}"
    click_on "Edit"
    fill_in "firstname", with: "FooHans"
    click_on "Save"
    wait_until { current_path == "/admin/users/#{@user.id}" }

    visit "/admin/users/#{@system_admin.id}"
    click_on "Show Audit Requests"

    req = AuditedRequest.where(user_id: @system_admin.id).order(:created_at).last
    txid = req.txid
    create_changes_test_data(txid)
    visit "/admin/audited/changes/?table=test_changes"

    wait_until do
      all("table.audited-changes tbody tr").count == 1
    end

    except_changes_counts(txid, today)
  end
end
