import '@testing-library/jest-dom';
import React from 'react';
global.React = React;

if (typeof window !== 'undefined') {
  window.HTMLElement.prototype.scrollIntoView = function() {};
}
