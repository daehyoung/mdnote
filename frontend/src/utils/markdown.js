import { marked } from 'marked';
import mermaid from 'mermaid';
import katex from 'katex';
import markedKatex from 'marked-katex-extension';
import DOMPurify from 'dompurify';
import 'katex/dist/katex.min.css';

// Initialize Mermaid once
mermaid.initialize({ 
    startOnLoad: false,
    theme: 'default',
    securityLevel: 'loose'
});

// Configure Marked with Katex
marked.use(markedKatex({
  throwOnError: false
}));

// Configure Mermaid Renderer
const renderer = {
  code(token) {
    if (typeof token === 'object' && token.lang === 'mermaid') {
      // We wrap mermaid in a div that is handled by the mermaid library later
      return `<div class="mermaid">${token.text}</div>`;
    }
    return false;
  }
};

marked.use({ renderer });

/**
 * Custom marked function that includes DOMPurify sanitization
 */
const sanitizedMarked = (content) => {
    const rawHtml = marked.parse(content);
    return DOMPurify.sanitize(rawHtml, {
        ADD_TAGS: ['div'], // Ensure div for mermaid is allowed
        ADD_ATTR: ['class'] 
    });
};

export { sanitizedMarked as marked, mermaid };
