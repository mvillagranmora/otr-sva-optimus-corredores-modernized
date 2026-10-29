// Generated from ../src/cl/kaiser/odata/OdataFilter.g4 by ANTLR 4.5.3
package cl.kaiser.odata.gen;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class OdataFilterLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, Number=23, Variable=24, 
		IdentName=25, CharacterLiteral=26, StringLiteral=27, FloatingPointLiteral=28, 
		WS=29;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
		"T__17", "T__18", "T__19", "T__20", "T__21", "Number", "Variable", "IdentName", 
		"CharacterLiteral", "StringLiteral", "FloatingPointLiteral", "Digit", 
		"Letter", "EscapeSequence", "OctalEscape", "UnicodeEscape", "HexDigit", 
		"Exponent", "FloatTypeSuffix", "WS"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'('", "')'", "'or'", "'and'", "'eq'", "'ne'", "'ge'", "'gt'", "'le'", 
		"'lt'", "'eq null'", "'ne null'", "'eq '''", "'ne '''", "'startswith'", 
		"','", "'eq false'", "'endswith'", "'substringof'", "'datetime'", "'tolower'", 
		"'toupper'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, null, null, null, null, null, null, null, "Number", 
		"Variable", "IdentName", "CharacterLiteral", "StringLiteral", "FloatingPointLiteral", 
		"WS"
	};
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public OdataFilterLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "OdataFilter.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\37\u0167\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3"+
		"\5\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21"+
		"\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24\3\24"+
		"\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\30"+
		"\5\30\u00cc\n\30\3\30\6\30\u00cf\n\30\r\30\16\30\u00d0\3\31\6\31\u00d4"+
		"\n\31\r\31\16\31\u00d5\3\31\7\31\u00d9\n\31\f\31\16\31\u00dc\13\31\3\32"+
		"\3\32\6\32\u00e0\n\32\r\32\16\32\u00e1\3\32\3\32\3\32\7\32\u00e7\n\32"+
		"\f\32\16\32\u00ea\13\32\3\33\3\33\3\33\7\33\u00ef\n\33\f\33\16\33\u00f2"+
		"\13\33\3\33\3\33\3\34\3\34\3\34\7\34\u00f9\n\34\f\34\16\34\u00fc\13\34"+
		"\3\34\3\34\3\35\5\35\u0101\n\35\3\35\6\35\u0104\n\35\r\35\16\35\u0105"+
		"\3\35\3\35\7\35\u010a\n\35\f\35\16\35\u010d\13\35\3\35\5\35\u0110\n\35"+
		"\3\35\5\35\u0113\n\35\3\35\5\35\u0116\n\35\3\35\3\35\6\35\u011a\n\35\r"+
		"\35\16\35\u011b\3\35\5\35\u011f\n\35\3\35\5\35\u0122\n\35\3\35\5\35\u0125"+
		"\n\35\3\35\6\35\u0128\n\35\r\35\16\35\u0129\3\35\3\35\5\35\u012e\n\35"+
		"\3\35\5\35\u0131\n\35\3\35\6\35\u0134\n\35\r\35\16\35\u0135\3\35\5\35"+
		"\u0139\n\35\3\36\3\36\3\37\3\37\3 \3 \3 \3 \5 \u0143\n \3!\3!\3!\3!\3"+
		"!\3!\3!\3!\3!\5!\u014e\n!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3$\3$\5$\u015b"+
		"\n$\3$\6$\u015e\n$\r$\16$\u015f\3%\3%\3&\3&\3&\3&\2\2\'\3\3\5\4\7\5\t"+
		"\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\2=\2?\2A\2C\2E"+
		"\2G\2I\2K\37\3\2\f\4\2//aa\4\2))^^\4\2$$^^\4\2C\\c|\n\2$$))^^ddhhpptt"+
		"vv\5\2\62;CHch\4\2GGgg\4\2--//\6\2FFHHffhh\5\2\13\f\16\17\"\"\u0182\2"+
		"\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2"+
		"\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2"+
		"\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2"+
		"\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2K\3\2"+
		"\2\2\3M\3\2\2\2\5O\3\2\2\2\7Q\3\2\2\2\tT\3\2\2\2\13X\3\2\2\2\r[\3\2\2"+
		"\2\17^\3\2\2\2\21a\3\2\2\2\23d\3\2\2\2\25g\3\2\2\2\27j\3\2\2\2\31r\3\2"+
		"\2\2\33z\3\2\2\2\35\u0080\3\2\2\2\37\u0086\3\2\2\2!\u0091\3\2\2\2#\u0093"+
		"\3\2\2\2%\u009c\3\2\2\2\'\u00a5\3\2\2\2)\u00b1\3\2\2\2+\u00ba\3\2\2\2"+
		"-\u00c2\3\2\2\2/\u00cb\3\2\2\2\61\u00d3\3\2\2\2\63\u00df\3\2\2\2\65\u00eb"+
		"\3\2\2\2\67\u00f5\3\2\2\29\u0138\3\2\2\2;\u013a\3\2\2\2=\u013c\3\2\2\2"+
		"?\u0142\3\2\2\2A\u014d\3\2\2\2C\u014f\3\2\2\2E\u0156\3\2\2\2G\u0158\3"+
		"\2\2\2I\u0161\3\2\2\2K\u0163\3\2\2\2MN\7*\2\2N\4\3\2\2\2OP\7+\2\2P\6\3"+
		"\2\2\2QR\7q\2\2RS\7t\2\2S\b\3\2\2\2TU\7c\2\2UV\7p\2\2VW\7f\2\2W\n\3\2"+
		"\2\2XY\7g\2\2YZ\7s\2\2Z\f\3\2\2\2[\\\7p\2\2\\]\7g\2\2]\16\3\2\2\2^_\7"+
		"i\2\2_`\7g\2\2`\20\3\2\2\2ab\7i\2\2bc\7v\2\2c\22\3\2\2\2de\7n\2\2ef\7"+
		"g\2\2f\24\3\2\2\2gh\7n\2\2hi\7v\2\2i\26\3\2\2\2jk\7g\2\2kl\7s\2\2lm\7"+
		"\"\2\2mn\7p\2\2no\7w\2\2op\7n\2\2pq\7n\2\2q\30\3\2\2\2rs\7p\2\2st\7g\2"+
		"\2tu\7\"\2\2uv\7p\2\2vw\7w\2\2wx\7n\2\2xy\7n\2\2y\32\3\2\2\2z{\7g\2\2"+
		"{|\7s\2\2|}\7\"\2\2}~\7)\2\2~\177\7)\2\2\177\34\3\2\2\2\u0080\u0081\7"+
		"p\2\2\u0081\u0082\7g\2\2\u0082\u0083\7\"\2\2\u0083\u0084\7)\2\2\u0084"+
		"\u0085\7)\2\2\u0085\36\3\2\2\2\u0086\u0087\7u\2\2\u0087\u0088\7v\2\2\u0088"+
		"\u0089\7c\2\2\u0089\u008a\7t\2\2\u008a\u008b\7v\2\2\u008b\u008c\7u\2\2"+
		"\u008c\u008d\7y\2\2\u008d\u008e\7k\2\2\u008e\u008f\7v\2\2\u008f\u0090"+
		"\7j\2\2\u0090 \3\2\2\2\u0091\u0092\7.\2\2\u0092\"\3\2\2\2\u0093\u0094"+
		"\7g\2\2\u0094\u0095\7s\2\2\u0095\u0096\7\"\2\2\u0096\u0097\7h\2\2\u0097"+
		"\u0098\7c\2\2\u0098\u0099\7n\2\2\u0099\u009a\7u\2\2\u009a\u009b\7g\2\2"+
		"\u009b$\3\2\2\2\u009c\u009d\7g\2\2\u009d\u009e\7p\2\2\u009e\u009f\7f\2"+
		"\2\u009f\u00a0\7u\2\2\u00a0\u00a1\7y\2\2\u00a1\u00a2\7k\2\2\u00a2\u00a3"+
		"\7v\2\2\u00a3\u00a4\7j\2\2\u00a4&\3\2\2\2\u00a5\u00a6\7u\2\2\u00a6\u00a7"+
		"\7w\2\2\u00a7\u00a8\7d\2\2\u00a8\u00a9\7u\2\2\u00a9\u00aa\7v\2\2\u00aa"+
		"\u00ab\7t\2\2\u00ab\u00ac\7k\2\2\u00ac\u00ad\7p\2\2\u00ad\u00ae\7i\2\2"+
		"\u00ae\u00af\7q\2\2\u00af\u00b0\7h\2\2\u00b0(\3\2\2\2\u00b1\u00b2\7f\2"+
		"\2\u00b2\u00b3\7c\2\2\u00b3\u00b4\7v\2\2\u00b4\u00b5\7g\2\2\u00b5\u00b6"+
		"\7v\2\2\u00b6\u00b7\7k\2\2\u00b7\u00b8\7o\2\2\u00b8\u00b9\7g\2\2\u00b9"+
		"*\3\2\2\2\u00ba\u00bb\7v\2\2\u00bb\u00bc\7q\2\2\u00bc\u00bd\7n\2\2\u00bd"+
		"\u00be\7q\2\2\u00be\u00bf\7y\2\2\u00bf\u00c0\7g\2\2\u00c0\u00c1\7t\2\2"+
		"\u00c1,\3\2\2\2\u00c2\u00c3\7v\2\2\u00c3\u00c4\7q\2\2\u00c4\u00c5\7w\2"+
		"\2\u00c5\u00c6\7r\2\2\u00c6\u00c7\7r\2\2\u00c7\u00c8\7g\2\2\u00c8\u00c9"+
		"\7t\2\2\u00c9.\3\2\2\2\u00ca\u00cc\7/\2\2\u00cb\u00ca\3\2\2\2\u00cb\u00cc"+
		"\3\2\2\2\u00cc\u00ce\3\2\2\2\u00cd\u00cf\5;\36\2\u00ce\u00cd\3\2\2\2\u00cf"+
		"\u00d0\3\2\2\2\u00d0\u00ce\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\60\3\2\2"+
		"\2\u00d2\u00d4\5=\37\2\u00d3\u00d2\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00d3"+
		"\3\2\2\2\u00d5\u00d6\3\2\2\2\u00d6\u00da\3\2\2\2\u00d7\u00d9\5;\36\2\u00d8"+
		"\u00d7\3\2\2\2\u00d9\u00dc\3\2\2\2\u00da\u00d8\3\2\2\2\u00da\u00db\3\2"+
		"\2\2\u00db\62\3\2\2\2\u00dc\u00da\3\2\2\2\u00dd\u00e0\5=\37\2\u00de\u00e0"+
		"\7a\2\2\u00df\u00dd\3\2\2\2\u00df\u00de\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1"+
		"\u00df\3\2\2\2\u00e1\u00e2\3\2\2\2\u00e2\u00e8\3\2\2\2\u00e3\u00e7\5="+
		"\37\2\u00e4\u00e7\5;\36\2\u00e5\u00e7\t\2\2\2\u00e6\u00e3\3\2\2\2\u00e6"+
		"\u00e4\3\2\2\2\u00e6\u00e5\3\2\2\2\u00e7\u00ea\3\2\2\2\u00e8\u00e6\3\2"+
		"\2\2\u00e8\u00e9\3\2\2\2\u00e9\64\3\2\2\2\u00ea\u00e8\3\2\2\2\u00eb\u00f0"+
		"\7)\2\2\u00ec\u00ef\5? \2\u00ed\u00ef\n\3\2\2\u00ee\u00ec\3\2\2\2\u00ee"+
		"\u00ed\3\2\2\2\u00ef\u00f2\3\2\2\2\u00f0\u00ee\3\2\2\2\u00f0\u00f1\3\2"+
		"\2\2\u00f1\u00f3\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f3\u00f4\7)\2\2\u00f4"+
		"\66\3\2\2\2\u00f5\u00fa\7$\2\2\u00f6\u00f9\5? \2\u00f7\u00f9\n\4\2\2\u00f8"+
		"\u00f6\3\2\2\2\u00f8\u00f7\3\2\2\2\u00f9\u00fc\3\2\2\2\u00fa\u00f8\3\2"+
		"\2\2\u00fa\u00fb\3\2\2\2\u00fb\u00fd\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd"+
		"\u00fe\7$\2\2\u00fe8\3\2\2\2\u00ff\u0101\7/\2\2\u0100\u00ff\3\2\2\2\u0100"+
		"\u0101\3\2\2\2\u0101\u0103\3\2\2\2\u0102\u0104\4\62;\2\u0103\u0102\3\2"+
		"\2\2\u0104\u0105\3\2\2\2\u0105\u0103\3\2\2\2\u0105\u0106\3\2\2\2\u0106"+
		"\u0107\3\2\2\2\u0107\u010b\7\60\2\2\u0108\u010a\4\62;\2\u0109\u0108\3"+
		"\2\2\2\u010a\u010d\3\2\2\2\u010b\u0109\3\2\2\2\u010b\u010c\3\2\2\2\u010c"+
		"\u010f\3\2\2\2\u010d\u010b\3\2\2\2\u010e\u0110\5G$\2\u010f\u010e\3\2\2"+
		"\2\u010f\u0110\3\2\2\2\u0110\u0112\3\2\2\2\u0111\u0113\5I%\2\u0112\u0111"+
		"\3\2\2\2\u0112\u0113\3\2\2\2\u0113\u0139\3\2\2\2\u0114\u0116\7/\2\2\u0115"+
		"\u0114\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u0117\3\2\2\2\u0117\u0119\7\60"+
		"\2\2\u0118\u011a\4\62;\2\u0119\u0118\3\2\2\2\u011a\u011b\3\2\2\2\u011b"+
		"\u0119\3\2\2\2\u011b\u011c\3\2\2\2\u011c\u011e\3\2\2\2\u011d\u011f\5G"+
		"$\2\u011e\u011d\3\2\2\2\u011e\u011f\3\2\2\2\u011f\u0121\3\2\2\2\u0120"+
		"\u0122\5I%\2\u0121\u0120\3\2\2\2\u0121\u0122\3\2\2\2\u0122\u0139\3\2\2"+
		"\2\u0123\u0125\7/\2\2\u0124\u0123\3\2\2\2\u0124\u0125\3\2\2\2\u0125\u0127"+
		"\3\2\2\2\u0126\u0128\4\62;\2\u0127\u0126\3\2\2\2\u0128\u0129\3\2\2\2\u0129"+
		"\u0127\3\2\2\2\u0129\u012a\3\2\2\2\u012a\u012b\3\2\2\2\u012b\u012d\5G"+
		"$\2\u012c\u012e\5I%\2\u012d\u012c\3\2\2\2\u012d\u012e\3\2\2\2\u012e\u0139"+
		"\3\2\2\2\u012f\u0131\7/\2\2\u0130\u012f\3\2\2\2\u0130\u0131\3\2\2\2\u0131"+
		"\u0133\3\2\2\2\u0132\u0134\4\62;\2\u0133\u0132\3\2\2\2\u0134\u0135\3\2"+
		"\2\2\u0135\u0133\3\2\2\2\u0135\u0136\3\2\2\2\u0136\u0137\3\2\2\2\u0137"+
		"\u0139\5I%\2\u0138\u0100\3\2\2\2\u0138\u0115\3\2\2\2\u0138\u0124\3\2\2"+
		"\2\u0138\u0130\3\2\2\2\u0139:\3\2\2\2\u013a\u013b\4\62;\2\u013b<\3\2\2"+
		"\2\u013c\u013d\t\5\2\2\u013d>\3\2\2\2\u013e\u013f\7^\2\2\u013f\u0143\t"+
		"\6\2\2\u0140\u0143\5C\"\2\u0141\u0143\5A!\2\u0142\u013e\3\2\2\2\u0142"+
		"\u0140\3\2\2\2\u0142\u0141\3\2\2\2\u0143@\3\2\2\2\u0144\u0145\7^\2\2\u0145"+
		"\u0146\4\62\65\2\u0146\u0147\4\629\2\u0147\u014e\4\629\2\u0148\u0149\7"+
		"^\2\2\u0149\u014a\4\629\2\u014a\u014e\4\629\2\u014b\u014c\7^\2\2\u014c"+
		"\u014e\4\629\2\u014d\u0144\3\2\2\2\u014d\u0148\3\2\2\2\u014d\u014b\3\2"+
		"\2\2\u014eB\3\2\2\2\u014f\u0150\7^\2\2\u0150\u0151\7w\2\2\u0151\u0152"+
		"\5E#\2\u0152\u0153\5E#\2\u0153\u0154\5E#\2\u0154\u0155\5E#\2\u0155D\3"+
		"\2\2\2\u0156\u0157\t\7\2\2\u0157F\3\2\2\2\u0158\u015a\t\b\2\2\u0159\u015b"+
		"\t\t\2\2\u015a\u0159\3\2\2\2\u015a\u015b\3\2\2\2\u015b\u015d\3\2\2\2\u015c"+
		"\u015e\4\62;\2\u015d\u015c\3\2\2\2\u015e\u015f\3\2\2\2\u015f\u015d\3\2"+
		"\2\2\u015f\u0160\3\2\2\2\u0160H\3\2\2\2\u0161\u0162\t\n\2\2\u0162J\3\2"+
		"\2\2\u0163\u0164\t\13\2\2\u0164\u0165\3\2\2\2\u0165\u0166\b&\2\2\u0166"+
		"L\3\2\2\2\"\2\u00cb\u00d0\u00d5\u00da\u00df\u00e1\u00e6\u00e8\u00ee\u00f0"+
		"\u00f8\u00fa\u0100\u0105\u010b\u010f\u0112\u0115\u011b\u011e\u0121\u0124"+
		"\u0129\u012d\u0130\u0135\u0138\u0142\u014d\u015a\u015f\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}