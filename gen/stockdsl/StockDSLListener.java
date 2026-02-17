// Generated from /Users/alexandernanda/Desktop/01_grammar/StockDSL.g4 by ANTLR 4.13.2

package stockdsl;

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link StockDSLParser}.
 */
public interface StockDSLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#program}.
	 * @param ctx the parse tree
	 */
	void enterProgram(StockDSLParser.ProgramContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#program}.
	 * @param ctx the parse tree
	 */
	void exitProgram(StockDSLParser.ProgramContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#strategyBlock}.
	 * @param ctx the parse tree
	 */
	void enterStrategyBlock(StockDSLParser.StrategyBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#strategyBlock}.
	 * @param ctx the parse tree
	 */
	void exitStrategyBlock(StockDSLParser.StrategyBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(StockDSLParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(StockDSLParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#rule}.
	 * @param ctx the parse tree
	 */
	void enterRule(StockDSLParser.RuleContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#rule}.
	 * @param ctx the parse tree
	 */
	void exitRule(StockDSLParser.RuleContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(StockDSLParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(StockDSLParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(StockDSLParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(StockDSLParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#logicalTerm}.
	 * @param ctx the parse tree
	 */
	void enterLogicalTerm(StockDSLParser.LogicalTermContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#logicalTerm}.
	 * @param ctx the parse tree
	 */
	void exitLogicalTerm(StockDSLParser.LogicalTermContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#comparison}.
	 * @param ctx the parse tree
	 */
	void enterComparison(StockDSLParser.ComparisonContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#comparison}.
	 * @param ctx the parse tree
	 */
	void exitComparison(StockDSLParser.ComparisonContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterPrimary(StockDSLParser.PrimaryContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitPrimary(StockDSLParser.PrimaryContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void enterFunctionCall(StockDSLParser.FunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#functionCall}.
	 * @param ctx the parse tree
	 */
	void exitFunctionCall(StockDSLParser.FunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#argList}.
	 * @param ctx the parse tree
	 */
	void enterArgList(StockDSLParser.ArgListContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#argList}.
	 * @param ctx the parse tree
	 */
	void exitArgList(StockDSLParser.ArgListContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#configStmt}.
	 * @param ctx the parse tree
	 */
	void enterConfigStmt(StockDSLParser.ConfigStmtContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#configStmt}.
	 * @param ctx the parse tree
	 */
	void exitConfigStmt(StockDSLParser.ConfigStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#symbolList}.
	 * @param ctx the parse tree
	 */
	void enterSymbolList(StockDSLParser.SymbolListContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#symbolList}.
	 * @param ctx the parse tree
	 */
	void exitSymbolList(StockDSLParser.SymbolListContext ctx);
	/**
	 * Enter a parse tree produced by {@link StockDSLParser#comparator}.
	 * @param ctx the parse tree
	 */
	void enterComparator(StockDSLParser.ComparatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link StockDSLParser#comparator}.
	 * @param ctx the parse tree
	 */
	void exitComparator(StockDSLParser.ComparatorContext ctx);
}