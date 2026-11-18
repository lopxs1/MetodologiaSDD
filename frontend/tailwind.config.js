import fs from 'node:fs';

const tokens = JSON.parse(
  fs.readFileSync(new URL('../design/tokens.json', import.meta.url), 'utf-8')
);

const mapTokenGroup = (group) =>
  Object.fromEntries(Object.entries(group).map(([key, token]) => [key, token.$value]));

const mapFontFamilies = (group) =>
  Object.fromEntries(
    Object.entries(group).map(([key, token]) => [
      key,
      token.$value.split(',').map((item) => item.trim()),
    ])
  );

/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        brand: mapTokenGroup(tokens.color.brand),
        status: mapTokenGroup(tokens.color.status),
        role: mapTokenGroup(tokens.color.role),
        neutral: mapTokenGroup(tokens.color.neutral),
      },
      fontFamily: mapFontFamilies(tokens.typography.fontFamily),
      fontSize: mapTokenGroup(tokens.typography.fontSize),
      spacing: mapTokenGroup(tokens.spacing),
      borderRadius: mapTokenGroup(tokens.borderRadius),
    },
  },
  plugins: [],
};
