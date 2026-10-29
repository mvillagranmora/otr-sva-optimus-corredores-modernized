--Probando comentario

/* 

DROP PROCEDURE "DummySP";

CREATE PROCEDURE "DummySP" (
    IN  p_inNum   BIGINT,
    IN  p_inChar  NVARCHAR(100),
    IN  p_inDec   DECIMAL,    
	OUT p_out_tab TABLE ("A" bigint,"B" bigint,"C" nvarchar(520),"D" Timestamp,"E" Date,"F" Decimal),
	OUT p_out_var NVARCHAR(40),   	 								  	   
	OUT  p_out_Num BIGINT,                                                  
	OUT  p_out_Dec DECIMAL                                                 
) 	
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER   
	DEFAULT SCHEMA BSCL
	READS SQL DATA AS
BEGIN
	p_out_var := 'DEMO DUMMY';
	p_out_Dec := 123.4567890;
	p_out_Num := 1000;
	p_out_tab = select  p_inNum  as A,2222 AS B,p_inChar as C,NOW() as D,TO_DATE('2010-01-12', 'YYYY-MM-DD') as E,p_inDec as F from dummy;
END;

call "DummySP" (1,' ',12.12,?,?,?,?)



*/

call "DummySP" (:p_inNum,:p_inChar,:p_inDec,$p_out_tab,@p_out_var,@p_out_Num,@p_out_Dec)