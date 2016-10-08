TEST_REGEX_SEARCH("(a(?1)b)", perl, "abc", match_default, make_array(-2, -2));
TEST_REGEX_SEARCH("(a(?1)+b)", perl, "abc", match_default, make_array(-2, -2));
TEST_REGEX_SEARCH("^([^()]|\\((?1)*\\))*$", perl, "abc", match_default, make_array(0, 3, 2, 3, -2, -2));
TEST_REGEX_SEARCH("^([^()]|\\((?1)*\\))*$", perl, "a(b)c", match_default, make_array(0, 5, 4, 5, -2, -2));
TEST_REGEX_SEARCH("^([^()]|\\((?1)*\\))*$", perl, "a(b(c))d", match_default, make_array(0, 8, 7, 8, -2, -2));
TEST_REGEX_SEARCH("^([^()]|\\((?1)*\\))*$", perl, "a(b(c)d", match_default, make_array(-2, -2));
TEST_REGEX_SEARCH("^\\>abc\\>([^()]|\\((?1)*\\))*\\<xyz\\<$", perl, ">abc>123<xyz<", match_default, make_array(0, 13, 7, 8, -2, -2));
TEST_REGEX_SEARCH("^\\>abc\\>([^()]|\\((?1)*\\))*\\<xyz\\<$", perl, ">abc>1(2)3<xyz<", match_default, make_array(0, 15, 9, 10, -2, -2));
TEST_REGEX_SEARCH("^\\>abc\\>([^()]|\\((?1)*\\))*\\<xyz\\<$", perl, ">abc>(1(2)3)<xyz<", match_default, make_array(0, 17, 5, 12, -2, -2));