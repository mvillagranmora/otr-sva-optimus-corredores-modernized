SET SCHEMA "BSCL";
 GO
DROP PROCEDURE "BSCL.Normativa.BD.Servicios.UAF::ConsultarPEP";
 GO
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar información para emitir la Circular de Personas Expuestas Políticamente.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 25/07/2016

"BSCL.Normativa.BD.Servicios.UAF::ConsultarPEP" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,

    --VARIABLES DEL PROCESO

    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaPEP	"BSCL.Normativa.BD::NOR.SALIDA_UAFCIR63PEP"

)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------
  		DECLARE v_Fecha NVARCHAR(10):='';
		DECLARE v_Count 							 BIGINT:=0;

		---Parametros

		---Periodo 
		SELECT TOP 1 LEFT("Valor",10) INTO v_Fecha
		FROM "BSCL.Plataforma.BD::PLA.Configuracion" WHERE "CodigoParametro"='PLA.FechaSistema' and "IdEmpresa"=p_IdEmpresa;
		
		v_logo = select TOP 1
		"Binario" AS "LogoCorredora"
		from "BSCL.Plataforma.BD.Vistas::PLA.ConsultarImagen"
		WHERE "NombreImagen" = 'LOGO_CORREDOR' ORDER BY "Version" DESC;
		
		v_tablaOperaciones=
		SELECT
			SOC."IdPersona",
			CLI."IdCliente",
			POR."IdPortafolio",
			POR."CodigoPortafolio",
			CLI."NombreCompleto"	 AS "NombreCliente",
			CLI."DocumentoIdentidad" AS "RutCliente",
			COALESCE(POR."CodigoEmpleado",'')	 AS	"CodigoEjecutivo",
			COALESCE(POR."NombreCompletoEjecutivo",'')	 AS	"NombreEjecutivo"
		FROM  "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
			INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" SOC
			 ON (CLI."IdCliente" =SOC."IdCliente")
			INNER JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersona" IND  --PERSONA POLÍTICAMENTE EXPUESTA PEP
						ON (IND."IdUso"='IND' AND IND."CodigoTipoIndicador"='PEP' AND IND."ValorIndicador"='S'
							AND IND."IdPersona" =SOC."IdPersona")
		INNER JOIN ( ---EJECUTIVOS POR PORTAFOLIOS
					 SELECT  "IdCliente"
							,"IdPortafolio"
					 		,"CodigoPortafolio"
					 		,"IdEmpleadoEjecutivoPortafolio"
					 		,"NombreCompletoEjecutivo"
					 		,"CodigoEmpleado"
					  FROM  "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivoPortafolio"
					 WHERE 	"FechaInicioReemplazo" IS NULL 									 --solo ejecutivo principal
					GROUP BY "IdCliente","IdPortafolio","CodigoPortafolio"
					,"IdEmpleadoEjecutivoPortafolio","NombreCompletoEjecutivo","CodigoEmpleado"  --Solo 1 supervisor
						) POR
			 ON (CLI."IdCliente" =POR."IdCliente")
		ORDER BY CLI."IdCliente", POR."IdPortafolio";
		--LEFT OUTER JOIN  "BSCL.SocioNegocio.BD.Vistas::EJE.ConsultarEjecutivo" EJE
		--	 ON (EJE."IdEmpleadoEjecutivo" =POR."IdEmpleadoEjecutivoPortafolio");	
			 
		--Verificar si existen operaciones 
		SELECT COUNT(1) INTO v_Count FROM :v_tablaOperaciones;
 				 
		IF v_Count > 0 THEN



				/*FLUJO  CUSTODIA*/
				v_tablaInstrumentos=SELECT
									NULL AS  "IdClasificacionTipoInstrumento",
									NULL AS  "IdTipoInstrumento",
									NULL AS  "IdInstrumento",
									NULL AS  "IdEmisor",
									NULL AS  "FechaVencimiento",
									NULL AS  "IdTipoReajuste",
									'N' "EsFungible",
									NULL AS  "IdBoveda" 										
									FROM DUMMY;
						
				v_tablaCarteras=SELECT
									NULL AS "FolioCartera",
									NULL AS "FolioGarantia",
									NULL AS "FolioSubyacente",
									PLA."IdCliente" AS "IdCliente",
									PLA."IdPortafolio" AS "IdPortafolio"
									FROM :v_tablaOperaciones  PLA
									GROUP BY PLA."IdCliente",PLA."IdPortafolio" ;


  V_out_FlujoTotalizadoFinal=SELECT CUS.* FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" CUS
								INNER JOIN :v_tablaCarteras CAR
								ON (CAR."IdCliente"=CUS."IdCliente" AND CAR."IdPortafolio"=CUS."IdPortafolio")
								--INNER JOIN :v_tablaInstrumentos INS
								--ON (INS."IdClasificacionTipoInstrumento"=CUS."IdClasificacionTipoInstrumento")
								WHERE TO_VARCHAR (CUS."FechaLiquidacion", 'YYYY-MM-DD')<=TO_VARCHAR (:v_Fecha, 'YYYY-MM-DD');

		--Valores de Clasificacion del instrumento
		SELECT  COUNT(1) INTO v_Count FROM :V_out_FlujoTotalizadoFinal;			
		if v_Count>0 THEN
		
			v_Datos = SELECT 
				   SOL."IdCliente"
				  ,SOL."IdPortafolio"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='RV'	THEN 'RV' ELSE NULL END AS "RV"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='RF'	THEN 'RF' ELSE NULL END AS "RF"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='IF'	THEN 'IF' ELSE NULL END AS "IF"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='DV'	THEN 'RV' ELSE NULL END AS "DV"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='FM'	THEN 'FM' ELSE NULL END AS "FM"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='SP'	THEN 'SP' ELSE NULL END AS "SP"
				  ,CASE WHEN SOL."IdClasificacionTipoInstrumento"='FW'	THEN 'FW' ELSE NULL END AS "FW"
				FROM :V_out_FlujoTotalizadoFinal SOL
				GROUP BY  SOL."IdCliente",SOL."IdPortafolio",SOL."IdClasificacionTipoInstrumento" ;
	
			v_DatosAgrupado =SELECT 
							 SOL."IdCliente"
							 ,SOL."IdPortafolio" 
							 ,MAX(SOL."RV") AS "RV"
							 ,MAX(SOL."RF") AS "RF"
							 ,MAX(SOL."IF") AS "IF"
							 ,MAX(SOL."DV") AS "DV"
							 ,MAX(SOL."FM") AS "FM"
							 ,MAX(SOL."SP") AS "SP"
							 ,MAX(SOL."FW") AS "FW"
							FROM :v_Datos SOL GROUP BY  SOL."IdCliente",SOL."IdPortafolio";
							
			v_DatosTitulos = SELECT   COALESCE("RV",'') 
				|| (CASE COALESCE("RF",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("RF",'') END )
				|| (CASE COALESCE("IF",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("IF",'') END )
				|| (CASE COALESCE("DV",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("DV",'') END )
				|| (CASE COALESCE("FM",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("FM",'') END )
				|| (CASE COALESCE("SP",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("SP",'') END )
				|| (CASE COALESCE("FW",'') WHEN '' THEN '' ELSE ' - ' ||  COALESCE("FW",'') END )
				 AS "Titulos", "IdCliente" ,"IdPortafolio"  FROM  :v_DatosAgrupado;				
			ELSE
				v_DatosTitulos=SELECT
				'' AS "Titulos",
				0 AS "IdCliente",
				0 AS "IdPortafolio"
				FROM DUMMY WHERE 1=2;	 
			END IF;

						
		--Tabla de Salida
			p_out_TablaPEP=
			SELECT  OPE."IdPersona",
					OPE."IdCliente",
					OPE."CodigoPortafolio",
					OPE."NombreCliente",
					OPE."RutCliente",
					OPE."CodigoEjecutivo",
					OPE."NombreEjecutivo",
					TO_VARCHAR(v_Fecha,'DD-MM-YYYY') AS "FechaConsulta",
					OPE."Titulos" AS "Titulos",
					L."LogoCorredora"
					
			FROM	
			(			
	     			SELECT 
		     			OP."IdPersona",
						OP."IdCliente",
						OP."IdPortafolio",
						OP."CodigoPortafolio",
						OP."NombreCliente",
						OP."RutCliente",
						OP."CodigoEjecutivo",
						OP."NombreEjecutivo",
						COALESCE(TI."Titulos",'') AS "Titulos"
	     			FROM :v_tablaOperaciones OP 
	     			LEFT OUTER JOIN :v_DatosTitulos  TI
	     			ON (OP."IdCliente"=TI."IdCliente" AND OP."IdPortafolio"=TI."IdPortafolio")
	     		
			) OPE CROSS JOIN :v_logo L;
				
		ELSE

		
		p_out_TablaPEP=SELECT
				0 AS "IdPersona",
				0 AS "IdCliente",
				0 AS "CodigoPortafolio",
				'' AS "NombreCliente",
				'' AS "RutCliente",
				'' AS "CodigoEjecutivo",
				'' AS "NombreEjecutivo",
				'' AS "FechaConsulta",
				'' AS "Titulos",
				L."LogoCorredora"
				FROM DUMMY CROSS JOIN :v_logo L  WHERE 1=2;	
		
		END IF;
		SELECT * FROM :p_out_TablaPEP;
END; 