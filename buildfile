require 'buildr/bnd'
require 'buildr-gemjar'

desc "A CTMS Suite authorization source based on Aker."
define "aker.ctmssuite" do
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
      b['Bundle-Activator'] = 'edu.northwestern.bioinformatics.aker.ctmssuite.Activator'
      b['Export-Package'] = 'edu.northwestern.bioinformatics.aker.ctmssuite'
      b['Private-Package'] = 'edu.northwestern.bioinformatics.aker.ctmssuite.internal'
    end
  end

  desc 'Aker and its dependencies'
  define 'aker-gems' do
    project.version = '3.0.3'

    package(:gemjar).with_gem('aker', project.version).with :manifest => {
      'Bundle-SymbolicName' => [project.group, project.name.gsub(':', '.')].join('.')
    }
  end

  define 'integration' do
    test.using(:junit, :integration).with Deps.felix, Deps.testing, Deps.paxexam,
      project('source').test_dependencies, project('aker-gems')
  end
end
