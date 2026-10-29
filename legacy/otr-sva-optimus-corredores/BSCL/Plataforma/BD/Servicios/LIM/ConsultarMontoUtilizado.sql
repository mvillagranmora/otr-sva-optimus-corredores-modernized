SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Plataforma.BD.Servicios::LIM.ConsultarMontoUtilizado";

CREATE PROCEDURE
-- 
--  Proposito: Servicio que obtiene informacion sobre el monto utilizado segun producto, 
--  Autor: CFLORES - BCS
--  Fecha Creacion: 14-07-2016
--  
"BSCL.Plataforma.BD.Servicios::LIM.ConsultarMontoUtilizado" (
	-- Parametros entrada comunes en todos los servicios publicos
	IN  p_IdEmpresa									BIGINT,			--ID Empresa destino
	IN  p_IdUsuario									BIGINT,			--ID del usuario que solicita el servicio
	IN  p_IdModulo									NVARCHAR(3),  	--ID del Modulo que lo invoca
	IN  p_UsaReglasNegocio	 						NVARCHAR(1),	-- S/N se usa en caso de servicios anidados.
	IN  p_simularOperacion							NVARCHAR(1) DEFAULT 'N', -- S/N se usa para simular operacion (algunos casos)	  
	-- Parametros entrada del servicio publico
	IN p_tablaOperaciones 		TABLE("IdPortafolio"					BIGINT,
									  "IdCliente"						BIGINT,
									  "MontoUtilizado"          		DECIMAL(26,8),
									  "IdMoneda"                		NVARCHAR(3),
									  "TipoCambio"						DECIMAL(26,8),
									  "IdOperacion" 					BIGINT, 
									  "IdTipoOperacion"        			NVARCHAR(6),
									  "IdTipoInstrumento"       		NVARCHAR(3),
									  "IdClasificacionTipoInstrumento" 	NVARCHAR(3),
									  "IdTipoReajuste"          		NVARCHAR(3),
									  "IdInstrumento"          			BIGINT,
									  "IdEmisor"      		    		NVARCHAR(40),
									  "Tasa"                    		DECIMAL (26,8),
									  "DiasPlazo"               		INTEGER,
									  "FechaOperacion"					NVARCHAR(10),--'YYYY-MM-DD'
									  "CodigoOperador"					NVARCHAR(40),
									  "IdAgente"						BIGINT,
									  "IdCanalIngreso" 					NVARCHAR(3)
									  ),
	IN p_IdTipoEntidad		 	NVARCHAR(40),---ORDEN, SPOT, FORWARD
	IN p_Fecha					NVARCHAR(10),--Fecha a la que se consulta
	-- Parametros salida del servicio publico
	OUT p_out_tablaMontoUtilizado	table (			"IdCliente" 						BIGINT, 
													"IdPortafolio" 						BIGINT,
													"MontoUtilizado"             		DECIMAL(26,8),
													"IdClasificacionTipoInstrumento" 	NVARCHAR(3),
													"IdTipoInstrumento"       	 		NVARCHAR(3),
													"IdMoneda" 					 		NVARCHAR(3))
	-- Salidas para mensajes de advertencias y errores
	/*OUT p_out_AdvertenciasContador 		int,
	OUT p_out_AdvertenciasDetalle 		table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520)),	
	OUT p_out_ErroresContador 			int,
	OUT p_out_ErroresDetalle 			table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520)
*/
	
) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
DEFAULT SCHEMA "BSCL"
AS
BEGIN
	--------------------
	--DECLARE Manejo de Errores
	--------------------	
	
	BEGIN	

		--------------------
		--Declare Variables
		--------------------
	
	    DECLARE v_Count INT:=0;
		DECLARE v_usanCustodia,v_usanOrdenAsignada,v_usanOrdenPorFacturar,v_usanMovimientoEntransito NVARCHAR(200):='';
		
		
		
		--------------------
		--Validaciones
		--------------------
		
		-- Existencia 

		--------------------
		--Reglas Negocio
		--------------------		
		
		--------------------
		--Lógica Negocio
		--------------------
		BEGIN 
			--Inicializacion de variables:
			v_usanCustodia:='ORDEN,TITULO';					--Entidades que requieren Custodia
			v_usanOrdenAsignada:='ORDEN';					--Entidades que requieren Ordenes Asignadas
			v_usanOrdenPorFacturar:='ORDEN';				--Entidades que requieren Ordenes por Facturar
			v_usanMovimientoEntransito:='TITULO';			--Entidades que requieren Ordenes por Facturar


			---Cuando es simulacion la operacion no se encuentra ingresada, se obtiene desde la Entrada
			v_tablaOperacion = SELECT CUS."IdCliente",CUS."IdPortafolio",CUS."MontoUtilizado",CUS."IdClasificacionTipoInstrumento", CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdMoneda"
			  				  FROM :p_tablaOperaciones CUS where 1=2;
			v_tablaMontos  	 = SELECT * FROM :v_tablaOperacion where 1=2;			  
			  				  
							
			IF p_simularOperacion='S' THEN
				v_tablaOperacion= SELECT CUS."IdCliente",CUS."IdPortafolio",CUS."MontoUtilizado",CUS."IdClasificacionTipoInstrumento", CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdMoneda"
			 					  FROM :p_tablaOperaciones CUS;
			END IF;
						

			---La Entidad requiere del monto: En custodia

			SELECT LOCATE (v_usanCustodia, p_IdTipoEntidad) "CUSTODIA" INTO  v_Count FROM DUMMY;
			IF v_Count > 0 THEN
				
				/*FLUJO  CUSTODIA*/
				v_tablaInstrumentos=SELECT
									PLA."IdClasificacionTipoInstrumento" AS  "IdClasificacionTipoInstrumento",
									PLA."IdTipoInstrumento"				 AS  "IdTipoInstrumento",
									NULL								 AS  "IdInstrumento",
									NULL AS  "IdEmisor",
									NULL AS  "FechaVencimiento",
									NULL AS  "IdTipoReajuste",
									'N' "EsFungible",
									NULL AS  "IdBoveda" 										
									FROM :p_tablaOperaciones  PLA
									GROUP BY PLA."IdClasificacionTipoInstrumento", PLA."IdTipoInstrumento";
						
				v_tablaCarteras=SELECT
									NULL AS "FolioCartera",
									NULL AS "FolioGarantia",
									NULL AS "FolioSubyacente",
									PLA."IdCliente" AS "IdCliente",
									PLA."IdPortafolio" AS "IdPortafolio"
									FROM :p_tablaOperaciones  PLA
									GROUP BY PLA."IdCliente",PLA."IdPortafolio" ;
				
				
				  V_out_FlujoTotalizadoFinal=SELECT CUS.*,CUS."Monto" AS "MontoDisponible" FROM "BSCL.Custodia.BD.Vistas::CUS.ConsultarCarteraDisponible" CUS
								INNER JOIN :v_tablaCarteras CAR
								ON (CAR."IdCliente"=CUS."IdCliente" AND CAR."IdPortafolio"=CUS."IdPortafolio")
								INNER JOIN :v_tablaInstrumentos INS
								ON (INS."IdClasificacionTipoInstrumento"=CUS."IdClasificacionTipoInstrumento"
									AND  INS."IdTipoInstrumento"=CUS."IdTipoInstrumento")
								WHERE TO_VARCHAR (CUS."FechaLiquidacion", 'YYYY-MM-DD')=TO_VARCHAR (:p_Fecha, 'YYYY-MM-DD');
								
								
				
				
				
				v_tablaMontos=	
				SELECT CUS."IdCliente",CUS."IdPortafolio",CUS."MontoDisponible" AS "MontoUtilizado",CUS."IdClasificacionTipoInstrumento", CUS."IdTipoInstrumento",CUS."IdInstrumento",(CASE WHEN CUS."IdMoneda"='' THEN 'CLP'ELSE CUS."IdMoneda" END) AS "IdMoneda"
							FROM :V_out_FlujoTotalizadoFinal CUS
							UNION ALL SELECT * FROM :v_tablaMontos;	
					
			END IF;
			
			---La Entidad requiere del monto: En Ordenes Asignadas y Sin Asignar
			SELECT LOCATE (v_usanOrdenAsignada, p_IdTipoEntidad) "ORDEN_ASIGNADAS" INTO  v_Count FROM DUMMY;
			IF v_Count > 0 THEN

				v_tablaMontos=	
							SELECT COM."IdCliente",COM."IdPortafolio",COM."MontoDisponibleOrden" AS "MontoUtilizado",COM."IdClasificacionTipoInstrumento", COM."IdTipoInstrumento",COM."IdInstrumento",COM."IdMonedaOrden" AS "IdMoneda"
							FROM "BSCL.Orden.BD.Vistas::ConsultarOrdenSinAsignar" COM
							INNER JOIN :p_tablaOperaciones OPE ON (OPE."IdCliente"=COM."IdCliente" AND OPE."IdPortafolio"=COM."IdPortafolio" AND OPE."IdClasificacionTipoInstrumento" =COM."IdClasificacionTipoInstrumento"AND OPE."IdTipoInstrumento"=COM."IdTipoInstrumento")
							WHERE  COM."AccionTipoOperacion"='COMPRA'
							UNION ALL SELECT * FROM :v_tablaMontos;	
			END IF;
			
			---La Entidad requiere del monto: En Ordenes por Facturar
