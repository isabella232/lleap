import SignatureFile from './signature-file'
import {buf2hex} from '../utils/buffer'

describe('models:signature-file', () => {

  it('should parse the file', () => {
    const content = JSON.stringify({
      filename: 'name',
      signature: 'signature',
      hash: '00',
      genesisID: '11',
      blockID: '22'
    });

    const sign = new SignatureFile();
    sign.parse(content);

    expect(sign.getFilename()).toBe('name');
    expect(sign.getHash(true)).toBe('00');
    expect(buf2hex(sign.getHash())).toBe('00');
    expect(sign.getOfflineServers()).toHaveLength(0);

    expect(() => sign.parse('adsa')).toThrow();
    expect(() => sign.parse('{}')).toThrow();
  });

});