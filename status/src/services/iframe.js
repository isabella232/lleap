import '../components/html-iframe/html-iframe.css'
import GenesisService from '../services/genesis'
import ByteBuffer from 'bytebuffer'

/**
 * This service takes care to trigger open and back events to components when the user
 * click on an HTML skipchain
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export class IFrameService {

  /**
   * 1. Get the html node for the service
   * 2. Add a listener to message event
   * @constructor
   */
  constructor() {
    this.listeners = [];

    window.addEventListener('message', (e) => {
      const data = e.data || '';
      if (data.indexOf("skipchain://") === 0) {
        this.open(data.replace("skipchain://", ""));
      }
    });
  }

  /**
   * Subscribe the listener to the upcoming events
   * @param {Object} listener
   */
  subscribe(listener) {
    if (this.listeners.indexOf(listener) === -1) {
      this.listeners.push(listener);
    }
  }

  /**
   * Remove the listener from the list of subscribed objects
   * @param {Object} listener
   */
  unsubscribe(listener) {
    const index = this.listeners.indexOf(listener);
    if (index > -1) {
      this.listeners.splice(index, 1);
    }
  }

  /**
   * Fetch and display the page of the skipchain with the given genesis ID
   * @param {String} id - 64 hex-digits ID of the genesis block
   */
  open(id) {

    return GenesisService.getLatestFromGenesisID(id).then((block) => {
      const data = ByteBuffer.wrap(block.Data);
      // buffer can have some issues to decode into UTF-8 so we force the decoding by hand
      let html = hexToUTF8(data.toHex());
      html = html.substr(html.indexOf('<!'));

      this.listeners.forEach(listener => IFrameService._triggerOpenEvent(listener, html));
    });
  }

  /**
   * Hide the website emulator
   */
  back() {
    this.listeners.forEach(listener => IFrameService._triggerBackEvent(listener));
  }

  /**
   * Call the open callback of each listener
   * @param {Object} listener
   * @param {String} html
   * @private
   */
  static _triggerOpenEvent(listener, html) {
    if (typeof listener.onOpenHTML === 'function') {
      listener.onOpenHTML(html);
    }
  }

  /**
   * Call the back callback of each listener
   * @param {Object} listener
   * @private
   */
  static _triggerBackEvent(listener) {
    if (typeof listener.onCloseHTML === 'function') {
      listener.onCloseHTML();
    }
  }

}

export default new IFrameService();

/**
 * Translate char by char a sequence of hex digit to UTF-8 digit
 * @param str
 * @returns {string}
 */
function hexToUTF8(str) {
  const hexes = str.match(/.{1,2}/g) || [];
  let back = "";
  for (let j = 0; j < hexes.length; j++) {
    back += String.fromCharCode(parseInt(hexes[j], 16));
  }

  return back;
}