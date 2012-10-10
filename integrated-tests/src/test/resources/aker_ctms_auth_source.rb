start = Time.now
$stdout.puts "%6.2f | Loading #{__FILE__}" % (Time.now - start)

require 'rubygems'
$stdout.puts "%6.2f | Loaded rubygems." % (Time.now - start)

gemjar = Dir[File.expand_path('../../../../../integrated-test-gems/target/*.jar', __FILE__)].first
fail "gemjar not found" unless gemjar
require gemjar
$stdout.puts "%6.2f | Loaded gemjar #{gemjar.inspect}" % (Time.now - start)

require 'aker'
require File.expand_path('../aker_patches.rb', __FILE__)

class AkerCtmsAuthSource
  attr_reader :aker_config

  ##
  # Expects to receive an Aker configuration with a single static
  # authority configured.
  def initialize(aker_config)
    @aker_config = aker_config
  end

  def get_user_by_username(username, level)
    user_hash(
      aker_config.composite_authority.find_user(username)
    )
  end

  def get_user_by_id(id, level)
    user_hash(
      aker_config.authorities.first.users.values.find { |u| u.identifiers[:ctms_id] == id }
    )
  end

  def get_users_by_role(role_name)
    fail "An exercise for the reader"
  end

  def search_users(criteria)
    fail "An exercise for the reader"
  end

  private

  def user_hash(aker_user)
    return nil unless aker_user
    {
      :username => aker_user.username,
      :id => aker_user.identifiers[:ctms_id],
      :email_address => aker_user.email,
      :first_name => aker_user.first_name,
      :last_name => aker_user.last_name,
      :roles => { }
    }
  end
end
$stdout.puts "%6.2f | Defined #{AkerCtmsAuthSource}" % (Time.now - start)

$suite_authorization_source = AkerCtmsAuthSource.new(
  Aker::Configuration.new do
    authority Aker::Authorities::Static.from_file(File.expand_path('../users.yml', __FILE__))
  end
)
$stdout.puts "%6.2f | Registered $suite_authorization_source" % (Time.now - start)
