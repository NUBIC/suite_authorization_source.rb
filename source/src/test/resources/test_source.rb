require 'date'

class TestSource
  USERS = {
    :alice => {
      :id => 4,
      :username => 'alice',
      :first_name => 'Alice',
      :last_name => 'Anderson',
      :email_address => 'alice@example.org',
      :account_end_date => Date.new(2050, 5, 24),
      :roles => {
        # unscoped true
        :system_administrator => true,

        # site-scoped true
        :user_administrator => true,
        # site-scoped specific sites
        :person_and_organization_information_manager => { :sites => %w(IL036 MN702) },
        # site-scoped all sites in hash
        :study_creator => { :sites => true },
        # site-scoped with unnecessary studies
        :study_qa_manager => { :sites => %w(TN423), :studies => true },

        # site+study true
        :data_reader => true,
        # site+study all sites, specific studies
        :data_analyst => { :sites => true, :studies => %w(A B G) },
        # site+study specific sites, all studies
        :registrar => { :sites => %w(CA504 LA000), :studies => true },
        # site+study specific sites, specific studies
        :study_calendar_template_builder => { :sites => %w(KA333), :studies => %w(B L) }
      }
    }
  }

  def get_user_by_username(username, level)
    USERS[username.to_sym]
  end
end

$suite_authorization_source = TestSource.new
