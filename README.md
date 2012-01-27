# Aker for the caBIG CTMS Suite

[Patient Study Calendar][psc] 2.10 and later supports an pluggable
authorization scheme defined by an OSGi bundle exporting a service
with a particular interface. Other caBIG CTMS Suite applications may
be modified to support this scheme in the future.

This library provides a plugin for this scheme which derives its
authorization information from an [Aker][aker] composite authority.

[aker]: https://github.com/NUBIC/aker
[psc]: https://wiki.nci.nih.gov/display/PSC/caBIG+Patient+Study+Calendar+%28PSC%29

## Prepare

The plugin requires two closely related configuration elements:

* An Aker configuration
* A way of mapping from the `Aker::User` objects provided by the Aker
  authority and users as defined by the CTMS Suite.

The plugin expects to read both these things from a ruby script whose
filename is indicated by a configuration property. The script will be
`eval`ed inside the plugin. Its effect should be to update the global
Aker configuration (`Aker.configuration`) with the authorities you
want to use, plus any configuration parameters they need (don't
forget to set a portal if your authorities use one). The plugin will
also read the user mapping from the Aker configuration; read on for
more details.

## User mapping

The user mapping must be a ruby object that responds to `call`,
receiving an array of `Aker::User`s and returning a hash mapping from
usernames to a hash of mapped suite user attributes.

Here's a very simple example to start things off. You could have a
very simple converter that gives everyone who logs in the Suite System
Administrator role and nothing else. (This wouldn't be very useful,
but there you go.) If it was called like so:

    all_sysadmins.call([Aker::User.new('alice'), Aker::User.new('bob')])

It would need to return a hash like this:

    {
      'alice' => {
         :id => 1,
         :roles => {
            :system_administrator => true
         }
      },
      bob' => {
         :id => 2,
         :roles => {
            :system_administrator => true
         }
      }
    }

There are several attributes the user mapping may return for each
user.

<table>
  <tr><th>Attribute</th><th>Mandatory</th><th>Description</th></tr>
  <tr>
    <td>`:id`</td><td>Yes</td>
    <td>
      A stable numeric ID for this user. This ID will be used to
      associate domain information with the user in the CTMS applications,
      so it should never change.
    </td>
  </tr>
  <tr>
    <td>`:username`</td><td>No</td>
    <td>
      The desired username for the user in the suite apps. If not
      specified, defaults to the username from the source
      `Aker::User`.
    </td>
  </tr>
  <tr>
    <td>`:first_name` and `:last_name`</td><td>No</td>
    <td>
      The first and/or last name for the user. These will be defaulted
      from the source `Aker::User` if not specified.
    </td>
  </tr>
  <tr>
    <td>`:roles`</td><td>Yes</td>
    <td>
      The suite roles this user should have. See the next section for
      more detail.
    </td>
  </tr>
</table>

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

#### In the user mapping

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
  `:studies` and the values may be either:
    * An array of strings indicating the assigned identifiers for the
      related domain objects under which the user's role membership is
      scoped, or
    * `true`, indicating that the user has the "all" scope for that scope
      type.

As noted above, each role has one of three possible required scopes:
site & study, site, and unscoped. If you do not provide scope
information for each type of scope that applies to the role, the role
won't take effect. (The plugin will detect and log this situation.)

## Use

Once you have your Aker configuration and your user mapping, you're
ready to deploy the plugin.

### More gems

TODO

### Deploying in PSC

#### Configuration

#### Bundles

# Project information

# Copyright
