// Generated from ../src/cl/kaiser/odata/OdataFilter.g4 by ANTLR 4.5.3
package cl.kaiser.odata.gen;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class OdataFilterParser extends Parser {
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
	public static final int
		RULE_prog = 0, RULE_boolexpr = 1, RULE_condboolexpr = 2, RULE_condexpr = 3, 
		RULE_field = 4;
	public static final String[] ruleNames = {
		"prog", "boolexpr", "condboolexpr", "condexpr", "field"
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

	@Override
	public String getGrammarFileName() { return "OdataFilter.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public OdataFilterParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ProgContext extends ParserRuleContext {
		public BoolexprContext boolexpr() {
			return getRuleContext(BoolexprContext.class,0);
		}
		public TerminalNode EOF() { return getToken(OdataFilterParser.EOF, 0); }
		public ProgContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prog; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitProg(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgContext prog() throws RecognitionException {
		ProgContext _localctx = new ProgContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_prog);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(10);
			boolexpr(0);
			setState(11);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BoolexprContext extends ParserRuleContext {
		public BoolexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boolexpr; }
	 
		public BoolexprContext() { }
		public void copyFrom(BoolexprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class GroupContext extends BoolexprContext {
		public BoolexprContext boolexpr() {
			return getRuleContext(BoolexprContext.class,0);
		}
		public GroupContext(BoolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitGroup(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OperatorOrContext extends BoolexprContext {
		public List<BoolexprContext> boolexpr() {
			return getRuleContexts(BoolexprContext.class);
		}
		public BoolexprContext boolexpr(int i) {
			return getRuleContext(BoolexprContext.class,i);
		}
		public OperatorOrContext(BoolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitOperatorOr(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OperatorBoolContext extends BoolexprContext {
		public CondboolexprContext condboolexpr() {
			return getRuleContext(CondboolexprContext.class,0);
		}
		public OperatorBoolContext(BoolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitOperatorBool(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class OperatorAndContext extends BoolexprContext {
		public List<BoolexprContext> boolexpr() {
			return getRuleContexts(BoolexprContext.class);
		}
		public BoolexprContext boolexpr(int i) {
			return getRuleContext(BoolexprContext.class,i);
		}
		public OperatorAndContext(BoolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitOperatorAnd(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BoolexprContext boolexpr() throws RecognitionException {
		return boolexpr(0);
	}

	private BoolexprContext boolexpr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BoolexprContext _localctx = new BoolexprContext(_ctx, _parentState);
		BoolexprContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_boolexpr, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(19);
			switch (_input.LA(1)) {
			case T__0:
				{
				_localctx = new GroupContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(14);
				match(T__0);
				setState(15);
				boolexpr(0);
				setState(16);
				match(T__1);
				}
				break;
			case T__14:
			case T__17:
			case T__18:
			case T__19:
			case T__20:
			case T__21:
			case Number:
			case Variable:
			case IdentName:
			case CharacterLiteral:
			case StringLiteral:
			case FloatingPointLiteral:
				{
				_localctx = new OperatorBoolContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(18);
				condboolexpr();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(29);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(27);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						_localctx = new OperatorOrContext(new BoolexprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
						setState(21);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(22);
						match(T__2);
						setState(23);
						boolexpr(4);
						}
						break;
					case 2:
						{
						_localctx = new OperatorAndContext(new BoolexprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_boolexpr);
						setState(24);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(25);
						match(T__3);
						setState(26);
						boolexpr(3);
						}
						break;
					}
					} 
				}
				setState(31);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class CondboolexprContext extends ParserRuleContext {
		public CondboolexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condboolexpr; }
	 
		public CondboolexprContext() { }
		public void copyFrom(CondboolexprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class EqEmptyContext extends CondboolexprContext {
		public CondexprContext condexpr() {
			return getRuleContext(CondexprContext.class,0);
		}
		public EqEmptyContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitEqEmpty(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class QtContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public QtContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitQt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NqStartswithContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public NqStartswithContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitNqStartswith(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LtContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public LtContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitLt(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EqNullContext extends CondboolexprContext {
		public CondexprContext condexpr() {
			return getRuleContext(CondexprContext.class,0);
		}
		public EqNullContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitEqNull(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EqContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public EqContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitEq(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class SubstringofContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public SubstringofContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitSubstringof(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class EndswithContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public EndswithContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitEndswith(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NqEmptyContext extends CondboolexprContext {
		public CondexprContext condexpr() {
			return getRuleContext(CondexprContext.class,0);
		}
		public NqEmptyContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitNqEmpty(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class QeContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public QeContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitQe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NeContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public NeContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitNe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class LeContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public LeContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitLe(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NqSubstringofContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public NqSubstringofContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitNqSubstringof(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class NqNullContext extends CondboolexprContext {
		public CondexprContext condexpr() {
			return getRuleContext(CondexprContext.class,0);
		}
		public NqNullContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitNqNull(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class StartswithContext extends CondboolexprContext {
		public List<CondexprContext> condexpr() {
			return getRuleContexts(CondexprContext.class);
		}
		public CondexprContext condexpr(int i) {
			return getRuleContext(CondexprContext.class,i);
		}
		public StartswithContext(CondboolexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitStartswith(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CondboolexprContext condboolexpr() throws RecognitionException {
		CondboolexprContext _localctx = new CondboolexprContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_condboolexpr);
		try {
			setState(105);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				_localctx = new EqContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(32);
				condexpr();
				setState(33);
				match(T__4);
				setState(34);
				condexpr();
				}
				break;
			case 2:
				_localctx = new NeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(36);
				condexpr();
				setState(37);
				match(T__5);
				setState(38);
				condexpr();
				}
				break;
			case 3:
				_localctx = new QeContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(40);
				condexpr();
				setState(41);
				match(T__6);
				setState(42);
				condexpr();
				}
				break;
			case 4:
				_localctx = new QtContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(44);
				condexpr();
				setState(45);
				match(T__7);
				setState(46);
				condexpr();
				}
				break;
			case 5:
				_localctx = new LeContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(48);
				condexpr();
				setState(49);
				match(T__8);
				setState(50);
				condexpr();
				}
				break;
			case 6:
				_localctx = new LtContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(52);
				condexpr();
				setState(53);
				match(T__9);
				setState(54);
				condexpr();
				}
				break;
			case 7:
				_localctx = new EqNullContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(56);
				condexpr();
				setState(57);
				match(T__10);
				}
				break;
			case 8:
				_localctx = new NqNullContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(59);
				condexpr();
				setState(60);
				match(T__11);
				}
				break;
			case 9:
				_localctx = new EqEmptyContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(62);
				condexpr();
				setState(63);
				match(T__12);
				}
				break;
			case 10:
				_localctx = new NqEmptyContext(_localctx);
				enterOuterAlt(_localctx, 10);
				{
				setState(65);
				condexpr();
				setState(66);
				match(T__13);
				}
				break;
			case 11:
				_localctx = new StartswithContext(_localctx);
				enterOuterAlt(_localctx, 11);
				{
				setState(68);
				match(T__14);
				setState(69);
				match(T__0);
				setState(70);
				condexpr();
				setState(71);
				match(T__15);
				setState(72);
				condexpr();
				setState(73);
				match(T__1);
				}
				break;
			case 12:
				_localctx = new NqStartswithContext(_localctx);
				enterOuterAlt(_localctx, 12);
				{
				setState(75);
				match(T__14);
				setState(76);
				match(T__0);
				setState(77);
				condexpr();
				setState(78);
				match(T__15);
				setState(79);
				condexpr();
				setState(80);
				match(T__1);
				setState(81);
				match(T__16);
				}
				break;
			case 13:
				_localctx = new EndswithContext(_localctx);
				enterOuterAlt(_localctx, 13);
				{
				setState(83);
				match(T__17);
				setState(84);
				match(T__0);
				setState(85);
				condexpr();
				setState(86);
				match(T__15);
				setState(87);
				condexpr();
				setState(88);
				match(T__1);
				}
				break;
			case 14:
				_localctx = new SubstringofContext(_localctx);
				enterOuterAlt(_localctx, 14);
				{
				setState(90);
				match(T__18);
				setState(91);
				match(T__0);
				setState(92);
				condexpr();
				setState(93);
				match(T__15);
				setState(94);
				condexpr();
				setState(95);
				match(T__1);
				}
				break;
			case 15:
				_localctx = new NqSubstringofContext(_localctx);
				enterOuterAlt(_localctx, 15);
				{
				setState(97);
				match(T__18);
				setState(98);
				match(T__0);
				setState(99);
				condexpr();
				setState(100);
				match(T__15);
				setState(101);
				condexpr();
				setState(102);
				match(T__1);
				setState(103);
				match(T__16);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class CondexprContext extends ParserRuleContext {
		public CondexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_condexpr; }
	 
		public CondexprContext() { }
		public void copyFrom(CondexprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ToDatetimeContext extends CondexprContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public ToDatetimeContext(CondexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitToDatetime(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ToLowerContext extends CondexprContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public ToLowerContext(CondexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitToLower(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ToFieldContext extends CondexprContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public ToFieldContext(CondexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitToField(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ToUpperContext extends CondexprContext {
		public FieldContext field() {
			return getRuleContext(FieldContext.class,0);
		}
		public ToUpperContext(CondexprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitToUpper(this);
			else return visitor.visitChildren(this);
		}
	}

	public final CondexprContext condexpr() throws RecognitionException {
		CondexprContext _localctx = new CondexprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_condexpr);
		try {
			setState(120);
			switch (_input.LA(1)) {
			case T__19:
				_localctx = new ToDatetimeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				match(T__19);
				setState(108);
				field();
				}
				break;
			case T__20:
				_localctx = new ToLowerContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(109);
				match(T__20);
				setState(110);
				match(T__0);
				setState(111);
				field();
				setState(112);
				match(T__1);
				}
				break;
			case T__21:
				_localctx = new ToUpperContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(114);
				match(T__21);
				setState(115);
				match(T__0);
				setState(116);
				field();
				setState(117);
				match(T__1);
				}
				break;
			case Number:
			case Variable:
			case IdentName:
			case CharacterLiteral:
			case StringLiteral:
			case FloatingPointLiteral:
				_localctx = new ToFieldContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(119);
				field();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FieldContext extends ParserRuleContext {
		public TerminalNode StringLiteral() { return getToken(OdataFilterParser.StringLiteral, 0); }
		public TerminalNode CharacterLiteral() { return getToken(OdataFilterParser.CharacterLiteral, 0); }
		public TerminalNode IdentName() { return getToken(OdataFilterParser.IdentName, 0); }
		public TerminalNode FloatingPointLiteral() { return getToken(OdataFilterParser.FloatingPointLiteral, 0); }
		public TerminalNode Number() { return getToken(OdataFilterParser.Number, 0); }
		public TerminalNode Variable() { return getToken(OdataFilterParser.Variable, 0); }
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof OdataFilterVisitor ) return ((OdataFilterVisitor<? extends T>)visitor).visitField(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_field);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(122);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << Number) | (1L << Variable) | (1L << IdentName) | (1L << CharacterLiteral) | (1L << StringLiteral) | (1L << FloatingPointLiteral))) != 0)) ) {
			_errHandler.recoverInline(this);
			} else {
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 1:
			return boolexpr_sempred((BoolexprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean boolexpr_sempred(BoolexprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 3);
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\3\37\177\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\3\3\5\3"+
		"\26\n\3\3\3\3\3\3\3\3\3\3\3\3\3\7\3\36\n\3\f\3\16\3!\13\3\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4"+
		"\5\4l\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5{\n\5"+
		"\3\6\3\6\3\6\2\3\4\7\2\4\6\b\n\2\3\3\2\31\36\u008d\2\f\3\2\2\2\4\25\3"+
		"\2\2\2\6k\3\2\2\2\bz\3\2\2\2\n|\3\2\2\2\f\r\5\4\3\2\r\16\7\2\2\3\16\3"+
		"\3\2\2\2\17\20\b\3\1\2\20\21\7\3\2\2\21\22\5\4\3\2\22\23\7\4\2\2\23\26"+
		"\3\2\2\2\24\26\5\6\4\2\25\17\3\2\2\2\25\24\3\2\2\2\26\37\3\2\2\2\27\30"+
		"\f\5\2\2\30\31\7\5\2\2\31\36\5\4\3\6\32\33\f\4\2\2\33\34\7\6\2\2\34\36"+
		"\5\4\3\5\35\27\3\2\2\2\35\32\3\2\2\2\36!\3\2\2\2\37\35\3\2\2\2\37 \3\2"+
		"\2\2 \5\3\2\2\2!\37\3\2\2\2\"#\5\b\5\2#$\7\7\2\2$%\5\b\5\2%l\3\2\2\2&"+
		"\'\5\b\5\2\'(\7\b\2\2()\5\b\5\2)l\3\2\2\2*+\5\b\5\2+,\7\t\2\2,-\5\b\5"+
		"\2-l\3\2\2\2./\5\b\5\2/\60\7\n\2\2\60\61\5\b\5\2\61l\3\2\2\2\62\63\5\b"+
		"\5\2\63\64\7\13\2\2\64\65\5\b\5\2\65l\3\2\2\2\66\67\5\b\5\2\678\7\f\2"+
		"\289\5\b\5\29l\3\2\2\2:;\5\b\5\2;<\7\r\2\2<l\3\2\2\2=>\5\b\5\2>?\7\16"+
		"\2\2?l\3\2\2\2@A\5\b\5\2AB\7\17\2\2Bl\3\2\2\2CD\5\b\5\2DE\7\20\2\2El\3"+
		"\2\2\2FG\7\21\2\2GH\7\3\2\2HI\5\b\5\2IJ\7\22\2\2JK\5\b\5\2KL\7\4\2\2L"+
		"l\3\2\2\2MN\7\21\2\2NO\7\3\2\2OP\5\b\5\2PQ\7\22\2\2QR\5\b\5\2RS\7\4\2"+
		"\2ST\7\23\2\2Tl\3\2\2\2UV\7\24\2\2VW\7\3\2\2WX\5\b\5\2XY\7\22\2\2YZ\5"+
		"\b\5\2Z[\7\4\2\2[l\3\2\2\2\\]\7\25\2\2]^\7\3\2\2^_\5\b\5\2_`\7\22\2\2"+
		"`a\5\b\5\2ab\7\4\2\2bl\3\2\2\2cd\7\25\2\2de\7\3\2\2ef\5\b\5\2fg\7\22\2"+
		"\2gh\5\b\5\2hi\7\4\2\2ij\7\23\2\2jl\3\2\2\2k\"\3\2\2\2k&\3\2\2\2k*\3\2"+
		"\2\2k.\3\2\2\2k\62\3\2\2\2k\66\3\2\2\2k:\3\2\2\2k=\3\2\2\2k@\3\2\2\2k"+
		"C\3\2\2\2kF\3\2\2\2kM\3\2\2\2kU\3\2\2\2k\\\3\2\2\2kc\3\2\2\2l\7\3\2\2"+
		"\2mn\7\26\2\2n{\5\n\6\2op\7\27\2\2pq\7\3\2\2qr\5\n\6\2rs\7\4\2\2s{\3\2"+
		"\2\2tu\7\30\2\2uv\7\3\2\2vw\5\n\6\2wx\7\4\2\2x{\3\2\2\2y{\5\n\6\2zm\3"+
		"\2\2\2zo\3\2\2\2zt\3\2\2\2zy\3\2\2\2{\t\3\2\2\2|}\t\2\2\2}\13\3\2\2\2"+
		"\7\25\35\37kz";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}