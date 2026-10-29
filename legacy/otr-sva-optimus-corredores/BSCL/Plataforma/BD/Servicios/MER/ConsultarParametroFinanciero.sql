SET SCHEMA "BSCL";  
 
DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ConsultarParametroFinanciero";

CREATE PROCEDURE "BSCL.Plataforma.BD.Servicios::MER.ConsultarParametroFinanciero"(
	IN	p_ParametrosConsulta TABLE(
									"IdParametroFinanciero"	NVARCHAR(40),
									"FechaConsulta"					NVARCHAR(10)),
	OUT	p_out_ParametrosResultado TABLE(
								"IdParametroFinanciero"		NVARCHAR(40),
								"FechaConsulta"				NVARCHAR(10),
								"Valor"						DECIMAL(26,8),
								"IdMoneda"					NVARCHAR(3),
								"Fecha"						NVARCHAR(10)) DEFAULT EMPTY,
								
	OUT p_out_ErroresDetalle	TABLE(
									"IdParametroFinanciero"		NVARCHAR(40),
									"FechaConsulta"				NVARCHAR(10),
									"Codigo"					NVARCHAR(40),
									"Descripcion"				NVARCHAR(520)) DEFAULT EMPTY							
	
)
	LANGUAGE SQLSCRIPT 
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"

AS   
  BEGIN	
  	
  	
	p_out_ParametrosResultado = WITH ASDF AS (
			SELECT
				par."IdParametroFinanciero",defpar."Nombre",par."Valor",defpar."IdMonedaValor" as "IdMoneda",par."Fecha",ent."FechaConsulta",
			ROW_NUMBER() OVER (partition by par."IdParametroFinanciero",ent."FechaConsulta" order by par."IdParametroFinanciero",ent."FechaConsulta",par."Fecha" DESC) AS "Prioridad"
			FROM :p_ParametrosConsulta ent
			INNER JOIN "BSCL.Plataforma.BD::MER.ParametroFinanciero" par ON par."IdParametroFinanciero" = ent."IdParametroFinanciero" AND par."Fecha" <= ent."FechaConsulta"
			INNER JOIN "BSCL.Plataforma.BD::MER.DefinicionParametroFinanciero" defpar ON defpar."IdParametroFinanciero" = par."IdParametroFinanciero"
		)
		SELECT TEMP."IdParametroFinanciero",TEMP."FechaConsulta",ASDF."Valor",ASDF."IdMoneda",ASDF."Fecha"
		FROM :p_ParametrosConsulta TEMP
		LEFT JOIN ASDF ON ASDF."IdParametroFinanciero" = TEMP."IdParametroFinanciero" AND ASDF."FechaConsulta" = TEMP."FechaConsulta" AND "Prioridad" = 1;

		v_Errores = SELECT row_number() over (order by "IdParametroFinanciero","FechaConsulta") as "IdInterno",'NO_SE_ENCONTRO_VALOR_PARA_PARAMETRO_FINANCIERO' AS "Codigo",'' as "CodigoDato1",'' as "ValorDato1",
						'' As "CodigoDato2",'' as "ValorDato2","IdParametroFinanciero","FechaConsulta"
						FROM :p_out_ParametrosResultado WHERE "Valor" IS NULL;	
		
		
		CALL "BSCL.Plataforma.BD.Servicios::ConsultarMensajesDiccionarioMasivo" (:v_Errores,v_out_ErroresDetalleMensajes);
		
		p_out_ErroresDetalle = 	SELECT det."IdParametroFinanciero",det."FechaConsulta",
										det."Codigo",
										msj."Descripcion"
								FROM :v_Errores det
								LEFT JOIN :v_out_ErroresDetalleMensajes msj on det."IdInterno" = msj."IdInterno" AND det."Codigo" = msj."Codigo";
			
					
					
 END;
