require 'date'

class TestSource
  USERS = [
    {
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
    },
    {
      :id => 9,
      :username => 'betty',
      :first_name => 'Betty',
      :last_name => 'Brierson',
      :email_address => 'betty@example.org',
      :roles => {
        :user_administrator => { :sites => %w(IL036) }
      }
    },
    {
      :id => 16,
      :username => 'cat',
      :first_name => 'Catherine',
      :last_name => 'Chen',
      :email_address => 'cat@example.org',
      :roles => {
        :study_creator => true,
        :study_subject_calendar_manager => true
      }
    }
  ]

  def get_user_by_username(username, level)
    USERS.find { |u| u[:username] == username.to_s }
  end

  def get_user_by_id(id, level)
    USERS.find { |u| u[:id] == id }
  end

  def get_users_by_role(role_name)
    USERS.select { |u| u[:roles].has_key?(role_name.to_sym) }
  end

  def search_users(criteria)
    appliable_criteria = [:username, :first_name, :last_name].
      collect { |k| [k, criteria[:"#{k}_substring"]] }.
      select { |attr, criterion| criterion }.
      collect { |attr, criterion| [attr, Regexp.new(criterion, Regexp::IGNORECASE)] }
    if appliable_criteria.empty?
      USERS
    else
      appliable_criteria.collect { |attr, re| USERS.select { |u| re =~ u[attr] } }.flatten.uniq
    end
  end
end

$suite_authorization_source = TestSource.new
