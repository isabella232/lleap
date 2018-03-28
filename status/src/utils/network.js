// From:
// https://www.npmjs.com/package/split-host
// but it had syntax errors in it I had to fix, so I pasted it in here.
var isPort = function isPort(value) {
  return typeof value === 'number' && value >= 0 && value <= 65534 && Math.floor(value) === value;
};

function splitHost(host) {
  if (host === undefined) {
    return;
  }

  host = String(host);
  var i = host.lastIndexOf(':');

  if (i === -1) {
    var _port = +host;
    return isPort(_port) ? { port: _port } : { host: host, hostname: host };
  }

  var port = +host.slice(i + 1);
  if (isPort(port)) {
    var hostname = host.slice(0, i);
    return { host: hostname, hostname, port };
  }

    return { host: host, hostname: host };
};

/**
 * Transform a tcp address to the corresponding WS address because the Cothority uses the port + 1 of the tcp address
 *
 * @param {String} address - TCP with port address format
 * @returns {String} The same address with the port value +1
 */
export function tcp2ws(address) {
  let addr = address.replace('tls://', '');
  var hp = splitHost(addr)
  return hp.host + ':' + (hp.port + 1);
}
