import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import assert from 'node:assert/strict';

const css = readFileSync(resolve('src/index.css'), 'utf8');

function readToken(name) {
  const match = css.match(new RegExp(`--${name}:\\s*([^;]+);`));
  assert.ok(match, `Missing CSS token --${name}`);
  return match[1].trim().toLowerCase();
}

assert.equal(readToken('color-bg'), '#f6f8fc', 'Expected a light page background');
assert.equal(readToken('color-surface'), '#ffffff', 'Expected white surface cards');
assert.equal(readToken('color-text'), '#172033', 'Expected dark primary text');
assert.equal(readToken('color-text-secondary'), '#5b6475', 'Expected readable secondary text');
assert.match(css, /body\s*\{[\s\S]*background-color:\s*var\(--color-bg\);/m, 'Body should use the light background token');
console.log('Light theme tokens verified.');
