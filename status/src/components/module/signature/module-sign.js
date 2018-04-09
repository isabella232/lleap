import React from 'react'
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'

import Module from '../module'
import FileDropArea from './utils/drop-file-area'
import SignArea from './utils/sign-area'

/**
 * Module to upload a file and sign it using the current skipchain
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class ModuleSign extends React.Component {

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      file: undefined
    };

    this.handleFileDropped = this.handleFileDropped.bind(this);
    this.handleBack = this.handleBack.bind(this);
  }

  /**
   * Change the file we want to sign
   * @param {File} file
   */
  handleFileDropped(file) {
    this.setState({
      file
    });
  }

  /**
   * Cancel the current file
   */
  handleBack() {
    this.setState({file: undefined});
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {file} = this.state;
    const body = file ?
      <SignArea key="sign-area" file={file} onBack={this.handleBack}/> :
      <FileDropArea key="file-drop-area" onFileDrop={this.handleFileDropped}/>;

    return (
      <Module className="module-sign" title="Sign a file" icon="lock">
        <ReactCSSTransitionGroup transitionName="module"
                                 transitionLeaveTimeout={1}
                                 transitionEnterTimeout={300}>
          {body}
        </ReactCSSTransitionGroup>
      </Module>
    );
  }
}