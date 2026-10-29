SET SCHEMA "BSCL";
		
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.NOR::CIR1720";

CREATE PROCEDURE 
--
--  Proposito: Servicio para la generacion del archivo por JOB Manager .
--  Autor: CFLORES - BCS
--  Fecha Creacion: 17/04/2017
"BSCL.Normativa.BD.Servicios.NOR::CIR1720" (
	--VARIABLES DEL PROCESO
 	IN p_IdSolicitudCircular 		  BIGINT DEFAULT 0, 
 	IN p_IdTipoDocumento   		 	  NVARCHAR(10) DEFAULT '',
	IN p_TablaParametros    		  TABLE("TipoParametro" nvarchar(100) , "Valor" nvarchar(100)) 
    --VARIABLES DE SALIDA 
	,OUT p_out_TablaSalida			  TABLE("IdInterno" BIGINT, "FechaCuadraturaCustodia" SECONDDATE, "Nemotecnico" NVARCHAR(40), "NombreEmisor" NVARCHAR(100), "TipoInstrumento" NVARCHAR(10), "CantidadUnidadesNominal" DECIMAL(26,8),
											"TipoUnidades" VARCHAR(15), "Valorizacion" DECIMAL(26,8), "CarteraPropia" DECIMAL(26,8), "CustodiaTerceros" DECIMAL(26,8), "CustodiaPropiaDCV" DECIMAL(26,8), "CustodiaTercerosDCV" DECIMAL(26,8),
											"CustodiaSociedadEmisora" DECIMAL(26,8), "CustodiaIntermediario" DECIMAL(26,8), "CustodiaBolsas" DECIMAL(26,8), "CustodiaOtros" DECIMAL(26,8), "FechaRegistro" DATE, "IdCuadratura" BIGINT,
											"Tipo" NVARCHAR(40), "NemotecnicoCMF" NVARCHAR(40), "PrecioCierre" DECIMAL(26,8), "Habilitado" NVARCHAR(1))
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
	
	DECLARE v_IdDocumento,v_IdEmpresa, v_Count,v_IdTrabajoParametros,v_IdCliente,v_out_IdCircular1720 INT:=0;
	DECLARE v_FechaOperacionDesde, v_FechaOperacionHasta NVARCHAR(10):='';
	
	DECLARE v_TODOS_CLI,v_TODOS_NEMO		 NVARCHAR(2):='N';
	DECLARE v_NEMO 							 NVARCHAR(40):='';
	DECLARE v_IDCLI				 			 NVARCHAR(40):='0';
	DECLARE v_ParametrosSolicitudRut    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	DECLARE v_ParametrosSolicitudNemo    TABLE("TipoParametro" NVARCHAR(100),"Valor" NVARCHAR(100));
	
	IF p_IdSolicitudCircular>0 THEN
		
		SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
		FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
		WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
		
		SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
		FROM "BSCL.Normativa.BD::DOC.Documento"
		WHERE "IdDocumento" = v_IdDocumento;
	
		 				
		
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='FTER'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"
			FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
		 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
		 	
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" PAR 
						WHERE PAR."IdSolicitudCircular" = p_IdSolicitudCircular;	
			
	END IF;
	
	SELECT  COUNT(1) INTO v_Count FROM :p_TablaParametros;			
	if v_Count>0 THEN
		SELECT TOP 1  "Valor" INTO v_IdEmpresa
			FROM :p_TablaParametros SOL WHERE SOL."TipoParametro"='IdEmpresa';
			
		v_Datos = SELECT 
			  CASE WHEN SOL."TipoParametro"='fechaHasta'	THEN "Valor" ELSE NULL END AS "FTER"
			, CASE WHEN SOL."TipoParametro"='IDCLI'			THEN "Valor" ELSE NULL END AS "IDCLI"
			, CASE WHEN SOL."TipoParametro"='NEMO'			THEN "Valor" ELSE NULL END AS "NEMO"			
			FROM :p_TablaParametros SOL;
			
		 v_DatosParametros= SELECT PAR."TipoParametro" AS "TipoParametro" , PAR."Valor" AS "Valor"
						 FROM :p_TablaParametros PAR UNION ALL
						 SELECT 'IdTipoDocumento' AS "TipoParametro" , p_IdTipoDocumento AS "Valor"
						 FROM DUMMY;		
			
	END IF;


	v_DatosAgrupado =SELECT  
					  MAX(TRIM(SOL."FTER"))		AS "FTER"
					, MAX(TRIM(SOL."IDCLI"))	AS "IDCLI"
					, MAX(TRIM(SOL."NEMO"))		AS "NEMO"
					FROM :v_Datos SOL;
					

	SELECT TOP 1 "FTER", "IDCLI","NEMO" INTO v_FechaOperacionHasta, v_IDCLI, v_NEMO FROM :v_DatosAgrupado;
	
	v_ParametrosSolicitudRut 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	v_ParametrosSolicitudNemo 			= select '' as "TipoParametro", '' as "Valor" from dummy where 1=2;
	
	
	
	SELECT "BSCL.Normativa.BD.Secuencias::GBL.IdTrabajoParametros".NEXTVAL INTO v_IdTrabajoParametros FROM DUMMY;
			
	INSERT INTO "BSCL.Normativa.BD::GBL.Parametros" ("IdInterno","Tipo","NombreParametro","Valor")
	SELECT :v_IdTrabajoParametros AS "IdInterno",'SOLICITUD', PAR."TipoParametro", PAR."Valor"
		FROM :v_DatosParametros PAR;
			
	v_IdCliente = CAST (TRIM(v_IDCLI) AS BIGINT); 	
	
	
	CALL "BSCL.Normativa.BD.Servicios::CIR1720.ConsultarCuadratura"
	(
		p_IdEmpresa					=> v_IdEmpresa,
		p_FechaOperacionDesde		=> v_FechaOperacionHasta,
		p_FechaOperacionHasta		=> v_FechaOperacionHasta,
		p_IdCliente 				=> v_IdCliente,
	    p_Nemotecnico 				=> v_NEMO,
		p_IdTrabajoParametros		=> :v_IdTrabajoParametros,
		p_out_IdInfoTranOfic		=> v_out_IdCircular1720
	);
	
	
	
	select COUNT(1) INTO v_Count from "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1720" WHERE "IdInterno"=v_out_IdCircular1720;
	if v_Count>0 THEN
			p_out_TablaSalida = Select 	* from "BSCL.Normativa.BD::GBL.SALIDA_CIRCULAR1720" WHERE "IdInterno"=v_out_IdCircular1720;
	ELSE
				p_out_TablaSalida=SELECT
					0  AS	"IdInterno"	,
					'0001-01-01 00:00:00'  AS	"FechaCuadraturaCustodia",
					'' AS 		"Nemotecnico",
					'' AS 		"NombreEmisor",
					'' AS 		"TipoInstrumento",
					0  AS		"CantidadUnidadesNominal",
					'' AS 		"TipoUnidades",
					0  AS		"Valorizacion",
					0  AS		"CarteraPropia",
					0  AS		"CustodiaTerceros",
					0  AS		"CustodiaPropiaDCV",
					0  AS		"CustodiaTercerosDCV",
					0  AS		"CustodiaSociedadEmisora",
					0  AS		"CustodiaIntermediario",
					0  AS		"CustodiaBolsas",
					0  AS		"CustodiaOtros",
					'0001-01-01 00:00:00' AS 	"FechaRegistro",					
					0  AS	"IdCuadratura",
					'' AS 	"Tipo",
					'' AS   "NemotecnicoCMF",
					0  AS   "PrecioCierre",
					'S' AS  "Habilitado"
					FROM DUMMY WHERE 1=2;	
				
	END IF;						
		
							SELECT 
							TO_DATE ("FechaCuadraturaCustodia") AS "FechaCuadraturaCustodia",
								"Nemotecnico",
								"NombreEmisor",
								"TipoInstrumento",
								"CantidadUnidadesNominal",
								"TipoUnidades",
								"Valorizacion",
								"CarteraPropia",
								"CustodiaTerceros",
								"CustodiaPropiaDCV",
								"CustodiaTercerosDCV",
								"CustodiaSociedadEmisora",
								"CustodiaIntermediario",
								"CustodiaBolsas",
								"CustodiaOtros",
							TO_DATE ("FechaCuadraturaCustodia") AS	"FechaRegistro",
								"IdCuadratura",
								"Tipo",
								"NemotecnicoCMF",
								"PrecioCierre",
								"Habilitado" 
								FROM :p_out_TablaSalida;								
    
END
