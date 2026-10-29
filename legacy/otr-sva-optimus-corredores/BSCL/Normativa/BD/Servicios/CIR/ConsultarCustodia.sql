SET SCHEMA "BSCL";
GO 
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.ConsultarCustodia";
GO 
CREATE PROCEDURE 
--
--  Proposito: Servicio para consultar custodia para la Circularizacion.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 06/10/2016

"BSCL.Normativa.BD.Servicios::CIR.ConsultarCustodia" ( 

	--VARIABLES DE ENTRADA OBLIGATORIAS
    IN p_IdEmpresa			BIGINT,
    IN p_FechaConsulta		NVARCHAR(10),

    --VARIABLES DEL PROCESO
	IN p_FormaCustodia				NVARCHAR(40) DEFAULT '',
	IN p_TipoFechaConsulta			NVARCHAR(3) DEFAULT 'LIQ', --PARA MANTENER EL COMPORTAMIENTO ORIGINAL	
	IN p_TablaCarteras    		  	TABLE("IdCliente"							BIGINT,
										  "IdPortafolio"						BIGINT),
										  
	IN p_TablaInstrumentos 			TABLE("IdClasificacionTipoInstrumento"	NVARCHAR(3)),
    --VARIABLES DE SALIDA OBLIGATORIAS

	OUT p_out_TablaSalida table ( "TipoCartera"							NVARCHAR(40)
								 ,"FechaOperacion"	NVARCHAR(10)
								 ,"IdBoveda"			NVARCHAR(3)
								 ,"CantidadDisponible"	DECIMAL(26,8)
								 ,"IdInstrumento" 		BIGINT
								 ,"Nemotecnico"			NVARCHAR(40)
								 ,"IdEmisor"			NVARCHAR(40)
								 ,"IdTipoInstrumento"	NVARCHAR(3)
								 ,"NombreTipoInstrumento" NVARCHAR(100)
								 ,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
								 ,"NombreClasificacionTipoInstrumento"	NVARCHAR(100)
								 ,"EsFungible"			NVARCHAR(1)
								 ,"FechaVencimiento" NVARCHAR(10)
								 ,"IdMoneda"	 	NVARCHAR(3)
								 ,"PrecioMercadoCustodia"			 DECIMAL(26,8)
								 ,"TasaMercadoActual"		 DECIMAL(26,8)
								 ,"IdCliente"		BIGINT
								 ,"IdPortafolio"	BIGINT
								 ,"ValorMercadoCustodia"					DECIMAL(26,8)
								 ,"MontoMercadoCustodia"					DECIMAL(26,8)
								 ,"IdTipoOperacion"                         NVARCHAR(40)
								    )
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		--------------------

		/*FLUJO  CUSTODIA*/
		v_tablaInstrumentos=SELECT
							"IdClasificacionTipoInstrumento" AS  "IdClasificacionTipoInstrumento",
							NULL AS  "IdTipoInstrumento",
							NULL AS  "IdInstrumento",
							NULL AS  "IdEmisor",
							NULL AS  "FechaVencimiento",
							NULL AS  "IdTipoReajuste",
							'N' "EsFungible",
							NULL AS  "IdBoveda" 										
							FROM :p_TablaInstrumentos;
				
		v_tablaCarteras=SELECT
							NULL AS "FolioCartera",
							NULL AS "FolioGarantia",
							NULL AS "FolioSubyacente",
							"IdCliente" AS "IdCliente",
							"IdPortafolio" AS "IdPortafolio"
							FROM :p_TablaCarteras;
							
	


			INSERT INTO "BSCL.Custodia.BD::GBL.ConsultarCertificadoCustodia_Entrada_tblInstrumento"("IdClasificacionTipoInstrumento")
			SELECT  "IdClasificacionTipoInstrumento" FROM :p_TablaInstrumentos;

				  
			INSERT INTO "BSCL.Custodia.BD::GBL.ConsultarCertificadoCustodia_Entrada_tblCartera"("IdCliente", "IdPortafolio")
			SELECT "IdCliente", "IdPortafolio" FROM :v_tablaCarteras;


				CALL "BSCL.Custodia.BD.Servicios::ConsultarCertificadoCustodia"(
				   p_IdEmpresa  =>p_IdEmpresa
				  ,p_FechaConsulta =>TO_VARCHAR (:p_FechaConsulta, 'YYYY-MM-DD')
				  ,p_TipoFechaConsulta => p_TipoFechaConsulta
				  ,p_out_tbl=> V_out_FlujoTotalizadoFinal);
				  
				  
  ---Este servicio es utilizado por el Certificado de Custodia y la Circularizacion
  ---Si son cambios especificos, realizar en servicio propio (circularizacion/certificado) que hacen la llamada
	p_out_TablaSalida=SELECT     "TipoCartera"
								 ,"FechaOperacion"
								 ,"IdBoveda"
								 ,"CantidadCustodia"  AS "CantidadDisponible" 
								 ,"IdInstrumento"
								 ,"Nemotecnico"
								 ,"IdEmisor"
								 ,"IdTipoInstrumento"
								 ,"NombreTipoInstrumento"
								 ,"IdClasificacionTipoInstrumento"
								 ,"NombreClasificacionTipoInstrumento"
								 ,"EsFungible"
								 ,"FechaVencimiento"
								 ,"IdMoneda"
								 ,"PrecioMercadoCustodia"
								 ,"TasaMercadoActual"
								 ,"IdCliente"
								 ,"IdPortafolio"
								 ,"ValorMercadoCustodia"
								 ,"MontoMercadoCustodia"
								 ,"IdTipoOperacion"
			FROM :V_out_FlujoTotalizadoFinal WHERE "TipoCartera"='CUSTODIA'
			UNION ALL
			SELECT 				 "TipoCartera"
								 ,"FechaOperacion"
								 ,"IdBoveda"
								 ,"CantidadCustodia"  AS "CantidadDisponible" 
								 ,"IdInstrumento"
								 ,"Nemotecnico"
								 ,"IdEmisor"
								 ,"IdTipoInstrumento"
								 ,"NombreTipoInstrumento"
								 ,"IdClasificacionTipoInstrumento"
								 ,"NombreClasificacionTipoInstrumento"
								 ,"EsFungible"
								 ,"FechaVencimiento"
								 ,"IdMoneda"
								 ,"PrecioMercadoCustodia"
								 ,"TasaMercadoActual"
								 ,"IdCliente"
								 ,"IdPortafolio"
								 ,"ValorMercadoCustodia"
								 ,"MontoMercadoCustodia"
								 ,"IdTipoOperacion"
			FROM :V_out_FlujoTotalizadoFinal WHERE "TipoCartera"='CUSTODIA_NEGATIVA'
			UNION ALL
			SELECT 				 "TipoCartera"
								 ,"FechaOperacion"
								 ,"IdBoveda"
								 ,"CantidadGarantia"  AS "CantidadDisponible" 
								 ,"IdInstrumento"
								 ,"Nemotecnico"
								 ,"IdEmisor"
								 ,"IdTipoInstrumento"
								 ,"NombreTipoInstrumento"
								 ,"IdClasificacionTipoInstrumento"
								 ,"NombreClasificacionTipoInstrumento"
								 ,"EsFungible"
								 ,"FechaVencimiento"
								 ,"IdMoneda"
								 ,"PrecioMercadoCustodia"
								 ,"TasaMercadoActual"
								 ,"IdCliente"
								 ,"IdPortafolio"
								 ,"ValorMercadoCustodia"
								 ,"MontoMercadoCustodia"
								 ,"IdTipoOperacion"
			FROM :V_out_FlujoTotalizadoFinal WHERE "TipoCartera"='GARANTIAS';
			
	

END; 
