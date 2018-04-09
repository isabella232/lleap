import {hex2buf, buf2hex} from './buffer'

describe('utils:buffer', () => {
  
  it('should convert to buf', () => {
    const buf = hex2buf('000102');
    expect(buf.toString()).toBe("0,1,2");
  });
  
  it('should convert to hex', () => {
    const hex = buf2hex(new Uint8Array([0, 1, 2]));
    expect(hex).toBe("000102");
  });
  
});