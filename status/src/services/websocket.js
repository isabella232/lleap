import CothorityMessages from '../lib/cothority-messages'

const EMPTY_MESSAGE = new Uint8Array([]);

/**
 * This service maintains or create websockets for the different actions the front-end can perform
 *
 * @author Gaylor Bosson (gaylor.bosson@epfl.ch)
 */
export class CothorityWebsocket {

  /**
   * @constructor
   */
  constructor() {
    this.status = {};
  }

  /**
   * Get the status of the given server among a WS
   * @param {String} address - ip address
   * @returns {Promise}
   */
  getStatus(address) {

    // The promise will resolve when the socket sends back the response of the call
    return new Promise((resolve, reject) => {
      this.status[address] = createSocket(
        this.status[address],
        address + '/Status/Request',
        (e) => reject(e),
        (data) => resolve(CothorityMessages.decodeStatusResponse(data)),
        EMPTY_MESSAGE
      );
    });

  }

  /**
   * Get the signature of the given hash with the given roster
   * @param {Uint8Array} hash - hash of the message
   * @param {String} address - ip address
   * @param {Roster} roster - check protobuf Roster message
   * @returns {Promise}
   */
  getSignature(hash, address, roster) {

    return new Promise((resolve, reject) => {
      this.sign_socket = createSocket(
        this.sign_socket,
        address + '/CoSi/SignatureRequest',
        (e) => reject(e),
        (data) => resolve(CothorityMessages.decodeSignatureResponse(data)),
        CothorityMessages.createSignatureRequest(hash, roster)
      );
    });

  }

  /**
   * Get the list of blocks of the skipchain with the given genesis ID
   * @param {String} address - ip address
   * @param {Uint8Array} id - Genesis ID
   * @returns {Promise}
   */
  getLatestBlock(address, id) {

    return new Promise((resolve, reject) => {
      this.latest_block = createSocket(
        this.latest_block,
        address + '/Skipchain/GetUpdateChain',
        (e) => reject(e),
        (data) => resolve(CothorityMessages.decodeLatestBlockResponse(data)),
        CothorityMessages.createLatestBlockRequest(id)
      )
    });

  }

  /**
   * Store a new block with the given roster
   * @param {String} address - ip address
   * @param {Uint8Array} id - Genesis ID
   * @param {Array} servers - list of servers
   * @returns {Promise}
   */
  storeNewBlock(address, id, servers) {

    return new Promise((resolve, reject) => {
      this.store_block = createSocket(
        this.store_block,
        address + '/Skipchain/StoreSkipBlock',
        (e) => reject(e),
        (data) => resolve(CothorityMessages.decodeStoreSkipBlockResponse(data)),
        CothorityMessages.createStoreSkipBlockRequest(id, servers)
      )
    });

  }

  /**
   * Get a random number from address
   * @param {String} address - ip address
   * @returns {Promise}
   */
  getRandom(address) {
    return new Promise((resolve, reject) => {
      this.random = createSocket(
        this.random,
        address + '/RandHound/RandRequest',
        (e) => reject(e),
        (data) => resolve(CothorityMessages.decodeRandomResponse(data)),
        CothorityMessages.createRandomMessage()
      )
    });
  }

}

export default new CothorityWebsocket();

/**
 * Use the existing socket or create a new one if required
 * @param socket - WebSocket old socket
 * @param address - String ws address
 * @param error - Function callback if an error occurred
 * @param callback - Function callback when a message is received
 * @param message - ArrayBuffer the message to send
 * @returns {*}
 */
function createSocket(socket, address, error, callback, message) {
  if (!socket || socket.readyState > 2) {
      const protocol = address.match(/^pulsar.dedis.ch/) ? 'wss' : 'ws';
      // TODO: Either fix the code that's sending these, or fix this more correctly here.
      address = address.replace("tcp://", "")  
      address = address.replace("tls://", "")  
    socket = new WebSocket(`${protocol}://${address}`);
    socket.binaryType = 'arraybuffer';
  }

  function onError(e) {
    socket.removeEventListener('error', onError);
    error(e);
  }
  socket.addEventListener('error', onError);

  function onMessage(e) {
    socket.removeEventListener('message', onMessage);
    callback(e.data);
  }
  socket.addEventListener('message', onMessage);

  if (socket.readyState === 0) {
    socket.addEventListener('open',() => {
      socket.send(message);
    });
  }
  else {
    socket.send(message);
  }

  return socket;
}
