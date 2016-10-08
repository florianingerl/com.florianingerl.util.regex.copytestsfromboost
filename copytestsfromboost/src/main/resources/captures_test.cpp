pchar e1[4][5] = 
   {
      { "aBBcccDDDDDeeeeeeee", },
      { "a", "BB", "ccc", "DDDDD", "eeeeeeee", },
      { "a", "ccc", "eeeeeeee", },
      { "BB", "DDDDD", },
   };
   test_captures("(([a-z]+)|([A-Z]+))+", "aBBcccDDDDDeeeeeeee", e1);
   pchar e3[3][1] = 
   {
      { "abcbar" },
      { "abc" },
   };
   test_captures("(.*)bar|(.*)bah", "abcbar", e3);
   pchar e4[3][1] = 
   {
      { "abcbah" },
      { 0, },
      { "abc" },
   };
   test_captures("(.*)bar|(.*)bah", "abcbah", e4);
   pchar e5[2][16] = 
   {
      { "now is the time for all good men to come to the aid of the party" },
      { "now", "is", "the", "time", "for", "all", "good", "men", "to", "come", "to", "the", "aid", "of", "the", "party" },
   };
   test_captures("^(?:(\\w+)|(?>\\W+))*$", "now is the time for all good men to come to the aid of the party", e5);
   pchar e6[2][16] = 
   {
      { "now is the time for all good men to come to the aid of the party" },
      { "now", "is", "the", "time", "for", "all", "good", "men", "to", "come", "to", "the", "aid", "of", "the", "party" },
   };
   test_captures("^(?>(\\w+)\\W*)*$", "now is the time for all good men to come to the aid of the party", e6);
   pchar e7[4][14] = 
   {
      { "now is the time for all good men to come to the aid of the party" },
      { "now" },
      { "is", "the", "time", "for", "all", "good", "men", "to", "come", "to", "the", "aid", "of", "the" },
      { "party" },
   };
   test_captures("^(\\w+)\\W+(?>(\\w+)\\W+)*(\\w+)$", "now is the time for all good men to come to the aid of the party", e7);
   pchar e8[5][9] = 
   {
      { "now is the time for all good men to come to the aid of the party" } ,
      { "now" },
      { "is", "for", "men", "to", "of" },
      { "the", "time", "all", "good", "to", "come", "the", "aid", "the" },
      { "party" },
   };
   test_captures("^(\\w+)\\W+(?>(\\w+)\\W+(?:(\\w+)\\W+){0,2})*(\\w+)$", "now is the time for all good men to come to the aid of the party", e8);