/*
			SELECT LOCATE (v_usanOrdenPorFacturar, p_IdTipoEntidad) "ORDEN_PENDIENTES_FACTURACION" INTO  v_Count FROM DUMMY;
			IF v_Count > 0 THEN

				v_tablaMontos=	
							SELECT COM."IdCliente",COM."IdPortafolio",COM."Monto" AS "MontoUtilizado",COM."IdClasificacionTipoInstrumento", COM."IdTipoInstrumento",COM."IdInstrumento",COM."IdMoneda" 
							FROM "BSCL.Facturacion.BD.Vistas::ConsultarMovimientoComprobante" COM
							INNER JOIN :p_tablaOperaciones OPE ON (OPE."IdCliente"=COM."IdCliente" AND OPE."IdPortafolio"=COM."IdPortafolio" AND OPE."IdClasificacionTipoInstrumento" =COM."IdClasificacionTipoInstrumento"AND OPE."IdTipoInstrumento"=COM."IdTipoInstrumento")
							WHERE COM."IdEstadoFacturacion"='ING' AND COM."AccionTipoOperacion"='COMPRA'
							UNION ALL SELECT * FROM :v_tablaMontos;	
			END IF;
	*/
			---La Entidad requiere del monto: En movimientos en Transito
/*
			SELECT LOCATE (v_usanMovimientoEntransito, p_IdTipoEntidad) "MOVIMIENTOS_EN_TRANSITO" INTO  v_Count FROM DUMMY;
			IF v_Count > 0 THEN

				v_tablaMontos=	
							 SELECT * FROM :v_tablaMontos;	
			END IF;
	*/		
		

			p_out_tablaMontoUtilizado=
				SELECT GRU."IdCliente",GRU."IdPortafolio",SUM(GRU."MontoUtilizado") AS "MontoUtilizado",GRU."IdClasificacionTipoInstrumento", GRU."IdTipoInstrumento",GRU."IdMoneda"
				FROM ( 
						SELECT CUS."IdCliente",CUS."IdPortafolio",CUS."MontoUtilizado",CUS."IdClasificacionTipoInstrumento", CUS."IdTipoInstrumento",CUS."IdInstrumento", CUS."IdMoneda" 
						FROM :v_tablaMontos	CUS						
						UNION ALL
						SELECT CUS."IdCliente",CUS."IdPortafolio",CUS."MontoUtilizado",CUS."IdClasificacionTipoInstrumento", CUS."IdTipoInstrumento",CUS."IdInstrumento", CUS."IdMoneda" 
						FROM :v_tablaOperacion	CUS						
				 	) AS GRU
				GROUP BY GRU."IdCliente",GRU."IdPortafolio",GRU."IdClasificacionTipoInstrumento", GRU."IdTipoInstrumento",GRU."IdMoneda";
						
					
			
			
			--SIGNAL SALIDA_CONTROLADA;		
		END;		
					
		--------------------
		--FIN SP 
		--------------------
	END;	
END;
