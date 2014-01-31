Master-to-master API
====================

We are adding plugins to Jenkins that allows it to talk to other Jenkins masters.
To enable plugins to take advantage of this mechanism and interact with other masters,
this plugin defines a small contract.

The `InterMasterConnection` extension point, which can be implemented by any number of other plugins,
lists currently connected other masters. Each connected master is represented by a `Master` object,
which exposes several key properties of masters such as the following:

  - URL of the master
  - [RSA public key that uniquely identifies the instance globally](https://wiki.jenkins-ci.org/display/JENKINS/Instance+Identity)

The `Master` interface also provides access to a `Channel` object when the underlying transport supports that,
and it also provides the lookup API to query other service interfaces.

    Master master = ...;
    BuildTriggerService bts = master.getService(BuildTriggerService.class);
    bts.trigger("foo")


Sample implementation
---------------------
This plugin is currently in the beta stage and we are looking for feedback to the API.

This plugin also currently comes with the simple implementaion that directly connects two Jenkins masters
point-to-point via `Channel`, which helps validate the API design. The plan is to move this functionality
to another plugin before release, and make it the default implementation of this contract.

If you are interested in trying this out, launch Jenkins, go to the _Manage Jenkins_ page, and click
_Simple Inter-Master Communications_. You can have it talk to another Jenkins instance by typing in its URL.
The _Connect all_ button provides a crude way to reattempt connections to masters when they are dead.
