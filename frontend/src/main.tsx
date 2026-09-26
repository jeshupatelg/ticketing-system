import React from 'react';
import ReactDOM from 'react-dom/client';
import { App } from './App';
import './index.css';

// Global fetch interceptor: converts background OAuth2/Keycloak 302 redirects into top-level window navigations
const originalFetch = window.fetch;
window.fetch = async (...args) => {
  const response = await originalFetch(...args);

  const isKeycloakRedirect =
    response.redirected &&
    (response.url.includes('/openid-connect/auth') || response.url.includes('keycloak'));

  const contentType = response.headers.get('content-type') || '';
  const isKeycloakHtml =
    contentType.includes('text/html') &&
    (response.url.includes('/openid-connect/auth') ||
      response.url.includes('keycloak') ||
      response.url.includes('/login'));

  if (isKeycloakRedirect || isKeycloakHtml) {
    // Reload the current document so the API Gateway intercepts a top-level HTML page request
    // and saves the current context path in its RequestCache for post-login return
    window.location.reload();
    // Halt further promise resolution while browser unloads the page
    return new Promise(() => {});
  }

  return response;
};

ReactDOM.createRoot(document.getElementById('root') as HTMLElement).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
