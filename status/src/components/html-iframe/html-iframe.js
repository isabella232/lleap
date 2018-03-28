import React from 'react'

import './html-iframe.css'
import IFrameService from '../../services/iframe'

/**
 * Show a fixed IFrame when the user click on an HTML skipchain
 *
 * @author Gaylor Bosson (galor.bosson@epfl.ch)
 */
export default class HTMLIFrame extends React.Component {

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      html: null
    };

    this.handleBack = this.handleBack.bind(this);
  }

  /**
   * update the html state to show the content in the IFrame
   * @param {String} html - the html content to show
   */
  onOpenHTML(html) {
    this.setState({html});
  }

  /**
   * Reset the content of the html state to hide the IFrame
   */
  onCloseHTML() {
    this.setState({
      html: null
    });
  }

  /**
   * Handle the back action that will close the IFrame
   * We use the service instead of the state to trigger the event to possible other components
   */
  handleBack() {
    IFrameService.back();
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentWillMount() {
    IFrameService.subscribe(this);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentWillUnmount() {
    IFrameService.unsubscribe(this);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {html} = this.state;

    if (!html) {
      // hide the whole component if we don't show anything
      return null;
    }

    return (
      <div className="block-iframe">
        <div>
          DEDIS WebSite Emulator
          <span><a href="#" onClick={this.handleBack}>Back</a></span>
        </div>
        <iframe src={`data:text/html;base64,${btoa(html)}`}></iframe>
      </div>
    );
  }
}