const {test} = require("node:test");
const assert = require("node:assert/strict");
const {providerCategory} = require("../lib/newsCategories");

test("technology label is valid for each provider", () => {
  for (const provider of ["gnews", "currents", "newsdata"]) {
    assert.equal(providerCategory(provider, "Tech"), "technology");
  }
});

test("world maps to the Currents v1 taxonomy", () => {
  assert.equal(providerCategory("currents", "World"), "regional");
  assert.equal(providerCategory("gnews", "World"), "world");
  assert.equal(providerCategory("newsdata", "World"), "world");
});

test("no filter is sent for aggregate feeds", () => {
  assert.equal(providerCategory("gnews", "All"), undefined);
  assert.equal(providerCategory("currents", "For You"), undefined);
  assert.equal(providerCategory("newsdata", undefined), undefined);
});
