import { describe, it, expect, vi } from 'vitest';
import { marked } from '../markdown';

describe('Markdown Utility', () => {
  it('renders basic markdown to HTML', () => {
    const md = '# Hello';
    const html = marked(md);
    expect(html).toContain('<h1>Hello</h1>');
  });

  it('handles empty input', () => {
    expect(marked('')).toBe('');
  });
});
