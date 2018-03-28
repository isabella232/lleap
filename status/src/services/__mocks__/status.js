
export default new class {
  subscribe = jest.fn();
  unsubscribe = jest.fn();

  getAvailableRoster = jest.fn().mockReturnValue([{
    server: {}
  }, {
    server: {}
  }, {
    server: {}
  }]);

  getOfflineRoster = jest.fn().mockReturnValue([{
    server: {address: ''}
  }]);
}