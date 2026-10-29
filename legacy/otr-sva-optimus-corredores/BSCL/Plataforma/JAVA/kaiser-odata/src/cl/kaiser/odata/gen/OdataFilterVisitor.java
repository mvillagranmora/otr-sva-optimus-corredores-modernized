// Generated from ../src/cl/kaiser/odata/OdataFilter.g4 by ANTLR 4.5.3
package cl.kaiser.odata.gen;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link OdataFilterParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface OdataFilterVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link OdataFilterParser#prog}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProg(OdataFilterParser.ProgContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Group}
	 * labeled alternative in {@link OdataFilterParser#boolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGroup(OdataFilterParser.GroupContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OperatorOr}
	 * labeled alternative in {@link OdataFilterParser#boolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorOr(OdataFilterParser.OperatorOrContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OperatorBool}
	 * labeled alternative in {@link OdataFilterParser#boolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorBool(OdataFilterParser.OperatorBoolContext ctx);
	/**
	 * Visit a parse tree produced by the {@code OperatorAnd}
	 * labeled alternative in {@link OdataFilterParser#boolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperatorAnd(OdataFilterParser.OperatorAndContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Eq}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEq(OdataFilterParser.EqContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Ne}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNe(OdataFilterParser.NeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Qe}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQe(OdataFilterParser.QeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Qt}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQt(OdataFilterParser.QtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Le}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLe(OdataFilterParser.LeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Lt}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLt(OdataFilterParser.LtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EqNull}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqNull(OdataFilterParser.EqNullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NqNull}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNqNull(OdataFilterParser.NqNullContext ctx);
	/**
	 * Visit a parse tree produced by the {@code EqEmpty}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqEmpty(OdataFilterParser.EqEmptyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NqEmpty}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNqEmpty(OdataFilterParser.NqEmptyContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Startswith}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStartswith(OdataFilterParser.StartswithContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NqStartswith}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNqStartswith(OdataFilterParser.NqStartswithContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Endswith}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEndswith(OdataFilterParser.EndswithContext ctx);
	/**
	 * Visit a parse tree produced by the {@code Substringof}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSubstringof(OdataFilterParser.SubstringofContext ctx);
	/**
	 * Visit a parse tree produced by the {@code NqSubstringof}
	 * labeled alternative in {@link OdataFilterParser#condboolexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitNqSubstringof(OdataFilterParser.NqSubstringofContext ctx);
	/**
	 * Visit a parse tree produced by the {@code toDatetime}
	 * labeled alternative in {@link OdataFilterParser#condexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToDatetime(OdataFilterParser.ToDatetimeContext ctx);
	/**
	 * Visit a parse tree produced by the {@code toLower}
	 * labeled alternative in {@link OdataFilterParser#condexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToLower(OdataFilterParser.ToLowerContext ctx);
	/**
	 * Visit a parse tree produced by the {@code toUpper}
	 * labeled alternative in {@link OdataFilterParser#condexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToUpper(OdataFilterParser.ToUpperContext ctx);
	/**
	 * Visit a parse tree produced by the {@code toField}
	 * labeled alternative in {@link OdataFilterParser#condexpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitToField(OdataFilterParser.ToFieldContext ctx);
	/**
	 * Visit a parse tree produced by {@link OdataFilterParser#field}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitField(OdataFilterParser.FieldContext ctx);
}