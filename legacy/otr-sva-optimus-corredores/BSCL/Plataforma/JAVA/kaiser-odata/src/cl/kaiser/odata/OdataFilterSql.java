package cl.kaiser.odata;

import cl.kaiser.odata.gen.OdataFilterBaseVisitor;
import cl.kaiser.odata.gen.OdataFilterLexer;
import cl.kaiser.odata.gen.OdataFilterParser;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class OdataFilterSql extends OdataFilterBaseVisitor<String> {
	Map<String, String> parmsValue = new HashMap<String, String>();
	String				odataFilter;
	String				sqlFilter;
	int					parmsCount;
    SimpleDateFormat formatterTS 	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    SimpleDateFormat formatterSD 	= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatterDD 	= new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat formatterTT 	= new SimpleDateFormat("HH:mm:ss");	
	
	public OdataFilterSql(String odataFilter) {
		this.odataFilter=odataFilter!=null?odataFilter:"";
	}
	
	public void Parser() {
		parmsCount = 0;		
		parmsValue.clear();
		sqlFilter="";
		if (odataFilter.length()>0) {
			ANTLRInputStream 	input;
			OdataFilterLexer 	lexer;
			CommonTokenStream 	tokens;
			OdataFilterParser 	parser;
			ParseTree 			tree;
			input = new ANTLRInputStream(odataFilter);
			lexer = new OdataFilterLexer(input);
			tokens = new CommonTokenStream(lexer);
			parser = new OdataFilterParser(tokens);
			tree = parser.prog();
			sqlFilter=this.visit(tree);
		}
	}
	
	private void setValues(NamedParameterStatement smt) throws SQLException, ParseException {
		for (Map.Entry<String, String> entry : parmsValue.entrySet()) {
			String key=entry.getKey().substring(1);
			String val=entry.getValue();
			String type=val.substring(0,val.indexOf("@"));  
			String obj=val.substring(val.indexOf("@")+1);  
			if (type.equals("STRING")) {
				String str=obj.substring(1, obj.length()-1);
				smt.setString(key,str);
			}else if (type.equals("NUMBER")) {
				smt.setLong(key, Long.parseLong(obj));
			}else if (type.equals("DECIMAL")) {
				smt.setDecimal(key, new BigDecimal(obj));
			}else if (type.equals("DATETIME")) {
				String str=obj.substring(1, obj.length()-1);
				Timestamp dval= new Timestamp(formatterSD.parse(str).getTime());
				smt.setTimestamp(key, dval);
			}else {
				throw new SQLException("NOT FOUND PARAM "+key);
			}
		}		
	}
	
	public PreparedStatement getStatement(Connection cnn,String object,String select,String orderby,long top,long skip) throws SQLException {
		String query="";
		String cols="*";	
		if (select!=null) cols=select;
		query="select "+cols+" from "+object+" "+sqlFilter;
		if (orderby!=null) {
			query+=" order by ";
			for (String oCol:orderby.split(",")) {
				String[] p=oCol.split(" ");
				if (p.length==1)query+='"'+p[0]+"\",";
				else query+="\""+p[0]+"\" "+p[1]+",";
			}
			if (query.endsWith(",")) query=query.substring(0,query.length()-1);
		}
		if (top>0) query+=" limit "+top;
		if (skip>0) query+=" offset "+skip;			
		NamedParameterStatement smt=new NamedParameterStatement(cnn, query);
		try {
			setValues(smt);
		} catch (ParseException e) {
			throw new SQLException("PARSER ERROR",e);
		}
		return smt.getStatement();		
	}
	
	public PreparedStatement getCountStatement(Connection cnn,String object) throws SQLException {
		String query="";
		query="select count(1) from "+object+" "+sqlFilter;
		NamedParameterStatement smt=new NamedParameterStatement(cnn, query); 
		try {
			setValues(smt);
		} catch (ParseException e) {
			throw new SQLException("PARSER ERROR",e);
		}
		return smt.getStatement();			
	}
	
	@Override
	public String visitProg(OdataFilterParser.ProgContext ctx) {
		String val="where "+visit(ctx.boolexpr()); 
		if (val.equals("where null")) return "";
		return val;
	}
	
	@Override 
	public String visitGroup(OdataFilterParser.GroupContext ctx) { 
		String val=" ( "+visit(ctx.boolexpr())+ " ) "; 
		return val;
	}
	
	@Override public String visitOperatorAnd(OdataFilterParser.OperatorAndContext ctx) { 
		String val=" ( "+visit(ctx.boolexpr(0))+ " AND " + visit(ctx.boolexpr(1)) + " ) "; 
		return val;	
	}
	
	@Override 
	public String visitOperatorOr(OdataFilterParser.OperatorOrContext ctx) { 
		String val=" ( "+visit(ctx.boolexpr(0))+ " OR " + visit(ctx.boolexpr(1)) + " ) "; 
		return val;	
	}
	
	@Override 
	public String visitOperatorBool(OdataFilterParser.OperatorBoolContext ctx) { 		
		String val=" ( "+visit(ctx.condboolexpr())+ " ) "; 
		return val;		
	}
	

	@Override 
	public String visitNe(OdataFilterParser.NeContext ctx) { 
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " <> " +right; 
		return val;		
	}
	
	@Override 
	public String visitQe(OdataFilterParser.QeContext ctx) { 
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " >= " +right; 
		return val;		
	}
	
	@Override 
	public String visitQt(OdataFilterParser.QtContext ctx) { 
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " > " +right; 
		return val;		
	}
	
	
	@Override 
	public String visitLe(OdataFilterParser.LeContext ctx) { 
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " <= " +right; 
		return val;		
	}	
	
	@Override 
	public String visitLt(OdataFilterParser.LtContext ctx) { 
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " < " +right; 
		return val;		
	}	
	
	@Override 
	public String visitEq(OdataFilterParser.EqContext ctx) {
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));				
		String val=left + " = " +right; 
		return val;				
	}
	
	@Override 
	public String visitEqNull(OdataFilterParser.EqNullContext ctx) {
		String left=visit(ctx.condexpr());			
		String val=left + " is null "; 
		return val;				
	}	
	
	@Override 
	public String visitNqNull(OdataFilterParser.NqNullContext ctx) {
		String left=visit(ctx.condexpr());			
		String val=left + " is not null "; 
		return val;				
	}	
	
	@Override 
	public String visitEqEmpty(OdataFilterParser.EqEmptyContext ctx) {
		String left=visit(ctx.condexpr());			
		String val=left + " =  '' "; 
		return val;				
	}		
	
	@Override 
	public String visitNqEmpty(OdataFilterParser.NqEmptyContext ctx) {
		String left=visit(ctx.condexpr());			
		String val=left + " <>  '' "; 
		return val;				
	}		
		  
	@Override 
	public String visitStartswith(OdataFilterParser.StartswithContext ctx) {
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));	
		String val=" LOCATE ("+left+", "+right+") = 1 ";
		return val;				
	}
	
	@Override 
	public String visitNqStartswith(OdataFilterParser.NqStartswithContext ctx) { 
		String left=visit(ctx.condexpr(0)); 
		String right=visit(ctx.condexpr(1));	
		String val=" LOCATE ("+left+", "+right+") <> 0 ";
		return val;				
	}
	
	
	@Override 
	public String visitEndswith(OdataFilterParser.EndswithContext ctx) {
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));	
		String val=" LOCATE ("+left+", "+right+", -1) = 1 ";	
		return val;				 
	}	
	
	@Override 
	public String visitSubstringof(OdataFilterParser.SubstringofContext ctx) {
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));
		String val=" LOCATE ("+right+", "+left+") > 0 ";	
		return val;				
	}	
	
	@Override 
	public String visitNqSubstringof(OdataFilterParser.NqSubstringofContext ctx) {
		String left=visit(ctx.condexpr(0));
		String right=visit(ctx.condexpr(1));
		String val=" LOCATE ("+right+", "+left+") = 0 ";	
		return val;				
	}		
	
	@Override 
	public String visitToLower(OdataFilterParser.ToLowerContext ctx) { 
		return "LCASE(" + visit(ctx.field()) + ")";
	}
	
	@Override 
	public String visitToUpper(OdataFilterParser.ToUpperContext ctx) { 
		return "UCASE(" +  visit(ctx.field()) + ")"; 
	}
	
	@Override
	public String visitToField(OdataFilterParser.ToFieldContext ctx) { 
		return  visit(ctx.field()); 
	}
	
	@Override 
	public String visitToDatetime(OdataFilterParser.ToDatetimeContext ctx) { 	
		String val=ctx.field().getText();
		if (val.startsWith("'") && val.endsWith("'")) {
			val=val.replace('T', ' ');
			String parm=String.format(":PARMS_%d",parmsCount++);
			parmsValue.put(parm,String.format("DATETIME@%s",val));
			return parm;
		}			
		return val;
	}
	
	@Override
	public String visitField(OdataFilterParser.FieldContext ctx) { 
		String val=ctx.getText(); 
		if (val.startsWith("'") && val.endsWith("'")) {			
			String parm=String.format(":PARMS_%d",parmsCount++);
			parmsValue.put(parm,String.format("STRING@%s",val));
			return parm;
		}		
		if (val.matches("^(-?)[0-9]+$")) {
			String parm=String.format(":PARMS_%d",parmsCount++);
			parmsValue.put(parm,String.format("NUMBER@%s",val));
			return parm;
		}
		if (val.matches("^(-?)[0-9]+[.]([0-9]+)$")) { 
			String parm=String.format(":PARMS_%d",parmsCount++);
			parmsValue.put(parm,String.format("DECIMAL@%s",val));
			return parm;
		}	
		if (val.startsWith("\"") && val.endsWith("\"")) {			
			return val;
		}
		else {
			return '"'+val+'"';
		}
	}	
	
}
