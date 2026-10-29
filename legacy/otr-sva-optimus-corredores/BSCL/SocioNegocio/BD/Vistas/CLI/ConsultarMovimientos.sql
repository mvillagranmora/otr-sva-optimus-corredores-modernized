SET SCHEMA "BSCL"; 
 
DROP VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarMovimientos"; 
 
CREATE VIEW "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarMovimientos" 
AS
	SELECT       TOP 50
				  "SEC_MOVTO"							AS "Secuencia"
				 ,CASE 
				 	WHEN "IND_ANULADO" = 'A'
				 		THEN 'ANULADO'
				 	ELSE
				 		"EST_MOVTO"
				  END 													AS "Estado"
	             ,"NEMO"												AS "Instrumento"
	             ,"TIPO_REAJ"											AS "Tipo_Reajuste"
	             ,"FEC_LIQUID"											AS "Fecha_Liquidacion"
	             ,"FEC_VCTO"											AS "Fecha_Vencimiento"
	             ,"CANTIDAD"				 							AS "Cantidad"
	             ,"VAL_RESC"											AS "Rescate"
	             ,"TASA"												AS "Tasa"
	             ,"PRECIO"												AS "Precio"
	             ,"MONTO"												AS "Monto"
	             ,"COD_MOVTO"											AS "Cod_Mov"
	             ,CONCAT("COD_MOVTO", CONCAT(' ', "CONCEPTO_MVTO")) 	AS "Concepto"
	             ,C."NOMBRECONCEPTO"									AS "Desc_Movimiento"
	             ,"COD_BOVEDA"											AS "Boveda"
	             ,TRIM("RUT_CLI")										AS "Cliente"
	             ,"COD_RUBRO"											AS "Mercado"
	             ,"MERCADO"												AS "TipoInstumento"
	             
	FROM 		"BSCL.Replica.BD.Vistas::ReplicaTBCTMVTO" M
	LEFT JOIN 	"BSCL.Replica.BD.Vistas::ReplicaPlataforma_Concepto" C 
               ON C."IDCONCEPTO" = M."CONCEPTO_MVTO"
    WHERE 	"COD_RUBRO" IN ('IF','RF','RV')
    AND 	"UNIDAD_DES" != 'TE'
    AND		"EST_MOVTO"	  = 'T';
              

GRANT SELECT ON "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarMovimientos" TO  BCS_DESARROLLADOR WITH GRANT OPTION; 
	