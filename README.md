# OMI-ServiceNow Integration

**This document will NOT serve to teach someone how to be comfortable with RESTful services, or the languages used.**

## Omi -> Service-Now
When an OMI event is registered (internally or from external tools sending data), that matches a user created "Event Forwarding Filter", it will trigger any code added to a "Connected Server' in OMI that is defined within the "Event Forwarding Filter". This code/script is written in Groovy, with a pre-defined set of functions that must defined. These methods are called when specific events occur, and cannot be changed (eg creating or updating an external event). Below is the code. The response from creation is parsed, and the Service-Now incident ID is stored into the event record in OMI. Currently we can only create new records, the update method doesn't seem to like to execute, no errors to be found. just won't run.

## Sensu -> Omi
A handler has been written that will foward the event on to OMI via the RESTful OPR event service in OMI. Currently the URL and auth hash are hard coded. I will need to find a better way to do this in the future, especially before we go to prod (even just an environment parameter) but hey, do and improve!
