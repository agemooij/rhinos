
var notOk = function(errorCode, parameters) {
  if (!(parameters instanceof Object)) {
    parameters = {}
  }
  
  return {
      "evidenceMessage" : "<null>",
      "returnCode": "NOT-OK",
      "sessionKey": "",
      "statusData": {
          "code": 120,
          "parameters": parameters
  }
};