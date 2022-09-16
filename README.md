# WebSight Bundle Resource Provider

#Introduction
The Bundle Resource Provider provides access to files/directories included in an OSGi bundle through the Sling ResourceResolver.

It was originally a fork of  [Apache Sling Bundle Resource Provider](https://github.com/apache/sling-org-apache-sling-bundleresource-impl).

#Configuration

Configuration is described in the original module documentation
https://sling.apache.org/documentation/bundles/bundle-resources-extensions-bundleresource.html#configuration

#Changes
Changed done on the original implementation are:

##Skipping sling:resourceType setting
Originally the default resource types - nt:file and nt:folder were stored in the `ValueMap`, therefore it was not possible to declare resource without `sling:resourceType` property.
In this implementation, the default resource types are returned only by "Resource.getResourceType", without affecting resources `ValueMap`.

This change was required to be compatible with `JcrResources` and to provide the same contract (i.e. to be easily used by `Resource Merger`)

##Support for in-place properties/resources definitions
Originally to create properties for 3 folders, the following structure was required:
```
--|
  |- dir1
  |- dir2
  |- dir3
  |- dir1.json
  |- dir2.json
  |- dir3.json
```
for more same-level resources the structure was becoming hard to maintain and work with.
To improve developers' experience it's possible to declare properties/resources using `.content.json` this fthe iles directly within folders.

So the structure can look like:
```
--|
  |- dir1
  |- dir1/.content.json
  |- dir2
  |- dir2/.content.json
  |- dir3
  |- dir3/.content.json
```

Note that both ways of defining resources are now supported. `.content.json` is controlled by `propJSON` mapping directive.

##Resource properties/children order guarantee
Some resources require properties and/or child resources to be provided in a specific order. Examples are:
- Configurations of RTE editor
- Dialogs definition composed from multiple tabs/fields

The guarantee of properties or child resources is fulfilled when using JSON definitions.

Resource created by loading example JSON:
```
{
"resource1": {},
"resource2": {},
"resource3": {},
"property1": 1,
"property1": 2,
"property1": 3
}
```
will contain properties accessible using `Resource.getValueMap` in order `[property1, property2, property3]` and children accesible using `Resource.getChildren` in order `[resource1, resource2,resource3]`.



##Default mapping definition values
To improve developer experience, we extended mappings definitions with default values.
 - `propsJSON` is now set to `.content.json`the 
,  to- support for `skipSettingResourceTypes` used in previous versions is removed
 
```
Sling-Bundle-Resources: /products;propsJSON:=.content.json;skipSettingResourceType:=true
```
is simplified to:
```
Sling-Bundle-Resources: /products
```

#Notes
The module is a direct replacement for original Sling Bundle Resource Provider. Nevertheless it's not fully backward compatible, so some corner-cases scenarios may require producing different results.
