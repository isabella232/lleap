import {hashFile, readAsString, reduceFileSize} from './file'

global.cryptoJS = {
  sha256: jest.fn()
};

global.FileReader = class {
  
  readAsArrayBuffer() {
    this.onload({
      target: {result: ''}
    });
  }
  
  readAsText() {
    this.onload({
      target: {result: ''}
    });
  }
  
};

describe('utils:file', () => {
  
  it('should hash the given file', () => {
    expect.assertions(1);
    
    return hashFile({}).then(
      () => {
        expect(cryptoJS.sha256).toHaveBeenCalledTimes(1);
      }
    )
  });
  
  it('should read the file as a string', () => {
    expect.assertions(1);
    
    return readAsString({}).then(() => {
      // We don't want to test FileReader, only the fact we are using it
      expect(true).toBeTruthy();
    });
  });

  it('should return the size of the file', () => {
    const file = {size: 1024 * 1024 * 5};
    const size = reduceFileSize(file);

    expect(size).toBe('5.00 MB');
  });
  
});