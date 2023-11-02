# Examples for Testing: Unified API

In this hierarchy, you will find examples for testing CoreMedia applications
on unit/integration test level.

These tests are based on the so-called XML repository, thus, backed by an
in-memory, non-production use repository, that is better perceived as a
mock only dedicated to lightweight testing.

**Read-Only:** The tests are meant to be changed over time, applying new best
practices, like new or better test engines, new or better assertion frameworks,
more test-cases, etc.

The tests are grouped by certain topics as well as by level of experience
required:

* [basic](basic/README.md) – very basic tests for first steps
* [advanced](advanced/README.md) – more advanced tests with more complex setup or assertions
* [expert](expert/README.md) – use-case driven tests without any recommended order

## Layout

### Guiding Comments

If feasible, tests come with some description not within code but as part
of the Javadoc of the test-class or the test-method. This helps to focus on
reading the test and as side effect may prevent copy & paste of irrelevant
comments (yes, you are invited to copy from here).

### Sidecars/Test-Resources

To ease locating test-resources that are dedicated to a given test, you will
find folders with names of the corresponding tests in `src/test/resources`.
Just as for the tests, also these resources may contain some guiding comments.
