import React from 'react'
import Module from '../module'
import {FormGroup, Label, Input} from 'reactstrap'
import {Scrollbars} from 'react-custom-scrollbars';

import './module-skipchain.css'
import GenesisService from '../../../services/genesis'

/**
 * This module displays the list of the available skipchains except the ones
 * that contains an URL in the data
 *
 * @author Gaylor Bosson (gaylor.bosson@dmetrics.com)
 */
export default class ModuleSkipChain extends React.Component {

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      genesisList: [],
      currGenesis: ''
    };

    this.handleGenesisChange = this.handleGenesisChange.bind(this);
  }

  /**
   * It will update the state with the current skipchain
   * @param {Array} blocks
   * @param {Array} genesisList
   * @param {String} curr_genesis
   */
  onGenesisUpdate(blocks, genesisList, curr_genesis) {
    this.setState({
      blocks,
      genesisList: genesisList.filter(b => {
        return true;
      }),
      currGenesis: curr_genesis
    });
  }

  /**
   * Update the state to display the error
   * @param {Error} error
   */
  onGenesisError(error) {
    this.setState({error});
  }

  /**
   * It will trigger the skipchain change to the genesis service
   * @param {String} id - the new genesis block
   */
  handleGenesisChange(id) {
    const {currGenesis} = this.state;

    if (id !== currGenesis) {
      GenesisService.setCurrentSkipchainID(id);
    }
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
    const {genesisList, currGenesis, error} = this.state;

    if (error) {
      return ModuleSkipChain._renderError(error);
    }

    const rows = genesisList.map(b => {
      return (
        <FormGroup key={b.SkipchainID} check>
          <Label check>
            <Input type="radio" name="radio1" checked={b.SkipchainID === currGenesis}
                   onChange={() => this.handleGenesisChange(b.SkipchainID)}/>{' '}
            {b.SkipchainID.substr(0, 10)}
          </Label>
        </FormGroup>
      );
    });

    return (
      <Module title="SkipChain" icon="chain" className="module-skip-chain">
        <Scrollbars style={{width: "100%", position: "absolute", top: "0", bottom: "0"}}
                    renderThumbVertical={props => <div {...props} className="thumb-vertical"/>}>
          {rows}
        </Scrollbars>
      </Module>
    );
  }

  /**
   * Return the XML to display an error when it occurs
   * @param {Error} error
   * @returns {XML}
   * @private
   */
  static _renderError(error) {
    return (
      <Module title="SkipChain" icon="chain" className="module-skip-chain">
        <p className="has-error">
          {error.message}
        </p>
      </Module>
    );
  }

}
