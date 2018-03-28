import React from 'react'
import ByteBuffer from 'bytebuffer'

import './module-html.css'
import Module from '../module'
import GenesisService from '../../../services/genesis'
import IFrameService from '../../../services/iframe'

/**
 * This module diplays the list of HTML skipchain. It shows only the ones with a base URL meaning that https://dedis.ch
 * will be shown but not https://dedis.ch/blabla
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class ModuleHTML extends React.Component {

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      genesisList: []
    };
  }

  /**
   * @see GenesisService._triggerEvent
   * @param {Array} blocks - unused
   * @param {Array} genesisList - List of the genesis blocks available
   */
  onGenesisUpdate(blocks, genesisList) {
    // this.setState({
    //   genesisList: genesisList.filter(b => {
    //     const data = ByteBuffer.fromBase64(b.Data);
    //     return data.toString('utf8').match(/^https?:\/\/[^/]+$/);
    //   })
    // });
  }

  /**
   * Open the HTML page with the given genesis ID
   * @param id {String} 64 hex-digits genesis ID
   */
  static handleOpenPage(id) {
    IFrameService.open(id)
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentWillMount() {
    GenesisService.subscribe(this);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   */
  componentWillUnmount() {
    GenesisService.unsubscribe(this);
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {genesisList} = this.state;

    const rows = genesisList.map(block => {
      return (
        <div key={block.GenesisID} className="module-html-item" onClick={() => ModuleHTML.handleOpenPage(block.GenesisID)}>
          {ByteBuffer.fromBase64(block.Data).toString('utf8')}
        </div>
      );
    });

    return (
      <Module title="HTML" icon="chain" className="module-html">
        {rows}
      </Module>
    );
  }
}
