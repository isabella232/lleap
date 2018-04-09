/**
 * Read the given file object and return a promise that will resolve with the hash of the file
 * @param {File} file
 */
export function hashFile(file) {
  
  return new Promise((resolve) => {
    const reader = new FileReader();
    
    reader.onload = (e) => {
      const buf = new Uint8Array(e.target.result);
      resolve(cryptoJS.sha256(buf)); // eslint-disable-line
    };
    reader.readAsArrayBuffer(file);
  });
  
}

/**
 * Read a file as a string
 * @param {File} file
 * @returns {Promise}
 */
export function readAsString(file) {
  
  return new Promise((resolve) => {
    const reader = new FileReader();
    
    reader.onload = (e) => {
      resolve(e.target.result);
    };
    reader.readAsText(file);
  });
  
}

const SIZE_ABBREVIATIONS = ['B', 'kB', 'MB', 'GB'];

/**
 * Read the size of the file and return a human readable string
 * @param {File} file
 */
export function reduceFileSize(file) {
  let size = file.size;
  let abbr = 0;

  while (abbr < SIZE_ABBREVIATIONS.length - 1 && Math.round(size) > 1024) {
    size /= 1024;
    abbr++;
  }

  return `${Number(size).toFixed(2)} ${SIZE_ABBREVIATIONS[abbr]}`;
}