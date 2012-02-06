require 'buildr/bnd'

desc "A bridge for building CTMS Suite authorization sources in JRuby."
define "ctms-auth-ruby" do
  project.version = '0.0.0'
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
    end
  end

  define 'integrated-tests' do
    start_bundle_deps = [
      Deps.felix.configadmin,
      Deps.jakarta_commons.lang,
      Deps.jakarta_commons.collections,
      Deps.slf4j.api,
      project('source').and_dependencies
    ].flatten.collect { |a|
      case
      when Buildr::Project === a
        a.packages.first
      when a.to_s =~ /org.osgi/
        nil
      else
        artifact(a)
      end
    }.compact

    no_start_bundle_deps = [
      Deps.slf4j.simple
    ].flatten.collect { |a|
      artifact(a)
    }

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
  end
end
