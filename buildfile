require 'buildr/bnd'
require 'buildr-gemjar'

desc "A bridge for building CTMS Suite authorization sources in JRuby."
define "ctms-auth-ruby" do
  project.version = '0.0.0.DEV'
  project.group = 'edu.northwestern.bioinformatics'

  desc 'The OSGi bundle that provides the SuiteAuthorizationSource'
  define 'source' do
    libs = Dir[_('lib', 'main', '*.jar')].collect { |a|
      fake_artifact_name = "#{project.group}.deps:#{File.basename(a)}:jar:0.0.0"
      artifact(fake_artifact_name, a)
    }

    compile.with libs, Deps.jruby, Deps.ctms_commons, Deps.osgi, Deps.spring,
      Deps.slf4j.api, Deps.slf4j.jcl
    test.using(:junit).with Deps.slf4j.simple

    package(:bundle).tap do |b|
      b['Bundle-Activator'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.Activator'
      b['Export-Package'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby'
      b['Private-Package'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal'
      b['Bundle-Name'] = 'suite_authorization_source.rb Main'
    end
  end

  define 'integrated-test-gems' do
    package(:gemjar).
      with_gem('aker', '3.0.3').
      with_gem('jruby-openssl', '0.7.5')
  end

  define 'integrated-tests' do
    process_dep = proc do |a|
      case
      when Buildr::Project === a
        a.packages.first
      when a.to_s =~ /org.osgi/
        nil
      else
        artifact(a)
      end
    end

    start_bundle_deps = [
      Deps.felix.configadmin,
      Deps.jakarta_commons.lang,
      Deps.jakarta_commons.collections,
      Deps.slf4j.api,
      project('source').and_dependencies
    ].flatten.collect(&process_dep).compact

    no_start_bundle_deps = [
      Deps.slf4j.simple,
      project('jruby-dynamic-import')
    ].collect(&process_dep).compact

    test.enhance(no_start_bundle_deps)

    test.
      using(:junit, :integration).
      with(
        Deps.felix, Deps.paxexam,
        project('source').test_dependencies,
        start_bundle_deps
      ).
      using(
        :properties => {
          "startBundleArtifacts" => start_bundle_deps.collect(&:name).join(','),
          "noStartBundleArtifacts" => no_start_bundle_deps.collect(&:name).join(',')
        },
        :java_args => ENV['DEBUG'] ? %w(-Xdebug -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5013) : []
      )

    # This jar is used by the integrated tests, but should not be put
    # on the classpath -- it is `require`d from the gem-based
    # source's ruby setup script.
    test.enhance([project('integrated-test-gems').packages.first])
  end

  # In order for JRuby to be able to load java code from jars in
  # random gems, it needs to have DynamicImport-Package: * set.
  define 'jruby-dynamic-import' do
    package(:jar).with(:manifest => {
        'Bundle-ManifestVersion' => 2,
        'Bundle-Name' => 'Fragment that adds dynamic importing to JRuby',
        'Bundle-SymbolicName' => [project.group, project.name.gsub(':', '.')].join('.'),
        'Bundle-Version' => project.version,
        'Fragment-Host' => 'org.jruby.jruby',
        'DynamicImport-Package' => '*'
      })
  end
end
