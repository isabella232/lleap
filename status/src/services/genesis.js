import 'whatwg-fetch'
import SkipChainService from './skipchain'
import {hex2buf, buf2hex} from '../utils/buffer'
import {tcp2ws} from '../utils/network'

const GENESIS_BLOCK_SERVER = "https://lleap.dedis.ch/";
const GENESIS_BLOCK_FILE = "index.js";

/**
 * This service will contact the DEDIS server to get the list of skipchains with their servers. Those servers may
 * not be available.
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export class GenesisService {

  /**
   * Make a request to the GENESIS_BLOCK_SERVER url to get the list of genesis IDs of the skipchains
   * @constructor
   */
  constructor() {
    this.listeners = [];
    this.genesisList = [];
    this.curr_genesis = ''; // hex form
    this.blocks = [];

    // Get the servers list and the genesis block id
    this._fetch_request = fetch(GENESIS_BLOCK_SERVER + GENESIS_BLOCK_FILE, {headers: {'Content-Type': 'application/json'}})
      .then(
          (response) => response.json().then(data => {
          // We keep the list of available blocks
	  console.log("got index.js");
          this.genesisList = data.Blocks;
          this.curr_genesis = getFirstSkipChain(this.genesisList);

          this._request = this._fetchStatusForSkipchainID(this.curr_genesis);
        })
      )
      .catch((error) => {
          this.updateGenesis(new Error("Failed to get the list of Genesis blocks."));
      });
  }

  /**
   * Add the given listener to the list of object which will be called on update events
   * @param {Object} listener must have onGenesisUpdate function to get the updates
   */
  subscribe(listener) {
    if (this.listeners.indexOf(listener < 0)) {
      this.listeners.push(listener);

      // Trigger an event after the subscription. It can be an error update if one occurred before
      if (this.error) {
        this._triggerError(listener);
      }
      else {
        this._triggerEvent(listener);
      }
    }
  }

  /**
   * Remove the given listener from the list
   * @param {Object} listener
   */
  unsubscribe(listener) {
    const index = this.listeners.indexOf(listener);
    if (index >= 0) {
      this.listeners.splice(index, 1);
    }
  }

  /**
   * Trigger either an update or an error update
   * @param {Error} error a potential error that triggered
   */
  updateGenesis(error) {
    if (error) {
      this.error = error;
      this.listeners.forEach(listener => this._triggerError(listener));
    }
    else {
      this.listeners.forEach(listener => this._triggerEvent(listener));
    }
  }

  /**
   * Change the current skipchain given the ID of the genesis block
   * @param {String} id hex form of the ID
   */
  setCurrentSkipchainID(id) {
    const block = this.genesisList.filter(b => b.SkipchainID === id).pop();
    if (block) {
      // if the id exists we fetch the skipchain
      this.curr_genesis = id;
      this._request = this._fetchStatusForSkipchainID(id);
    }
  }

  /**
   * Given a genesis ID and a block ID, it will get the servers of the skipchain and then fetch the list of
   * blocks of the skipchain to finally return the block if it exists
   * If the block ID is not provided, we return the latest block
   * @param {String} id - Genesis ID
   * @param {String} blockID - 64 hex-digits Block ID
   * @returns {Promise}
   */
  getLatestFromSkipchainID(id, blockID = null) {
    return new Promise((resolve, reject) => {
      fetch(GENESIS_BLOCK_SERVER + id + '.js', {headers: {'Content-Type': 'application/json'}})
        .then(
          (response) => response.json().then(data => {
            SkipChainService.getLatestBlock(data.Servers.map(addr => tcp2ws(addr)), hex2buf(id))
              .then(data => {
                if (blockID) {
                  const block = data.filter(block => buf2hex(block.Hash) === blockID).pop();
                  block ? resolve(block) : reject();
                }
                else {
                  resolve(data.pop());
                }
              })
              .catch(e => reject(e));
          })
        )
        .catch(e => reject(e));
    });
  }

  /**
   * Trigger an update event to the listener with params:
   *  1. blocks The list of blocks of the skipchain
   *  2. genesisList The current list of available skipchains
   *  3. curr_genesis The current genesis ID of the current skipchain
   * @param {object} listener - An object with a declaration of onGenesisUpdate
   * @private
   */
  _triggerEvent(listener) {
    if (typeof listener.onGenesisUpdate === 'function') {
      listener.onGenesisUpdate(this.blocks, this.genesisList, this.curr_genesis);
    }
  }

  /**
   * Trigger an error event
   * @param {Object} listener
   * @private
   */
  _triggerError(listener) {
    if (typeof listener.onGenesisError === 'function') {
      listener.onGenesisError(this.error);
    }
  }

  /**
   * It will check that the skipchain exists and then it gets the list of blocks. Finally
   * it trigger according to the result either an update or an error
   * @param {String} id - The genesis ID
   * @private
   */
  _fetchStatusForSkipchainID(id) {
    const block = this.genesisList.filter(b => b.SkipchainID === id).pop();
    if (!block) {
      return this.updateGenesis(new Error("Cannot find the block associated with the genesis ID"));
    }

    const servers = block.Servers.map(addr => tcp2ws(addr));

    return SkipChainService.getLatestBlock(servers, hex2buf(block.SkipchainID))
	  .then((data) => {
        this.blocks = data;
        this.updateGenesis(null);
      })
      .catch((e) => this.updateGenesis(e));
  }
}

export default new GenesisService()

function getFirstSkipChain(list) {
    return list[0].SkipchainID;

    /*
    for (let i = 0; i < list.length; i++) {
	const block = list[i];

	if (!ByteBuffer.fromBase64(block.Data).toString('utf8').match(/^(https?|config):\/\//)) {
	    return block.SkipchainID;
	}
    }

    return '';
    */
}
