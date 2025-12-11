import { marked } from 'marked';
import mermaid from 'mermaid';
import katex from 'katex';
import markedKatex from 'marked-katex-extension';
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
      return `<div class="mermaid">${token.text}</div>`;
    }
    // Fallback for string signature (if needed)
    if (arguments.length > 1 && arguments[1] === 'mermaid') {
       return `<div class="mermaid">${token}</div>`;
    }
    return false;
  }
};

marked.use({ renderer });

export { marked, mermaid };
