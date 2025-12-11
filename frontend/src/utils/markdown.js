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
  code(code, infostring) {
    if (infostring === 'mermaid') {
      return `<div class="mermaid">${code}</div>`;
    }
    return false; // Delegate to other renderers
  }
};

marked.use({ renderer });

export { marked, mermaid };
