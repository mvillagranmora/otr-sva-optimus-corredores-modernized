SET SCHEMA "BSCL";
GO
DROP PROCEDURE  "BSCL.Normativa.BD.Servicios::PRO.CalcularMontoPromedioCliente";
GO

CREATE PROCEDURE 
--
--  Proposito: Servicio para calcular el monto promedio total diario por cliente a un determinado periodo segun parametros.
--  Autor: CFLORES - BCS
--  Fecha Creacion: 10/01/2017

"BSCL.Normativa.BD.Servicios::PRO.CalcularMontoPromedioCliente" ( 

    --VARIABLES DEL PROCESO
	IN p_IdEmpresa					  	BIGINT,
	IN p_FechaOperacionDesde 			NVARCHAR(10),
	IN p_FechaOperacionHasta 			NVARCHAR(10),
	IN p_ModoProceso 					NVARCHAR(14),
	OUT p_out_MensajeSalida				NVARCHAR(200)
  )
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		--------------------
		--Declare Variables
		
		DECLARE v_IdNormativa 								 NVARCHAR(15):='UAF';
		DECLARE v_IdTipoDocumento   		 	  			 NVARCHAR(10):='UAFSOIEST';
		

		DECLARE v_FechaOperacionDesde,v_FechaOperacionHasta,v_out_FechaAnterior	 NVARCHAR(10);--'YYYY-MM-DD'
		
		DECLARE v_PRO_CAL_PERMONTO_CANTDIAS 	 	 NVARCHAR(520):='';
		DECLARE v_out_error			  						 NVARCHAR(500);
		DECLARE	v_Count,v_IdPersona,v_IdDocumento, v_IdPais,v_NumeroDias,v_IdTrabajoParidad  BIGINT:=0;
		DECLARE v_out_AdvertenciasContador, v_out_ErroresContador INT;
		DECLARE v_out_ErroresDetalle           table ("Codigo" nvarchar(40),"Descripcion" nvarchar(520));
	    DECLARE v_out_AdvertenciasDetalle      table ("Codigo" nvarchar(40),"RequiereAutorizacion" nvarchar(40),"Descripcion" nvarchar(520));	
      

		---Parametros

		v_FechaOperacionDesde=p_FechaOperacionDesde;
		v_FechaOperacionHasta=p_FechaOperacionHasta;
		
	
		IF p_ModoProceso='RECALCULO' OR v_FechaOperacionDesde ='' THEN
				--------------------------------------------------------
				-- Parametro de calculo Dias para el periodo
				--------------------------------------------------------
				SELECT COUNT(1) INTO v_Count  -- cantidad de dias a considerar en el calculo del promedio de montos operados de cada cliente
				FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
				WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_CAL_PERMONTO_CANTDIAS' AND "IdNormativa"=v_IdNormativa;
				
				IF v_Count>0 THEN	
					SELECT "Valor" INTO v_PRO_CAL_PERMONTO_CANTDIAS
						FROM "BSCL.Normativa.BD.Vistas::NOR.ConsultarConfiguracion"
					WHERE "IdEmpresa"=p_IdEmpresa AND "CodigoParametro"='PRO_CAL_PERMONTO_CANTDIAS' AND "IdNormativa"=v_IdNormativa;
					ELSE
					v_PRO_CAL_PERMONTO_CANTDIAS:= '365';			
		
				END IF;	
				v_NumeroDias=	CAST(TRIM(v_PRO_CAL_PERMONTO_CANTDIAS) AS BIGINT);
				SELECT "IdPais" INTO  v_IdPais FROM  "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa"
						WHERE "IdEmpresa"=p_IdEmpresa;
				CALL "BSCL.Plataforma.BD.Utilitarios::ObtenerDiaAnterior" ( p_Fecha=>p_FechaOperacionHasta,p_NumeroDias=>v_NumeroDias, p_EsHabil=>'S',	p_IdPais=>v_IdPais,p_out_FechaAnterior=>v_out_FechaAnterior);
				
				v_FechaOperacionDesde=v_out_FechaAnterior;
		END IF;		
		
		--------------------------------------------------------
		-- Obtencion de Operaciones 
		--------------------------------------------------------

		v_TablaModulo = Select 'FAC' as "IdModulo" from dummy;-- where 1=2;
		
		---Acotar busqueda servicio BASE
		v_registroParametro = SELECT 'FECHADESDE' AS "Codigo", v_FechaOperacionDesde AS "Valor" FROM DUMMY
					UNION ALL SELECT 'FECHAHASTA' AS "Codigo", v_FechaOperacionHasta AS "Valor" FROM DUMMY;
					
			CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
			(
				p_IdEmpresa			=> p_IdEmpresa,
				p_IdNormativa		=> v_IdNormativa,
				p_IdTipoDocumento	=> v_IdTipoDocumento,
				p_tablaModulo		=> :v_TablaModulo,
				p_registroParametro	=> :v_registroParametro,				
				p_out_TablaSalida	=> v_SalidaConsultarOperaciones
			);
		--INSERT INTO "BSCL.Normativa.BD::NOR.GLPROCESO_SALIDA_OPERACIONBASE"
		--SELECT * FROM :v_SalidaConsultarOperaciones;
		---Omite ya existentes cuando no es Recalculo
		IF p_ModoProceso='NUEVOS' THEN
			v_SalidaConsultarOperaciones= SELECT RES.* FROM :v_SalidaConsultarOperaciones RES
										WHERE NOT EXISTS (Select 1 from "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" EST
															WHERE RES."IdCliente"=EST."IdCliente" AND EST."IdEmpresa"=p_IdEmpresa);
		END IF;

		
		SELECT COUNT(1) INTO  v_Count FROM :v_SalidaConsultarOperaciones;
		IF v_Count>0 THEN  -- A) solo con operaciones
		--------------------------------------------------------
		-- PARIDAD MONETARIA
		--------------------------------------------------------
			SELECT COUNT(1) INTO  v_Count FROM :v_SalidaConsultarOperaciones WHERE "IdMoneda"<> 'CLP';
			IF v_Count>0 THEN
			
				SELECT "BSCL.Plataforma.BD.Secuencias::MER.IdTrabajoParidad".NEXTVAL INTO v_IdTrabajoParidad FROM DUMMY;
				
				v_TablaInterparidades= SELECT ROW_NUMBER() OVER() AS "IdInterno",V.* FROM :v_SalidaConsultarOperaciones V
										WHERE "IdMoneda"<> 'CLP';
				
				INSERT INTO "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Entrada_Entrada"("IdTrabajo","IdInterno","Fecha","IdMonedaOrigen","IdMonedaDestino","ValorCambioLocalModificado","MontoOrigen","IdUso")
				SELECT :v_IdTrabajoParidad,Tab."IdInterno",TO_VARCHAR(Tab."FechaMovimiento",'YYYY-MM-DD') AS "Fecha", Tab."IdMoneda" as "IdMonedaOrigen", 'CLP' as "IdMonedaDestino",
				0 as "ValorCambioLocalModificado",Tab."MontoCompra" as "MontoOrigen",'ORDENES' as "IdUso"
				FROM :v_TablaInterparidades Tab;
	
				call "BSCL.Plataforma.BD.Servicios::MER.CalcularParidadYTipoCambio"(
					p_IdEmpresa => :p_IdEmpresa,
					p_IdTrabajo => :v_IdTrabajoParidad
				);
				
				v_OperacionesResultado=
						SELECT  COALESCE(outt."MontoDestino",0) AS  "MontoParidad", par.*
						FROM :v_TablaInterparidades par
						LEFT JOIN "BSCL.Plataforma.BD::MER.CalcularParidadYTipoCambio_Salida_Salida" outt 
						on par."IdInterno" = outt."IdInterno" AND outt."IdTrabajo" = :v_IdTrabajoParidad
						WHERE par."IdMoneda"<> 'CLP'
					UNION ALL
						 SELECT OP."MontoCompra" AS "MontoParidad", 0 AS "IdInterno" ,OP.*
					    FROM :v_SalidaConsultarOperaciones  OP WHERE OP."IdMoneda"= 'CLP';
				
	
			ELSE
				v_OperacionesResultado=
				 	SELECT OP."MontoCompra" AS "MontoParidad", 0 AS "IdInterno" ,OP.*
					    FROM :v_SalidaConsultarOperaciones OP;
			END IF;
		
		
			--------------------------------------------------------
			-- MONTO PROMEDIO DIARIO
			--------------------------------------------------------
			v_TabPromedioDiario=
			SELECT  Diario."IdCliente", Diario."IdTipoInstrumento", Diario."IdClasificacionTipoInstrumento",SUM(Diario."MontoDiario") AS "MontoTotalDias", COUNT(1) AS "CantidadDias",
			 ROUND((SUM(Diario."MontoDiario") / COUNT(1)),0) as "PromedioDiario"
			FROM 
				(SELECT "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD') AS "Fecha","IdTipoInstrumento","IdClasificacionTipoInstrumento", SUM("MontoParidad") AS "MontoDiario" FROM  :v_OperacionesResultado GROUP BY "IdCliente",TO_VARCHAR("FechaMovimiento",'YYYY-MM-DD'),"IdTipoInstrumento","IdClasificacionTipoInstrumento") AS Diario
			GROUP BY Diario."IdCliente", Diario."IdTipoInstrumento", Diario."IdClasificacionTipoInstrumento";
			
			--------------------------------------------------------
			-- MONTO MAXIMO
			--------------------------------------------------------
			v_TabMontoMaximo= SELECT "IdCliente","IdTipoInstrumento","IdClasificacionTipoInstrumento", MAX("MontoParidad") AS "MontoMaximo" FROM  :v_OperacionesResultado GROUP BY "IdCliente", "IdTipoInstrumento", "IdClasificacionTipoInstrumento";
			--------------------------------------------------------
			-- REGISTRAR ESTADISTICA
			--------------------------------------------------------
			IF p_ModoProceso='RECALCULO' THEN
				UPDATE ANT
				SET     ANT."MontoTotalDias" 		= PRO."MontoTotalDias"
					   ,ANT."CantidadDias"			= PRO."CantidadDias"
					   ,ANT."MontoPromedioDiario"	= PRO."PromedioDiario"
					   ,ANT."MontoMaximo"			= MA."MontoMaximo"
					   ,ANT."FechaModificacion"		= CURRENT_UTCTIMESTAMP
				FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente"  ANT
				INNER JOIN 	:v_TabPromedioDiario PRO 
				ON (ANT."IdEmpresa"=p_IdEmpresa AND  ANT."IdCliente"=PRO."IdCliente" AND
					ANT."IdTipoInstrumento"= PRO."IdTipoInstrumento" AND ANT."IdClasificacionTipoInstrumento"=PRO."IdClasificacionTipoInstrumento")
				INNER JOIN :v_TabMontoMaximo MA 
				ON (PRO."IdCliente"=MA."IdCliente" AND PRO."IdTipoInstrumento"=MA."IdTipoInstrumento" AND PRO."IdClasificacionTipoInstrumento"=MA."IdClasificacionTipoInstrumento");
				
				SELECT COUNT(1) INTO v_Count		
				FROM :v_TabPromedioDiario PRO 
						INNER JOIN :v_TabMontoMaximo MA 
						ON (PRO."IdCliente"=MA."IdCliente" AND PRO."IdTipoInstrumento"=MA."IdTipoInstrumento" AND PRO."IdClasificacionTipoInstrumento"=MA."IdClasificacionTipoInstrumento")
						WHERE EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" ANT
											WHERE ANT."IdEmpresa"=p_IdEmpresa AND  ANT."IdCliente"=PRO."IdCliente" AND
											ANT."IdTipoInstrumento"= PRO."IdTipoInstrumento" AND ANT."IdClasificacionTipoInstrumento"=PRO."IdClasificacionTipoInstrumento");
		
				
				p_out_MensajeSalida :='RECALCULO DE VALORES (' || CAST(v_Count AS VARCHAR(10)) || ')';			
			END IF;

			SELECT COUNT(1) INTO v_Count		
			FROM :v_TabPromedioDiario PRO 
						INNER JOIN :v_TabMontoMaximo MA 
						ON (PRO."IdCliente"=MA."IdCliente" AND PRO."IdTipoInstrumento"=MA."IdTipoInstrumento" AND PRO."IdClasificacionTipoInstrumento"=MA."IdClasificacionTipoInstrumento")
						WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" ANT
											WHERE ANT."IdEmpresa"=p_IdEmpresa AND  ANT."IdCliente"=PRO."IdCliente" AND
											ANT."IdTipoInstrumento"= PRO."IdTipoInstrumento" AND ANT."IdClasificacionTipoInstrumento"=PRO."IdClasificacionTipoInstrumento");
							
			IF v_Count>0 THEN --'NUEVOS'
				
				INSERT INTO  "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" (
							   "IdEstadistica",
							   "IdEmpresa",
							   "IdCliente",
							   "MontoTotalDias",
							   "CantidadDias",
							   "MontoPromedioDiario",
							   "MontoMaximo",
							   "IdMoneda",
							   "IdTipoInstrumento",
							   "IdClasificacionTipoInstrumento",
							   "FechaIngreso",
							   "FechaCreacion"
							)
						SELECT 
							"BSCL.Normativa.BD.Secuencias::PRO.IdEstadistica".NEXTVAL AS "IdEstadistica",
							p_IdEmpresa,
							PRO."IdCliente",
							PRO."MontoTotalDias",
							PRO."CantidadDias",
							PRO."PromedioDiario",
							MA."MontoMaximo",
							'CLP',
							PRO."IdTipoInstrumento",
							PRO."IdClasificacionTipoInstrumento",
							v_FechaOperacionHasta,
							CURRENT_UTCTIMESTAMP
						FROM :v_TabPromedioDiario PRO 
						INNER JOIN :v_TabMontoMaximo MA 
						ON (PRO."IdCliente"=MA."IdCliente" AND PRO."IdTipoInstrumento"=MA."IdTipoInstrumento" AND PRO."IdClasificacionTipoInstrumento"=MA."IdClasificacionTipoInstrumento")
						WHERE NOT EXISTS (SELECT 1 FROM "BSCL.Normativa.BD::PRO.EstadisticaOperacionCliente" ANT
											WHERE ANT."IdEmpresa"=p_IdEmpresa AND  ANT."IdCliente"=PRO."IdCliente" AND
											ANT."IdTipoInstrumento"= PRO."IdTipoInstrumento" AND ANT."IdClasificacionTipoInstrumento"=PRO."IdClasificacionTipoInstrumento");
											
						IF 	p_ModoProceso='NUEVOS' THEN						
							p_out_MensajeSalida :='CLIENTES NUEVOS (' || CAST(v_Count AS VARCHAR(10)) || ')';				
							
						END IF;
				
			END IF;--'NUEVOS
			
		ELSE
			p_out_MensajeSalida :='SIN OPERACIONES';	
		
		
		END IF;	-- A) solo con operaciones
				
		p_out_MensajeSalida := TRIM(p_out_MensajeSalida) || ' MODO : ' || TRIM(p_ModoProceso) || ' #DE : '||p_FechaOperacionDesde || ' - #HA:' ||p_FechaOperacionHasta;		


END; 
