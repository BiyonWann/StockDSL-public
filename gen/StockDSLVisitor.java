// Generated from /Users/alexandernanda/Desktop/01_grammar/StockDSL.g4 by ANTLR 4.13.2

package stockdsl;

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link StockDSLParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface StockDSLVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(StockDSLParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#strategyBlock}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStrategyBlock(StockDSLParser.StrategyBlockContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(StockDSLParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#rule}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRule(StockDSLParser.RuleContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction(StockDSLParser.ActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpr(StockDSLParser.ExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#logicalTerm}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalTerm(StockDSLParser.LogicalTermContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#comparison}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparison(StockDSLParser.ComparisonContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#primary}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(StockDSLParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(StockDSLParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#argList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgList(StockDSLParser.ArgListContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#configStmt}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConfigStmt(StockDSLParser.ConfigStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#symbolList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSymbolList(StockDSLParser.SymbolListContext ctx);
	/**
	 * Visit a parse tree produced by {@link StockDSLParser#comparator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparator(StockDSLParser.ComparatorContext ctx);
}