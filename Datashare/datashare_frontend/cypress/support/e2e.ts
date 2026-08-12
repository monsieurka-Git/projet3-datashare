// Cypress E2E support — DataShare
import './commands';
import '@cypress/code-coverage/support';

// Ignore les erreurs Angular non bloquantes (zone.js / chunk load)
Cypress.on('uncaught:exception', (err) => {
  if (
    err.message.includes('ResizeObserver') ||
    err.message.includes('NG0') ||
    err.message.includes('ExpressionChangedAfterItHasBeenChecked')
  ) {
    return false;
  }
  return true;
});
