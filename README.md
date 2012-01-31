# caBIG CTMS Suite authorization in ruby

[Patient Study Calendar][psc] 2.10 and later supports an pluggable
authorization scheme defined by an OSGi bundle exporting a service
with a particular interface. Other caBIG CTMS Suite applications may
be modified to support this scheme in the future.

This library, `suite_authorization_source.rb`, provides a plugin for
this scheme which allows you to implement this interface using a
lightly-coupled ruby script.

[aker]: https://github.com/NUBIC/aker
[psc]: https://wiki.nci.nih.gov/display/PSC/caBIG+Patient+Study+Calendar+%28PSC%29

# The interface

The ruby interface this plugin expects is similar to the java
`SuiteAuthorizationSource` it adapts. The design difference is that it
uses ruby-native simple values for arguments and return values instead
of the strongly-typed objects used in the java version. While the java
objects would be available via JRuby, using simple native types should
make it possible to test adapters using MRI and make it easier for
developers without specific JRuby experience to write ruby
authorization sources.

## The user hash

Most of the native types used in the ruby interface are described below
alongside the methods. One structure is shared among all four methods:
the hash representing a single user. An example user hash:

    {
      :username => 'superuser',
      :id => 1,
      :first_name => 'Sue',
      :last_name => 'User',
      :email_address => 'sue@nihil.it',
      :roles => {
        :system_administrator => true,
        :user_administrator => { :sites => true }
      },
      :account_end_date => Date.new(2020, 3, 9)
    }

This hash describes a user with username `"superuser"` and two
roles. Here's what the attributes mean.

<table>
  <tr><th>Attribute</th><th>Mandatory</th><th>Description</th></tr>

  <tr>
    <td><code>:username</code></td><td>Yes</td>
    <td>
      The desired username for the user in the suite apps.
    </td>
  </tr>

  <tr>
    <td><code>:id</code></td><td>Yes</td>
    <td>
      A stable numeric ID for this user. This ID will be used to
      associate domain information with the user in the CTMS
      applications, so it should never change.
    </td>
  </tr>

  <tr>
    <td><code>:first_name</code> and <code>:last_name</code></td><td>Yes</td>
    <td>
      The first and last name for the user.
    </td>
  </tr>

  <tr>
    <td><code>:email_address</code></td><td>Yes</td>
    <td>
      A working e-mail address for the user.
    </td>
  </tr>

  <tr>
    <td><code>:account_end_date</code></td><td>No</td>
    <td>
      The date after which the user should no longer have access to
      the system. If the current date is later than this date, any
      authorization attempts for the user will fail. However, the user
      will still show up in user lists in the CTMS Suite apps (which
      may be desirable).
    </td>
  </tr>

  <tr>
    <td><code>:roles</code></td><td>Yes</td>
    <td>
      The suite roles this user should have. See the next section for
      more detail.
    </td>
  </tr>
</table>

All mandatory attributes must be present, non-nil, and not blank.

### Roles

#### In the suite

The caBIG CTMS Suite shares a [set of 23 roles][ccts-roles] across all
applications. Roles generally have non-overlapping capabilities and
each user may have several roles assigned.

Some roles are scopeable by site or by site and study. Such roles may
be associated with one or more specific sites (and studies, as
applicable), but may also be scoped to the concept "all sites" (or
"all studies"). The suite documentation contains a [list of all the
roles][ccts-roles], the scopes that apply to each, and brief
descriptions of their capabilities in each suite application.

[ccts-roles]: https://wiki.nci.nih.gov/display/Suite/Unified+Security+-+Roles+2.3

#### In the user hash

The `:roles` key in the user mapping result must point to a hash whose
keys are the names of the roles of which the user is a member. The
name must be a symbol and must be a downcased and underscored
version of one of the role names from the [suite docs][ccts-roles].

Each one of these role name keys may point to one of a couple of
values:

* `true`, meaning that the user has the role to the maximum extent
  possible. If the role is scoped, `true` here means that the user has
  all sites and, if applicable, all studies scope.
* A hash providing scoping information. This should be of the form `{
  :sites => %w(IL034 MN070), :studies => true }`. More technically, the
  two scopes are specified with the symbol keys `:sites` and
  `:studies`. The values may be either:
    * An array of strings indicating the assigned identifiers for the
      related domain objects under which the user's role membership is
      scoped, or
    * `true`, indicating that the user has the "all" scope for that scope
      type.

As noted above, each role has one of three possible required scopes:
site & study, site, and unscoped. If you do not provide scope
information for each type of scope that applies to the role, the role
won't take effect. (The plugin will detect and log this situation.)

## The interface (for real this time)

As noted above, the interface your script must provide is very similar
to the `SuiteAuthorizationSource` java interface natively supported by
PSC. It has four methods:

### get_user_by_username(username)

Corresponding java method: `getUser(String, SuiteUserRoleLevel)`.

Must return the user hash for the single user with exactly the given
username, or nil if there is no such user.

### get_user_by_id(id)

Corresponding java method: `getUser(long, SuiteUserRoleLevel)`.

Must return the user hash for the single user with the given numeric
ID, or nil if there is no such user.

### get_users_by_role(role_name)

Corresponding java method: `getUsersByRole(SuiteRole)`.

Must return an array of user hashes describing the users with the
given named role, regardless of scope. `role_name` will be a symbol
following the same construction rules as the keys in the user hash's
role hash (see above).

If there are no such users, it may return nil or an empty array.

The returned user hashes may omit role data if it will improve
performance.

### search_users(criteria)

Corresponding java method: `searchUsers(SuiteUserSearchOptions)`.

Must return an array of user hashes describing the users matching the
given criteria. `criteria` will be a hash with zero or more of the
following keys:

* `:username_substring`: a case-insensitive substring of the username.
* `:first_name_substring`: a case-insensitive substring of the first
  name.
* `:last_name_substring`: definition left as an exercise for the
  reader.

If `criteria` is an empty hash, this method must return all the
available users.

The returned user hashes may omit role data if it will improve
performance.

# Configure the plugin

This plugin requires that you specify (via a configuration property)
the filename of a ruby script which implements the interface described
above. The result of `eval`ing this script must be an object that
responds to the four specified methods in the way described. All
methods on the object returned must be re-entrant.

The script may, of course, refer to other files using the usual ruby
methods `require` and `load`. It may also refer to rubygems which have
been deployed in fragment bundles attached to the plugin bundle.

(TODO: flesh this out.)

## Deploying in PSC

### Configuration

### Bundles

# Project information

# Copyright
