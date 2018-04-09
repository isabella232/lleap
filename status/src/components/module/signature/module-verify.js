import React from 'react'
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import {Row, Col, Form, FormGroup, Label, Input} from 'reactstrap'

import './module-verify.css'
import Module from '../module'
import DropFileArea from './utils/drop-file-area'
import GenesisService from '../../../services/genesis'
import SignatureFile from '../../../models/signature-file'
import {readAsString, hashFile} from '../../../utils/file'
import {buf2hex} from '../../../utils/buffer'
import {tcp2ws} from '../../../utils/network'

/**
 * Module to verify a given signature file and the target file
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export default class ModuleVerify extends React.Component {

  /**
   * @constructor
   * @param props
   */
  constructor(props) {
    super(props);

    this.state = {
      file: undefined,
      isVerified: false,
      isHashCorrect: false,
      isSignatureCorrect: false,
      error: ''
    };

    this.handleFileDrop = this.handleFileDrop.bind(this);
    this.handleFileReset = this.handleFileReset.bind(this);
  }

  /**
   * Handle a file dropped in the drop area
   * @param {File} fileDropped
   */
  handleFileDrop(fileDropped) {
    const {file, isVerified} = this.state;

    if (!file || (!!file && isVerified)) {
      // Either the file hasn't been uploaded yet or we have already verified the previous one
      this.setState({
        error: '',
        file: fileDropped,
        isVerified: false,
        isSignatureCorrect: false
      });

      return;
    }

    return this._verifySignature(fileDropped).then(
      (state) => {
        this.setState(Object.assign(state, {file: undefined}));
      },
      (e) => {
        this.setState({
          file: undefined,
          error: e.message
        });
      }
    );
  }

  /**
   * Reset the current state to be able to change the file selected
   */
  handleFileReset() {
    this.setState({
      file: undefined,
      isVerified: false,
      isSignatureCorrect: false,
      isHashCorrect: false
    });
  }

  /**
   * @override
   * @see https://facebook.github.io/react/docs/react-component.html
   * @returns {XML}
   */
  render() {
    const feedback = this._generateFeedback();

    return (
      <Module className="module-verify" title="Verify" icon="unlock">
        <Row>
          <Col xs="6"><DropFileArea onFileDrop={this.handleFileDrop}/></Col>
          <Col xs="6">
            <div className="module-verify-feedback">
              <ReactCSSTransitionGroup transitionName="feedback"
                                       transitionLeaveTimeout={1}
                                       transitionEnterTimeout={300}>
                {feedback}
              </ReactCSSTransitionGroup>
            </div>
          </Col>
        </Row>
      </Module>
    );
  }

  /**
   * Return the current status of the verifying process
   * @returns {XML}
   * @private
   */
  _generateFeedback() {
    const {file, isVerified, isSignatureCorrect, isHashCorrect, error} = this.state;

    let errorMessage = null;
    if (error.length > 0) {
      errorMessage = <p key="step-error" className="has-error">{error}</p>
    }

    const result = !isVerified ? errorMessage : (
        <FormGroup>
          <Label>
            Hash: <strong>{isHashCorrect ? 'verified' : <span className="has-error">wrong</span>}</strong>
          </Label><br/>
          <Label>
            Signature: <strong>{isSignatureCorrect ? 'verified' : <span className="has-error">wrong</span>}</strong>
          </Label>

          {errorMessage}
        </FormGroup>
      );

    return (
      <Form>
        <FormGroup check>
          <Label check onClick={this.handleFileReset}>
            <Input type="radio" name="step" checked={!file} readOnly/>
            Upload the file
          </Label>
        </FormGroup>
        <FormGroup className={!file ? 'disable' : ''} check>
          <Label check>
            <Input type="radio" name="step" checked={!!file} readOnly/>
            Upload the signature
          </Label>
        </FormGroup>
        {result}
      </Form>
    );
  }

  /**
   * Check the signature
   * @param infoFile {File}
   * @private
   */
  _verifySignature(infoFile) {
    const {file} = this.state;

    return new Promise((resolve, reject) => {
      readAsString(infoFile)
        .then(
          (info) => {
            const signFile = new SignatureFile();
            try {
              signFile.parse(info);
            }
            catch (e) {
              reject(e);
              return;
            }

            GenesisService.getLatestFromGenesisID(signFile.getGenesisID(true), signFile.getBlockID(true))
              .then((block) => {
                const pubkey = cryptoJS.aggregateKeys( // eslint-disable-line
                  block.Roster.list
                    .filter(s => signFile.getOfflineServers().every(off => tcp2ws(s.address).indexOf(off) === -1))
                    .map(s => s.public)
                );

                hashFile(file).then(
                  (hash) => {
                    resolve({
                      error: '',
                      isVerified: true,
                      isHashCorrect: buf2hex(hash) === signFile.getHash(true),
                      isSignatureCorrect: cryptoJS.verify(pubkey, hash, signFile.getSignature()) // eslint-disable-line
                    });
                  }
                );
              })
              .catch((e) => reject(e));
          }
        );
    });
  }
}