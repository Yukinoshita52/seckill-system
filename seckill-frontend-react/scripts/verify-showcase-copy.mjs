import { readFileSync } from 'node:fs';
import { resolve } from 'node:path';
import assert from 'node:assert/strict';

const home = readFileSync(resolve('src/pages/Home.tsx'), 'utf8');
const layout = readFileSync(resolve('src/components/Layout.tsx'), 'utf8');

assert.match(layout, /高并发秒杀系统/, 'Expected header branding to describe the project showcase');
assert.match(home, /四层过滤/, 'Expected homepage to highlight the traffic-filtering story');
assert.match(home, /Lua 原子扣减/, 'Expected homepage to highlight inventory deduction');
assert.match(home, /一键演示真实链路效果/, 'Expected homepage to present the real-flow demo module');

console.log('Showcase narrative verified.');
