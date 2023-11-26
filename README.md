## Updating dependencies

Use `bazel run @unpinned_maven//:pin` to update the maven source of truth, in this case the maven_lock.json with the deps in the MODULE.bazel file

Use `bazel run //:updatePythonReqs` to update the pip source of truth, in this case the requirements_lock.txt file in /third_party/python with the requirements.txt in the same directory
