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
 * Configured to allow Mermaid and KaTeX (MathML) tags/attributes.
 */
const sanitizedMarked = (content) => {
    const rawHtml = marked.parse(content || '');
    return DOMPurify.sanitize(rawHtml, {
        // Allow MathML for KaTeX and standard HTML profiles
        USE_PROFILES: { html: true, mathMl: true },
        // Explicitly allow div and class for Mermaid containers
        ADD_TAGS: ['div', 'span'], 
        ADD_ATTR: ['class', 'style', 'aria-hidden'],
        // Ensure data attributes are allowed if needed by any plugins
        FORBID_TAGS: ['script', 'style', 'iframe', 'object', 'embed'],
        FORBID_ATTR: ['onerror', 'onload', 'onclick']
    });
};

export { sanitizedMarked as marked, mermaid };
