import React from 'react'
import {PropTypes as T} from 'prop-types'

import {Button} from 'reactstrap'
import FontAwesome from 'react-fontawesome'

import './sign-area.css'
import {hashFile, reduceFileSize} from '../../../../utils/file'
import {tcp2ws} from '../../../../utils/network'
import CothorityWebsocket from '../../../../services/websocket'
import GenesisService from '../../../../services/genesis'
import StatusService from '../../../../services/status'
import SignatureFile from '../../../../models/signature-file'

/**
 * Show the status of the sign process
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class SignArea extends React.Component {

  static propTypes = {
    file: T.instanceOf(File).isRequired,
    onBack: T.func
  };

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      isSigning: false,
      error: '',

      latestBlock: null,
      genesisID: ''
    };

    this.handleBackAction = this.handleBackAction.bind(this);
    this.handleSign = this.handleSign.bind(this);
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
   * @see GenesisService._triggerEvent
   * @param {Array} blocks
   * @param {Array} genesisList
   * @param {String} currGenesis
   */
  onGenesisUpdate(blocks, genesisList, currGenesis) {
    this.setState({
      genesisID: currGenesis,
      block: blocks.slice().pop()
    });
  }

  /**
   * @see GenesisService._triggerError
   * @param {Error} error
   */
  onGenesisError(error) {
    this.setState({
      error: error.message
    });
  }

  /**
   * Return to the drop file component
   */
  handleBackAction() {
    this._triggerOnBack();
  }

  /**
   * Send a sign request over WS with the current loaded file
   */
  handleSign() {
    this.setState({
      isSigning: true
    });

    const {file} = this.props;
    // We assume we have a valid genesis ID and the associated block
    // else an error will be displayed before letting the user sign
    const {genesisID, block} = this.state;

    const servers = StatusService.getAvailableRoster().map(r => r.server);

    const signFile = new SignatureFile();
    signFile.setFileName(file.name);
    signFile.setGenesisID(genesisID);
    signFile.setBlockID(block.Hash);
    signFile.setOfflineServers(StatusService.getOfflineRoster().map(r => r.server.address));

    this._signPromise = new Promise((resolve, reject) => {
      // Check if more than 2/3 of the servers are available
      if (servers.length / block.Roster.list.length <= 2 / 3) {
        reject("Not enough available servers");
        return;
      }

      hashFile(file)
        .then(
          (hash) => {
            signFile.setHash(hash);
            const address = tcp2ws(servers[0].address);

            if (address.length > 0) {
              CothorityWebsocket.getSignature(hash, address, servers)
                .then(
                  (response) => {
                    const signature = (response.signature || []).slice(0, 64);
                    signFile.setSignature(signature);

                    signFile.save();
                    this._triggerOnBack();

                    resolve();
                  }
                )
                .catch(() => {
                  reject('Oops, something went wrong...');
                })
            }
          }
        );
    });

    // We want to display the error message if it occurs
    this._signPromise.catch(e => this.setState({error: e}));
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const {error} = this.state;
    if (error.length > 0) {
      return (
        <div className="sign-area">
          <div className="sign-area-error">{error}</div>
          <div className="sign-area-action">
            <Button color="danger" onClick={this.handleBackAction}>Ok</Button>
          </div>
        </div>
      );
    }

    const {file} = this.props;
    let size = reduceFileSize(file);

    const {isSigning} = this.state;
    const signAction = isSigning ? <div className="loading"><FontAwesome name="circle-o-notch" size="2x" spin/></div> :
      <Button color="success" onClick={this.handleSign}>Sign</Button>;

    return (
      <div className="sign-area">
        <div className="sign-area-file">
          <div><strong>Name</strong> {file.name}</div>
          <div><strong>Size</strong> {size}</div>
        </div>
        <div className="sign-area-action">
          <Button color="danger" onClick={this.handleBackAction}>Back</Button>
          {signAction}
        </div>
      </div>
    );
  }

  /**
   * Trigger the back event
   * @private
   */
  _triggerOnBack() {
    const {onBack} = this.props;
    if (typeof onBack === 'function') {
      onBack();
    }
  }
}
