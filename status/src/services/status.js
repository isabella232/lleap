import CothorityWS from './websocket'
import GenesisService from './genesis'
import {tcp2ws} from '../utils/network'

const REFRESH_INTERVAL = 30000;

/**
 * This service takes care of keeping the status of the roster of the current skipchain up-to-date
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export class StatusService {

  /**
   * Subscribe the the genesis service
   * @constructor
   */
  constructor() {
    this.status = {};
    this.listeners = [];
    this.servers = [];
    this.genesisList = [];
    this.refreshInterval = REFRESH_INTERVAL;

    GenesisService.subscribe(this);
  }

  /**
   * On a new skipchain it resets the status and fetch the new roster
   * @param {Array} blocks
   * @param {Array} genesisList
   * @see GenesisService._triggerEvent
   */
  onGenesisUpdate(blocks, genesisList) {
    if (blocks.length === 0) {
      return;
    }

    this.status = {};
    this.servers = blocks.slice().pop().Roster.list.map(block => tcp2ws(block.address));
    this.genesisList = genesisList;

    this._updateStatus();
  }

  /**
   * Send the new status to listeners
   */
  triggerUpdate() {
    this.listeners.forEach((listener) => {
      this._triggerEvent(listener);
    });
  }

  /**
   * Add the given object to the list of listeners
   * @param {Object} listener
   */
  subscribe(listener) {
    if (this.listeners.indexOf(listener) === -1) {
      this.listeners.push(listener);

      if (Object.keys(this.status).length > 0) {
        this._triggerEvent(listener);
      }
    }
  }

  /**
   * Remove the listener from the list
   * @param {object} listener
   */
  unsubscribe(listener) {
    const index = this.listeners.indexOf(listener);
    if (index >= 0) {
      this.listeners.splice(index, 1);
    }
  }

  /**
   * Return the list of server that are responding
   * @returns {Array<*>}
   */
  getAvailableRoster() {
    return Object.keys(this.status)
      .map(key => this.status[key])
      .filter(r => !!r.system);
  }

  /**
   * Return the list of server that are not responding
   * @returns {Array.<*>}
   */
  getOfflineRoster() {
    return Object.keys(this.status)
      .map(key => this.status[key])
      .filter(r => !r.system);
  }

  /**
   * Maintain a timeout to fetch every given interval the status of the servers
   * @private
   */
  _updateStatus() {
    if (this._timer) {
      clearTimeout(this._timer);
      delete this._timer;
    }

    const self = this;

    this.servers.forEach((address, i) => CothorityWS.getStatus(address)
			 .then((response) => {
			   response.timestamp = Date.now(); // add the timestamp of the last check
			   self.status[address] = response;
			   self.triggerUpdate();
			 })
			 .catch(() => {
			   self.status[address] = {
			     timestamp: Date.now(),
			     server: {address}
			   };
			   self.triggerUpdate();
			 })
			);
    
    this._timer = setTimeout(() => self._updateStatus(), self.refreshInterval);
  }

  /**
   * Send the status to the given listener
   * @param {Object} listener
   * @private
   */
  _triggerEvent(listener) {
    if (typeof listener.onStatusUpdate === 'function') {
      listener.onStatusUpdate(this.status, this.genesisList);
    }
  }
}

export default new StatusService();
