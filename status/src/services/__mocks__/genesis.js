
export default new class {

  subscribe(listener) {
    const blocks = [{
      Roster: {
        list: [{
          address: '127.0.0.1:7000'
        },{
          address: '127.0.0.1:7002'
        },{
          address: '127.0.0.1:7004'
        }]
      }
    }];

    const genesisList = [{
      GenesisID: '1',
      Data: 'aHR0cHM6Ly9kZWRpcy5jaA=='
    },{
      GenesisID: '2',
      Data: ''
    }];

    listener.onGenesisUpdate(blocks, genesisList);
  }

  unsubscribe = jest.fn();

  getLatestFromGenesisID = jest.fn().mockReturnValue(Promise.resolve({
    Data: new Uint8Array([0, 0, 0, 0]),
    Roster: {
      list: []
    }
  }));

}