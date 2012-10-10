desc "Do the full release process"
task :full_release => [:clean, :package, :deploy]

GITHUB_REPO_USER = 'NUBIC'
GITHUB_REPO_NAME = 'suite_authorization_source.rb'

task :github_credentials do |t|
  require 'highline'

  class << t; attr_accessor :login, :password; end

  hl = HighLine.new

  t.login = `git config github.user`.chomp
  unless $? == 0
    t.login = hl.ask("GitHub username: ")
  end

  t.password = hl.ask("GitHub password for #{t.login}: ") do |q|
    q.echo = '*'
  end
end

def github_downloads
  creds = task(:github_credentials)
  @github_downloads ||= Github::Repos::Downloads.new(
    :login => creds.login,
    :password => creds.password
  )
end

desc "Uploads the current artifacts to GitHub"
task :deploy => :github_credentials do
  require 'github_api'

  projs = [
    project('ctms-auth-ruby:source'),
    project('ctms-auth-ruby:jruby-dynamic-import')
  ]

  projs.each do |p|
    if !(ENV['RELEASE_DEV'] && ENV['RELEASE_DEV'] =~ /y/i) && p.version =~ /DEV$/
      fail "Can't release #{p.name} at #{p.version}; it's a dev version."
    end
  end

  projs.each do |pub_proj|
    pkg = pub_proj.packages.first
    pkg_file = pkg.to_s
    target_file_name = File.basename pkg_file

    info "Telling GitHub about #{target_file_name}"
    created = github_downloads.create(GITHUB_REPO_USER, GITHUB_REPO_NAME,
      'name' => target_file_name,
      'size' => File.size(pkg_file),
      'description' => "#{pub_proj.name} #{pub_proj.version}",
      'content_type' => 'application/x-java-archive'
    )

    info "Uploading #{target_file_name}"
    github_downloads.upload(created, pkg_file)
  end
end

namespace :deploy do
  task :delete, [:version] => [:github_credentials] do |t, args|
    require 'github_api'

    unless args[:version] =~ /\d+\.\d+\.\d+/
      fail "For safety, only delete one version at a time (not #{args[:version]})."
    end

    trace 'Looking up downloads for NUBIC/suite_authorization_source.rb'
    matches = github_downloads.list(GITHUB_REPO_USER, GITHUB_REPO_NAME).select do |dl|
      dl['description'] =~ /#{Regexp.escape args[:version]}/
    end

    if matches.empty?
      info "No downloads match #{args[:version]}"
    else
      info "Press enter to delete the following files UNRECOVERABLY:"
      matches.each do |match|
        info "  - #{match['description']}"
        info "    #{match['html_url']}"
      end
      info "or press ^C to abort."
      begin
        $stdin.gets
      rescue Interrupt
        info "\nNothing deleted."
        exit(0)
      end

      info "Deleting matches."
      matches.each do |match|
        trace "Deleting #{match.inspect}"
        github_downloads.delete(GITHUB_REPO_USER, GITHUB_REPO_NAME, match['id'])
      end
    end
  end
end
