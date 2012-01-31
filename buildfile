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

    compile.with libs, Deps.jruby, Deps.ctms_commons, Deps.osgi
    test.using(:junit).with Deps.testing

    package(:bundle).tap do |b|
      b['Bundle-Activator'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.Activator'
      b['Export-Package'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby'
      b['Private-Package'] = 'edu.northwestern.bioinformatics.ctmssuite.authorization.ruby.internal'
    end
  end

  define 'integration' do
    test.using(:junit, :integration).with Deps.felix, Deps.testing, Deps.paxexam,
      project('source').test_dependencies
  end
end
