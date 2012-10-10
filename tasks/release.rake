desc "Do the full release process"
task :release => [:clean, :package, :deploy]

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

  creds = task(:github_credentials)
  github_downloads = Github::Repos::Downloads.new(
    :login => creds.login,
    :password => creds.password
  )

  projs.each do |pub_proj|
    pkg = pub_proj.packages.first
    pkg_file = pkg.to_s
    target_file_name = "#{pub_proj.version}/#{File.basename pkg_file}"

    info "Telling GitHub about #{target_file_name}"
    created = github_downloads.create('NUBIC', 'suite_authorization_source.rb',
      'name' => target_file_name,
      'size' => File.size(pkg_file),
      'description' => "#{pub_proj.name} #{pub_proj.version}",
      'content_type' => 'application/x-java-archive'
    )

    info "Uploading #{target_file_name}"
    github_downloads.upload(created, pkg_file)
  end
end
