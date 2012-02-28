
var profiles = [
  {
    "id": "Default Profile",
    "4 questions": {
      "accountnr": 1111111,
      "card": "000M111",
      "expires": "05-2016",
      "birthday": "05-05-1987"
    },
    "activationcode": 111111,
    "pincode": 11112,
    "accounts": {
      "payment": [
        {
            "accountBalanceInCents" : 4470620,
            "accountCurrency" : "EUR",
            "accountId" : 747728259,
            "accountName" : "Hr A B C D E Mobile6",
            "accountNumber" : 747728259,
            "accountType" : "PAYMENT",
            "linkedAccountId" : "",
            "operationList" : ["VIEW_TRX", "TRANSFER_MONEY"].
            "productName" : "",
            "productType" : "",
        },
        {
            "accountBalanceInCents" : 5014876,
            "accountCurrency" : "EUR",
            "accountId" : 764757,
            "accountName" : "Mw A Wirdum",
            "accountNumber" : 764757,
            "accountType" : "PAYMENT",
            "linkedAccountId" : "",
            "operationList" : ["VIEW_TRX", "TRANSFER_MONEY"],
            "productName" : "",
            "productType" : "",
        }
      ],
      "savings": [
        {
          "accountBalanceInCents" : 323397936,
          "accountCurrency" : "EUR",
          "accountId" : 1165,
          "accountName" : "",
          "accountNumber" : 747728259,
          "accountType" : "SAVING",
          "linkedAccountId" : 747728259,
          "operationList" : ["VIEW_TRX", "DEPOSIT_MONEY", "WITHDRAW_MONEY"],
          "productName" : "Internetsparen",
          "productType" : 1010,
        },
        {
            "accountBalanceInCents" : 100250885427,
            "accountCurrency" : "EUR",
            "accountId" : 1181,
            "accountName" : "",
            "accountNumber" : 747728259,
            "accountType" : "SAVING",
            "linkedAccountId" : 747728259,
            "operationList" : ["VIEW_TRX", "DEPOSIT_MONEY"],
            "productName" : "Bonusrenterekening",
            "productType" : 1020,
        },
      ]
    },
    "actions": {
      "payment/preflight/": paymentPreflightOk(),
      "payment/authorize/": paymentAuthorizeOk(),
      "savings/preflight/": savingsPreflightOk(),
      "payment/authorize/": savingsAuthorizeOk()
    }
  }
]