import React from 'react'
import ReactDOM from 'react-dom'
import App from './app'

import '../public/FileSaver'
import './lib/bootstrap/scss/bootstrap.css'
import 'font-awesome/css/font-awesome.css'

import './index.css'

ReactDOM.render(
  <App />,
  document.getElementById('root')
);

/* Safari UInt8Array Fix */
Uint8Array.prototype.slice = Uint8Array.prototype.slice || function(a,b){ // eslint-disable-line
  return new Uint8Array(this.buffer.slice(a,b));
};
