repositories.remote << 'http://repo1.maven.org/maven2'
repositories.remote << 'https://ncimvn.nci.nih.gov/nexus/content/groups/public'
repositories.remote << 'http://repository.springsource.com/maven/bundles/external'

DepVersions = struct(
  :slf4j => '1.6.1'
)

Deps = struct(
  :jruby => "org.jruby:jruby-complete:jar:1.6.6",

  :osgi => struct(
    :core => 'org.osgi:org.osgi.core:jar:4.2.0',
    :compendium => 'org.osgi:org.osgi.compendium:jar:4.2.0'
  ),

  :slf4j => struct(
    :api => "org.slf4j:slf4j-api:jar:#{DepVersions.slf4j}",
    :jcl => "org.slf4j:jcl-over-slf4j:jar:#{DepVersions.slf4j}"
  ),

  :ctms_commons => struct(
    %w(base core suite-authorization).inject({}) { |h, a|
      h[a] = "gov.nih.nci.cabig.ctms:ctms-commons-#{a}:jar:1.1.1.RELEASE"; h
    }
  ),

  :spring => struct(
    %w(beans core).inject({}) { |h, a|
      h[a] = "org.springframework:spring-#{a}:jar:3.0.7.RELEASE" ; h
    }
  ),

  :testing => [
    "org.slf4j:slf4j-simple:jar:#{DepVersions.slf4j}"
  ],

  :paxexam => [
    %w(junit4 container-native testforge link-assembly).collect { |a|
      transitive("org.ops4j.pax.exam:pax-exam-#{a}:jar:2.3.0")
    },
    'org.ops4j.pax.url:pax-url-mvn:jar:1.3.5'
  ].flatten,

  :felix => struct(
    :framework => "org.apache.felix:org.apache.felix.framework:jar:3.0.9"
  )
)
